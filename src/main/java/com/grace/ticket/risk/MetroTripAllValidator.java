package com.grace.ticket.risk;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component  // 添加这个注解
public class MetroTripAllValidator {

	// 高德地图API配置 - 使用您提供的API Key
	private static final String AMAP_API_KEY = "dbf17a822ded817a149e45cb535f953a";
	private static final String AMAP_BASE_URL = "https://restapi.amap.com/v3";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 验证两次行程的合理性
	 */
	public RiskAssessment validateTripFeasibility(TripRecord previousTrip, TripRecord nextTrip) {
		// 计算可用中转时间
		long availableMinutes = Duration.between(previousTrip.getEndTime(), nextTrip.getStartTime()).toMinutes();

		if (availableMinutes <= 0) {
			return new RiskAssessment(RiskAssessment.RiskLevel.IMPOSSIBLE, "时间逻辑错误：第二次行程开始时间早于第一次结束时间", false, 0.95);
		}

		// 获取各种交通方式的路线规划
		List<RouteResult> routes = calculateAllRoutes(previousTrip.getEndStation(), nextTrip.getStartStation());

		// 风险评估
		return assessRisk(availableMinutes, routes, previousTrip, nextTrip);
	}

	/**
	 * 使用高德地图API计算所有可能的路线（包含自驾方案）
	 */
	private List<RouteResult> calculateAllRoutes(String fromStation, String toStation) {
		List<RouteResult> routes = new ArrayList<>();

		try {
			// 1. 获取地铁站的地理坐标
			String fromLocation = getStationLocation(fromStation);
			String toLocation = getStationLocation(toStation);

			if (fromLocation != null && toLocation != null) {
				System.out.println(
						"获取到坐标: " + fromStation + " -> " + fromLocation + ", " + toStation + " -> " + toLocation);

				// 2. 获取地铁路线
				RouteResult subwayRoute = getSubwayRoute(fromLocation, toLocation);
				if (subwayRoute != null) {
					routes.add(subwayRoute);
					System.out.println("地铁路线: " + subwayRoute.getDuration() + "分钟, " + subwayRoute.getCost() + "元");
				}

				// 3. 获取打车路线
				RouteResult taxiRoute = getTaxiRoute(fromLocation, toLocation);
				if (taxiRoute != null) {
					routes.add(taxiRoute);
					System.out.println("打车路线: " + taxiRoute.getDuration() + "分钟, " + taxiRoute.getCost() + "元");
				}

				// 4. 获取自驾路线
				RouteResult drivingRoute = getDrivingRoute(fromLocation, toLocation);
				if (drivingRoute != null) {
					routes.add(drivingRoute);
					System.out.println("自驾路线: " + drivingRoute.getDuration() + "分钟, " + drivingRoute.getCost() + "元");
				}

				// 5. 获取公交路线
				RouteResult busRoute = getBusRoute(fromLocation, toLocation);
				if (busRoute != null) {
					routes.add(busRoute);
					System.out.println("公交路线: " + busRoute.getDuration() + "分钟, " + busRoute.getCost() + "元");
				}
			} else {
				System.out.println("无法获取站点坐标，使用降级方案");
				routes.addAll(getFallbackRoutes(fromStation, toStation));
			}

		} catch (Exception e) {
			System.err.println("调用高德地图API失败: " + e.getMessage());
			e.printStackTrace();
			// 降级方案：使用默认的估算方法
			routes.addAll(getFallbackRoutes(fromStation, toStation));
		}

		return routes;
	}

	/**
	 * 获取地铁站的经纬度坐标
	 */
	private String getStationLocation(String stationName) throws Exception {
		String url = String.format("%s/place/text?key=%s&keywords=%s&city=深圳&types=150700&offset=1", AMAP_BASE_URL,
				AMAP_API_KEY, java.net.URLEncoder.encode(stationName + "地铁站", "UTF-8"));

		//System.out.println("请求地点搜索API: " + url);

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JsonNode root = objectMapper.readTree(responseString);

			//System.out.println("地点搜索响应状态: " + root.get("status").asText());

			if ("1".equals(root.get("status").asText()) && root.get("pois").size() > 0) {
				JsonNode poi = root.get("pois").get(0);
				return poi.get("location").asText(); // 格式: "经度,纬度"
			} else {
				System.err.println("地点搜索API返回状态异常: " + root.get("status").asText());
				if (root.has("info")) {
					System.err.println("错误信息: " + root.get("info").asText());
				}
			}
		}
		return null;
	}

	/**
	 * 获取地铁路线
	 */
	private RouteResult getSubwayRoute(String fromLocation, String toLocation) throws Exception {
		String url = String.format("%s/direction/transit/integrated?key=%s&origin=%s&destination=%s&city=深圳&strategy=0",
				AMAP_BASE_URL, AMAP_API_KEY, fromLocation, toLocation);

		//System.out.println("请求地铁路线API: " + url);

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JsonNode root = objectMapper.readTree(responseString);

			System.out.println("地铁路线响应状态: " + root.get("status").asText());

			if ("1".equals(root.get("status").asText()) && root.get("route").has("transits")
					&& root.get("route").get("transits").size() > 0) {
				JsonNode transit = root.get("route").get("transits").get(0);

				int duration = (int) Math.ceil(transit.get("duration").asDouble() / 60); // 秒转分钟
				int distance = transit.get("distance").asInt(); // 米
				double cost = calculateSubwayCost(distance, transit);

				return new RouteResult("subway", duration, distance, cost);
			} else {
				System.err.println("地铁路线规划失败: " + root.get("info").asText());
			}
		}
		return null;
	}

	/**
	 * 获取打车路线
	 */
	private RouteResult getTaxiRoute(String fromLocation, String toLocation) throws Exception {
		String url = String.format("%s/direction/driving?key=%s&origin=%s&destination=%s&strategy=2", AMAP_BASE_URL,
				AMAP_API_KEY, fromLocation, toLocation);

		//System.out.println("请求打车路线API: " + url);

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JsonNode root = objectMapper.readTree(responseString);

			System.out.println("打车路线响应状态: " + root.get("status").asText());

			if ("1".equals(root.get("status").asText()) && root.get("route").has("paths")
					&& root.get("route").get("paths").size() > 0) {
				JsonNode path = root.get("route").get("paths").get(0);

				int duration = (int) Math.ceil(path.get("duration").asDouble() / 60); // 秒转分钟
				int distance = path.get("distance").asInt(); // 米
				double cost = calculateTaxiCost(distance); // 使用打车费用计算

				// 考虑交通拥堵，增加20%的时间缓冲
				duration = (int) (duration * 1.2);

				return new RouteResult("taxi", duration, distance, cost);
			} else {
				System.err.println("打车路线规划失败: " + root.get("info").asText());
			}
		}
		return null;
	}

	/**
	 * 获取自驾路线
	 */
	private RouteResult getDrivingRoute(String fromLocation, String toLocation) throws Exception {
		String url = String.format("%s/direction/driving?key=%s&origin=%s&destination=%s&strategy=2", AMAP_BASE_URL,
				AMAP_API_KEY, fromLocation, toLocation);

		//System.out.println("请求自驾路线API: " + url);

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JsonNode root = objectMapper.readTree(responseString);

			System.out.println("自驾路线响应状态: " + root.get("status").asText());

			if ("1".equals(root.get("status").asText()) && root.get("route").has("paths")
					&& root.get("route").get("paths").size() > 0) {
				JsonNode path = root.get("route").get("paths").get(0);

				int duration = (int) Math.ceil(path.get("duration").asDouble() / 60); // 秒转分钟
				int distance = path.get("distance").asInt(); // 米
				double cost = calculateDrivingCost(distance); // 使用自驾费用计算

				// 考虑交通拥堵，增加20%的时间缓冲
				duration = (int) (duration * 1.2);

				return new RouteResult("driving", duration, distance, cost);
			} else {
				System.err.println("自驾路线规划失败: " + root.get("info").asText());
			}
		}
		return null;
	}

	/**
	 * 获取公交路线
	 */
	private RouteResult getBusRoute(String fromLocation, String toLocation) throws Exception {
		String url = String.format("%s/direction/transit/integrated?key=%s&origin=%s&destination=%s&city=深圳&strategy=3",
				AMAP_BASE_URL, AMAP_API_KEY, fromLocation, toLocation);

		try (CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

			HttpEntity entity = response.getEntity();
			String responseString = EntityUtils.toString(entity);
			JsonNode root = objectMapper.readTree(responseString);

			if ("1".equals(root.get("status").asText()) && root.get("route").has("transits")
					&& root.get("route").get("transits").size() > 0) {
				JsonNode transit = root.get("route").get("transits").get(0);

				int duration = (int) Math.ceil(transit.get("duration").asDouble() / 60); // 秒转分钟
				int distance = transit.get("distance").asInt(); // 米
				double cost = calculateBusCost(transit);

				return new RouteResult("bus", duration, distance, cost);
			}
		}
		return null;
	}

	/**
	 * 计算地铁费用（基于深圳地铁票价规则）
	 */
	private double calculateSubwayCost(int distance, JsonNode transit) {
		// 深圳地铁票价规则：起步价4公里2元，4-12公里1元/4公里，12-24公里1元/6公里，超过24公里1元/8公里
		double km = distance / 1000.0;
		double cost = 2.0; // 起步价

		if (km > 4) {
			if (km <= 12) {
				cost += Math.ceil((km - 4) / 4) * 1;
			} else if (km <= 24) {
				cost += 2 + Math.ceil((km - 12) / 6) * 1;
			} else {
				cost += 2 + 2 + Math.ceil((km - 24) / 8) * 1;
			}
		}

		// 检查是否有优惠信息
		if (transit != null && transit.has("cost") && transit.get("cost").has("transit_price")) {
			double apiCost = transit.get("cost").get("transit_price").asDouble();
			if (apiCost > 0) {
				cost = apiCost;
			}
		}

		return Math.min(cost, 15); // 最高15元封顶
	}

	/**
	 * 计算打车费用（基于深圳出租车计价规则）
	 */
	private double calculateTaxiCost(int distance) {
		double km = distance / 1000.0;
		double cost = 12.0; // 起步价12元（含3公里）

		if (km > 3) {
			if (km <= 12) {
				cost += (km - 3) * 2.6; // 2.6元/公里
			} else {
				cost += 9 * 2.6 + (km - 12) * 3.2; // 超过12公里3.2元/公里
			}
		}

		// 加上低速行驶费、燃油附加费等
		cost += 3.0;

		return Math.round(cost * 100.0) / 100.0;
	}

	/**
	 * 计算自驾费用（基于油费和停车费）
	 */
	private double calculateDrivingCost(int distance) {
		double km = distance / 1000.0;

		// 油费计算：假设油耗8L/100km，油价8元/L
		double fuelCost = km * 8 * 8 / 100; // 油费

		// 停车费估算：假设停车2小时，每小时5元
		double parkingCost = 10.0;

		// 高速费估算（如果有）
		double tollCost = calculateTollCost(km);

		double totalCost = fuelCost + parkingCost + tollCost;

		System.out.println(String.format("自驾费用明细: 油费=%.2f, 停车费=%.2f, 高速费=%.2f, 总计=%.2f", fuelCost, parkingCost,
				tollCost, totalCost));

		return Math.round(totalCost * 100.0) / 100.0;
	}

	/**
	 * 计算高速费用（简化估算）
	 */
	private double calculateTollCost(double distanceKm) {
		// 简化计算：假设0.5元/公里，但不超过30元
		double toll = distanceKm * 0.5;
		return Math.min(toll, 30.0);
	}

	/**
	 * 计算公交费用
	 */
	private double calculateBusCost(JsonNode transit) {
		// 深圳公交通常2-3元
		if (transit != null && transit.has("cost") && transit.get("cost").has("transit_price")) {
			return transit.get("cost").get("transit_price").asDouble();
		}
		return 2.5; // 默认值
	}

	/**
	 * 降级方案：当API调用失败时使用估算方法（包含自驾）
	 */
	private List<RouteResult> getFallbackRoutes(String fromStation, String toStation) {
		List<RouteResult> routes = new ArrayList<>();

		// 基于已知的地铁网络数据进行估算
		int subwayTime = estimateSubwayTime(fromStation, toStation);
		int distance = estimateDistance(fromStation, toStation);

		if (subwayTime > 0) {
			double subwayCost = calculateSubwayCost(distance, null);
			routes.add(new RouteResult("subway", subwayTime, distance, subwayCost));
			System.out.println("降级方案-地铁路线: " + subwayTime + "分钟, " + subwayCost + "元");
		}

		// 估算打车时间（通常是地铁时间的60-70%）
		int taxiTime = (int) (subwayTime * 0.65);
		double taxiCost = calculateTaxiCost(distance);
		routes.add(new RouteResult("taxi", taxiTime, distance, taxiCost));
		System.out.println("降级方案-打车路线: " + taxiTime + "分钟, " + taxiCost + "元");

		// 估算自驾时间（与打车时间相近）
		int drivingTime = taxiTime;
		double drivingCost = calculateDrivingCost(distance);
		routes.add(new RouteResult("driving", drivingTime, distance, drivingCost));
		System.out.println("降级方案-自驾路线: " + drivingTime + "分钟, " + drivingCost + "元");

		return routes;
	}

	/**
	 * 估算地铁时间（基于深圳地铁平均速度）
	 */
	private int estimateSubwayTime(String fromStation, String toStation) {
		// 简化的站间时间估算
		if ("红山".equals(fromStation) && "马安山".equals(toStation)) {
			return 58; // 分钟
		} else if ("福田".equals(fromStation) && "红山".equals(toStation)) {
			return 40; // 分钟
		}

		// 默认估算：深圳地铁平均旅行速度约35km/h，加上停站时间
		double estimatedDistance = estimateDistance(fromStation, toStation) / 1000.0;
		return (int) (estimatedDistance / 35 * 60 + 10); // 基础时间+10分钟缓冲
	}

	/**
	 * 估算距离
	 */
	private int estimateDistance(String fromStation, String toStation) {
		// 简化的距离估算
		if ("红山".equals(fromStation) && "马安山".equals(toStation)) {
			return 25000; // 25公里
		} else if ("福田".equals(fromStation) && "红山".equals(toStation)) {
			return 18000; // 18公里
		}
		return 20000; // 默认20公里
	}

	/**
	 * 风险评估逻辑（包含自驾方案考虑）
	 */
	private RiskAssessment assessRisk(long availableMinutes, List<RouteResult> routes, TripRecord previousTrip,
			TripRecord nextTrip) {
		RouteResult subwayRoute = routes.stream().filter(r -> "subway".equals(r.getMode())).findFirst()
				.orElse(new RouteResult("subway", Integer.MAX_VALUE, 0, 0));

		RouteResult taxiRoute = routes.stream().filter(r -> "taxi".equals(r.getMode())).findFirst()
				.orElse(new RouteResult("taxi", Integer.MAX_VALUE, 0, 0));

		RouteResult drivingRoute = routes.stream().filter(r -> "driving".equals(r.getMode())).findFirst()
				.orElse(new RouteResult("driving", Integer.MAX_VALUE, 0, 0));

		// 考虑额外的中转时间
		int subwayTransferTime = 15; // 分钟（出入站、换乘、等车）
		int taxiTransferTime = 5; // 分钟（等车、上下车）
		int drivingTransferTime = 8; // 分钟（取车、停车、步行）

		int totalSubwayTime = subwayRoute.getDuration() + subwayTransferTime;
		int totalTaxiTime = taxiRoute.getDuration() + taxiTransferTime;
		int totalDrivingTime = drivingRoute.getDuration() + drivingTransferTime;

		System.out.println("风险评估参数:");
		System.out.println("可用时间=" + availableMinutes + "分钟");
		System.out.println("地铁需=" + totalSubwayTime + "分钟");
		System.out.println("打车需=" + totalTaxiTime + "分钟");
		System.out.println("自驾需=" + totalDrivingTime + "分钟");

		// 检查是否有任何交通方式在时间上可行
		boolean subwayFeasible = availableMinutes >= totalSubwayTime;
		boolean taxiFeasible = availableMinutes >= totalTaxiTime;
		boolean drivingFeasible = availableMinutes >= totalDrivingTime;

		System.out.println("可行性: 地铁=" + subwayFeasible + ", 打车=" + taxiFeasible + ", 自驾=" + drivingFeasible);

		// 风险评估逻辑
		if (!subwayFeasible && !taxiFeasible && !drivingFeasible) {
			// 所有交通方式都不可行
			return new RiskAssessment(RiskAssessment.RiskLevel.IMPOSSIBLE, String.format("时间不足：可用%d分钟，最快交通方式需%d分钟",
					availableMinutes, Math.min(Math.min(totalSubwayTime, totalTaxiTime), totalDrivingTime)), false,
					0.95);
		} else if (subwayFeasible) {
			// 地铁可行，正常行程
			double timeMargin = (double) (availableMinutes - totalSubwayTime) / totalSubwayTime;
			if (timeMargin > 0.2) {
				return new RiskAssessment(RiskAssessment.RiskLevel.NORMAL, "行程时间充足，属于正常出行", true, 0.9);
			} else {
				return new RiskAssessment(RiskAssessment.RiskLevel.SUSPICIOUS,
						String.format("行程时间较为紧张(可用%d分钟，地铁需%d分钟)", availableMinutes, totalSubwayTime), true, 0.7);
			}
		} else {
			// 只能通过打车或自驾完成
			double feasibilityScore = calculatePremiumTransportFeasibility(availableMinutes, totalTaxiTime,
					totalDrivingTime, taxiRoute.getCost(), drivingRoute.getCost());

			if (feasibilityScore > 0.6) {
				return new RiskAssessment(RiskAssessment.RiskLevel.SUSPICIOUS,
						String.format("地铁不可行，但打车/自驾可完成(地铁需%d分钟，打车需%d分钟，自驾需%d分钟)", totalSubwayTime, totalTaxiTime,
								totalDrivingTime),
						true, 0.6);
			} else {
				return new RiskAssessment(RiskAssessment.RiskLevel.HIGH_RISK, "行程高度紧张，即使打车/自驾也勉强完成", false, 0.8);
			}
		}
	}

	/**
	 * 计算高价交通方式可行性
	 */
	private double calculatePremiumTransportFeasibility(long availableMinutes, int taxiTime, int drivingTime,
			double taxiCost, double drivingCost) {
		// 时间可行性
		double taxiTimeScore = availableMinutes >= taxiTime ? 1.0 : (double) availableMinutes / taxiTime;
		double drivingTimeScore = availableMinutes >= drivingTime ? 1.0 : (double) availableMinutes / drivingTime;

		// 成本合理性：费用越高，可行性越低
		double taxiCostScore = Math.max(0, 1 - (taxiCost / 80)); // 假设超过80元就不合理
		double drivingCostScore = Math.max(0, 1 - (drivingCost / 50)); // 自驾成本容忍度较高

		// 取最佳可行性分数
		double bestTimeScore = Math.max(taxiTimeScore, drivingTimeScore);
		double bestCostScore = Math.max(taxiCostScore, drivingCostScore);

		double feasibility = (bestTimeScore * 0.6 + bestCostScore * 0.4);

		System.out.println("高价交通可行性分析:");
		System.out.println("打车: 时间分数=" + taxiTimeScore + ", 成本分数=" + taxiCostScore);
		System.out.println("自驾: 时间分数=" + drivingTimeScore + ", 成本分数=" + drivingCostScore);
		System.out.println("综合可行性=" + feasibility);

		return feasibility;
	}

	/**
	 * 测试方法
	 */
	public static void main(String[] args) {
		MetroTripAllValidator validator = new MetroTripAllValidator();

		// 创建测试数据
		TripRecord previousTrip = new TripRecord("沙田", LocalDateTime.of(2025, 10, 13, 11, 40), "珠光",
				LocalDateTime.of(2025, 10, 13, 17, 10));

		TripRecord nextTrip = new TripRecord("科学馆", LocalDateTime.of(2025, 10, 13, 18, 15), null, null);

		System.out.println("开始验证行程合理性（包含自驾方案）...");
		RiskAssessment result = validator.validateTripFeasibility(previousTrip, nextTrip);

		// 输出结果
		System.out.println("\n=== 地铁行程风险评估结果 ===");
		System.out.println("风险等级: " + result.getRiskLevel());
		System.out.println("原因: " + result.getReason());
		System.out.println("是否允许通行: " + result.isAllowTravel());
		System.out.println("置信度: " + String.format("%.2f", result.getConfidence()));
	}
}
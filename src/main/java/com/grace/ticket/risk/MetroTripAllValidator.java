package com.grace.ticket.risk;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.grace.ticket.config.Constants;
import com.grace.ticket.dto.TicketInfoDTO;

@Component
public class MetroTripAllValidator {

    // 高德地图API配置 - 使用您提供的API Key
    private static final String AMAP_API_KEY = "dbf17a822ded817a149e45cb535f953a";
    private static final String AMAP_BASE_URL = "https://restapi.amap.com/v3";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 时间常量配置
    private static final int DEFAULT_WALK_TIME = 10; // 默认步行时间（分钟）
    private static final int DEFAULT_WAIT_TIME = 5;  // 默认候车时间（分钟）
    private static final int TRANSFER_WALK_TIME = 8; // 换乘步行时间（分钟）
    private static final int TAXI_SPEED = 30;        // 出租车平均速度 km/h

    // 模拟地铁站间距离数据（公里）
    private final Map<String, Map<String, Integer>> stationDistances = new HashMap<>();

    /**
     * 验证票卡行程 - 主要业务方法
     */
    public List<TicketInfoDTO> validateTicketTrips(String boardingStation, LocalDateTime boardingTime, 
                                                   List<TicketInfoDTO> tickets) {
        List<TicketInfoDTO> validatedTickets = new ArrayList<>();
        
        for (TicketInfoDTO ticket : tickets) {
            TicketInfoDTO validatedTicket = validateSingleTicketTrip(boardingStation, boardingTime, ticket);
            validatedTickets.add(validatedTicket);
        }
        
        return validatedTickets;
    }
    
    
    /**
     * 验证票卡行程 - 主要业务方法
     */
    public List<TicketInfoDTO> validateTicketTripsForVIP(String boardingStation, LocalDateTime boardingTime, 
                                                   List<TicketInfoDTO> tickets) {
        List<TicketInfoDTO> validatedTickets = new ArrayList<>();
        
        for (TicketInfoDTO ticket : tickets) {
            TicketInfoDTO validatedTicket = validateSingleTicketTrip(boardingStation, boardingTime, ticket);
            
            //获取到第一条就返回
            if(Constants.GREEN_LIGHT.equals(validatedTicket.getGreeLight())) {
            	validatedTickets.add(validatedTicket);
            	return validatedTickets;
            }
            
        }
        
        return validatedTickets;
    }

    /**
     * 验证单个票卡行程
     */
    private TicketInfoDTO validateSingleTicketTrip(String boardingStation, LocalDateTime boardingTime, 
                                                   TicketInfoDTO ticket) {
        String alightingStation = ticket.getAlightingStation();
        LocalDateTime alightingTime = ticket.getAlightingTime();
         
        // 计算行程时间
        TravelTimeResult timeResult = calculateTravelTimes(boardingStation, boardingTime, 
                                                          alightingStation, alightingTime);
        
        Duration duration = Duration.between(alightingTime, boardingTime);
        // 更新票卡信息
        ticket.setBoardingStation(boardingStation);
        ticket.setBoardingTime(boardingTime);
        ticket.setSubwayTravelTime(timeResult.getSubwayTravelTime());
        ticket.setSubwayWalkTime(timeResult.getSubwayWithWalkTime());
        ticket.setSubwayWaitTime(timeResult.getSubwayWithWalkAndWaitTime());
        ticket.setTaxiTime(timeResult.getTaxiTime());
        ticket.setTimeInterval(Integer.valueOf(""+ duration.toMinutes()));
        ticket.setTravelSuggestion(timeResult.getSuggestion());
        if(timeResult.isFeasible()) ticket.setGreeLight(Constants.GREEN_LIGHT);
        
        return ticket;
    }

    /**
     * 计算单次行程的各类时间
     */
    public TravelTimeResult calculateTravelTimes(String boardingStation, LocalDateTime boardingTime,
                                                String alightingStation, LocalDateTime alightingTime) {
        TravelTimeResult result = new TravelTimeResult();
        
        try {
            // 获取各种交通方式的路线规划 
            List<RouteResult> routes = calculateAllRoutes(boardingStation, alightingStation);
            
            // 提取地铁和打车路线
            RouteResult subwayRoute = routes.stream().filter(r -> "subway".equals(r.getMode())).findFirst()
                    .orElse(null);
            RouteResult taxiRoute = routes.stream().filter(r -> "taxi".equals(r.getMode())).findFirst()
                    .orElse(null);
            
            // 计算地铁相关时间
            if (subwayRoute != null) {
                // 纯地铁运行时间
                result.setSubwayTravelTime(subwayRoute.getDuration()); 
                
                // 地铁+步行时间
                int subwayWithWalkTime = subwayRoute.getDuration() + calculateWalkTime(subwayRoute);
                result.setSubwayWithWalkTime(subwayWithWalkTime);
                
                // 地铁+步行+候车时间
                int subwayWithWalkAndWaitTime = subwayWithWalkTime + DEFAULT_WAIT_TIME;
                result.setSubwayWithWalkAndWaitTime(subwayWithWalkAndWaitTime);
                
                // 设置地铁距离和费用
                result.setSubwayDistance(subwayRoute.getDistance());
                result.setSubwayCost(subwayRoute.getCost());
            } else {
                // 使用降级方案计算地铁时间
                int subwayTime = calculateSubwayTime(boardingStation, alightingStation);
                result.setSubwayTravelTime(subwayTime);
                result.setSubwayWithWalkTime(subwayTime + DEFAULT_WALK_TIME);
                result.setSubwayWithWalkAndWaitTime(subwayTime + DEFAULT_WALK_TIME + DEFAULT_WAIT_TIME);
            }
            
            // 计算打车时间
            if (taxiRoute != null) {
                result.setTaxiTime(taxiRoute.getDuration());
                result.setTaxiDistance(taxiRoute.getDistance());
                result.setTaxiCost(taxiRoute.getCost());
            } else {
                // 使用降级方案计算打车时间
                int taxiTime = calculateTaxiTime(boardingStation, alightingStation);
                result.setTaxiTime(taxiTime);
            }
            
            // 验证行程可能性并生成建议
            validateAndSetSuggestion(result, alightingTime, boardingTime);
            
        } catch (Exception e) {
            System.err.println("计算行程时间失败: " + e.getMessage());
            // 使用降级方案
            applyFallbackCalculation(result, boardingStation, alightingStation, boardingTime, alightingTime);
        }
        
        return result;
    }

    /**
     * 使用高德地图API计算所有可能的路线（包含自驾方案）
     */
    public List<RouteResult> calculateAllRoutes(String fromStation, String toStation) {
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
                /*
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
                */
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
    public String getStationLocation(String stationName) throws Exception {
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
    private RouteResult getSubwayRoute2(String fromLocation, String toLocation) throws Exception {
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

                return new RouteResult("subway", duration,0, distance, cost);
            } else {
                System.err.println("地铁路线规划失败: " + root.get("info").asText());
            }
        }
        return null;
    }
    
    public RouteResult getSubwayRoute(String fromLocation, String toLocation) throws Exception {
        String url = String.format("%s/direction/transit/integrated?key=%s&origin=%s&destination=%s&city=深圳&strategy=0",
                AMAP_BASE_URL, AMAP_API_KEY, fromLocation, toLocation);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(new HttpGet(url))) {

            HttpEntity entity = response.getEntity();
            String responseString = EntityUtils.toString(entity);
            JsonNode root = objectMapper.readTree(responseString);

            if ("1".equals(root.get("status").asText()) && root.get("route").has("transits")
                    && root.get("route").get("transits").size() > 0) {
                JsonNode transit = root.get("route").get("transits").get(0);

                int totalDuration = (int) Math.ceil(transit.get("duration").asDouble() / 60); // 总时间（分钟）
                int pureSubwayTime = (int) Math.ceil(getPureSubwayTime(transit) / 60.0); // 纯地铁时间（分钟）
                int distance = transit.get("distance").asInt();
                double cost = calculateSubwayCost(distance, transit);

                // 可以返回纯地铁时间或同时返回两种时间
                return new RouteResult("subway", totalDuration, pureSubwayTime, distance, cost);
            }
        }
        return null;
    }
    
    private int getPureSubwayTime(JsonNode transit) {
        int pureSubwayTime = 0; // 单位：秒
        
        if (transit.has("segments")) {
            JsonNode segments = transit.get("segments");
            
            for (JsonNode segment : segments) {
                // 检查是否是地铁分段
                if (segment.has("railway")) {
                    JsonNode railway = segment.get("railway");
                    if (railway.has("time")) {
                        pureSubwayTime += railway.get("time").asInt();
                    }
                    // 或者使用地铁分段的duration
                    else if (railway.has("duration")) {
                        pureSubwayTime += railway.get("duration").asInt();
                    }
                }
            }
        }
        
        return pureSubwayTime; // 返回秒数
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


            if ("1".equals(root.get("status").asText()) && root.get("route").has("paths")
                    && root.get("route").get("paths").size() > 0) {
                JsonNode path = root.get("route").get("paths").get(0);

                int duration = (int) Math.ceil(path.get("duration").asDouble() / 60); // 秒转分钟
                int distance = path.get("distance").asInt(); // 米
                double cost = calculateTaxiCost(distance); // 使用打车费用计算

                // 考虑交通拥堵，增加20%的时间缓冲
                duration = (int) (duration * 1.2);

                return new RouteResult("taxi", duration,0, distance, cost);
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


            if ("1".equals(root.get("status").asText()) && root.get("route").has("paths")
                    && root.get("route").get("paths").size() > 0) {
                JsonNode path = root.get("route").get("paths").get(0);

                int duration = (int) Math.ceil(path.get("duration").asDouble() / 60); // 秒转分钟
                int distance = path.get("distance").asInt(); // 米
                double cost = calculateDrivingCost(distance); // 使用自驾费用计算

                // 考虑交通拥堵，增加20%的时间缓冲
                duration = (int) (duration * 1.2);

                return new RouteResult("driving", duration,0, distance, cost);
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

                return new RouteResult("bus", duration, 0,distance, cost);
            }
        }
        return null;
    }

    /**
     * 计算步行时间（基于路线复杂度和换乘次数）
     */
    private int calculateWalkTime(RouteResult subwayRoute) {
        int baseWalkTime = DEFAULT_WALK_TIME;
        double distanceKm = subwayRoute.getDistance() / 1000.0;
        if (distanceKm > 2) {
            baseWalkTime += (int) ((distanceKm - 2) / 5 * 60);
        }
        return Math.min(baseWalkTime, 30);
    }

    /**
     * 验证行程可能性并设置建议
     */
    private void validateAndSetSuggestion(TravelTimeResult result, LocalDateTime boardingTime, 
                                         LocalDateTime alightingTime) {
        if (boardingTime == null || alightingTime == null) {
            result.setSuggestion("时间信息不完整");
            return;
        }
        
        long availableMinutes = Duration.between(boardingTime, alightingTime).toMinutes();
        
        if (availableMinutes <= 0) {
            result.setSuggestion("下车时间必须晚于上车时间");
            return;
        }
        
        boolean subwayPossible = availableMinutes >= result.getSubwayTravelTime();
        boolean taxiPossible = availableMinutes >= result.getTaxiTime();
        
        if (subwayPossible && taxiPossible) {
            if (result.getSubwayTravelTime() < result.getTaxiTime()) {
                int timeSaved = result.getTaxiTime() - result.getSubwayTravelTime();
                result.setSuggestion(String.format("地铁时间：%d , 打车时间：%d 建议乘坐地铁", result.getSubwayTravelTime(), result.getTaxiTime()));
            } else {
            	result.setSuggestion(String.format("地铁时间：%d , 打车时间：%d 建议乘坐打车", result.getSubwayTravelTime(), result.getTaxiTime()));
            }
        } else if (subwayPossible) {
            result.setSuggestion(String.format("建议乘坐地铁，时间充足，费用%.1f元", result.getSubwayCost()));
        } else if (taxiPossible) {
            result.setSuggestion(String.format("建议打车，地铁时间不足，费用%.1f元", result.getTaxiCost()));
        } else {
            result.setSuggestion("时间不足，建议调整行程");
        }
        
        result.setFeasible(subwayPossible || taxiPossible);
    }

    /**
     * 降级方案计算
     */
    private void applyFallbackCalculation(TravelTimeResult result, String boardingStation, 
                                        String alightingStation, LocalDateTime boardingTime,
                                        LocalDateTime alightingTime) {
        int subwayTime = calculateSubwayTime(boardingStation, alightingStation);
        result.setSubwayTravelTime(subwayTime);
        result.setSubwayWithWalkTime(subwayTime + DEFAULT_WALK_TIME);
        result.setSubwayWithWalkAndWaitTime(subwayTime + DEFAULT_WALK_TIME + DEFAULT_WAIT_TIME);
        
        int taxiTime = calculateTaxiTime(boardingStation, alightingStation);
        result.setTaxiTime(taxiTime);
        
        int distance = calculateDistance(boardingStation, alightingStation);
        result.setSubwayDistance(distance);
        result.setTaxiDistance(distance);
        result.setSubwayCost(calculateSubwayCost(distance, null));
        result.setTaxiCost(calculateTaxiCost(distance));
        
        validateAndSetSuggestion(result, boardingTime, alightingTime);
    }

    /**
     * 计算地铁时间（基于站间距离）
     */
    private int calculateSubwayTime(String fromStation, String toStation) {
        if (fromStation.equals(toStation)) {
            return 0;
        }
        
        Integer distance = getStationDistance(fromStation, toStation);
        // 假设地铁平均速度 40km/h
        return (int) Math.round(distance / 40.0 * 60);
    }

    /**
     * 计算打车时间（基于站间距离）
     */
    private int calculateTaxiTime(String fromStation, String toStation) {
        Integer distance = getStationDistance(fromStation, toStation);
        // 考虑交通状况，实际时间可能比理论时间长
        return (int) Math.round(distance / TAXI_SPEED * 60 * 1.2);
    }

    /**
     * 计算站间距离
     */
    private int calculateDistance(String fromStation, String toStation) {
        return getStationDistance(fromStation, toStation);
    }

    /**
     * 获取站间距离
     */
    private Integer getStationDistance(String fromStation, String toStation) {
        initializeStationData();
        return stationDistances.getOrDefault(fromStation, new HashMap<>())
                .getOrDefault(toStation, 10);
    }

    /**
     * 初始化站点数据
     */
    private void initializeStationData() {
        if (!stationDistances.isEmpty()) {
            return;
        }
        
        String[] stations = {"站A", "站B", "站C", "站D", "站E", "福田", "红山", "马安山", "科学馆", "沙田", "珠光"};
        
        for (String from : stations) {
            Map<String, Integer> distances = new HashMap<>();
            for (String to : stations) {
                if (from.equals(to)) {
                    distances.put(to, 0);
                } else {
                    // 模拟距离，实际应该根据真实数据
                    int baseDistance = Math.abs(from.hashCode() - to.hashCode()) % 20 + 5;
                    distances.put(to, baseDistance);
                }
            }
            stationDistances.put(from, distances);
        }
        
        // 设置特定站点间的距离
        stationDistances.get("红山").put("马安山", 25);
        stationDistances.get("马安山").put("红山", 25);
        stationDistances.get("福田").put("红山", 18);
        stationDistances.get("红山").put("福田", 18);
        stationDistances.get("科学馆").put("福田", 8);
        stationDistances.get("福田").put("科学馆", 8);
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

        //System.out.println(String.format("自驾费用明细: 油费=%.2f, 停车费=%.2f, 高速费=%.2f, 总计=%.2f", fuelCost, parkingCost, tollCost, totalCost));

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
            routes.add(new RouteResult("subway", subwayTime,0, distance, subwayCost));
            System.out.println("降级方案-地铁路线: " + subwayTime + "分钟, " + subwayCost + "元");
        }

        // 估算打车时间（通常是地铁时间的60-70%）
        int taxiTime = (int) (subwayTime * 0.65);
        double taxiCost = calculateTaxiCost(distance);
        routes.add(new RouteResult("taxi", taxiTime, 0,distance, taxiCost));
        System.out.println("降级方案-打车路线: " + taxiTime + "分钟, " + taxiCost + "元");

        // 估算自驾时间（与打车时间相近）
        int drivingTime = taxiTime;
        double drivingCost = calculateDrivingCost(distance);
        routes.add(new RouteResult("driving", drivingTime,0, distance, drivingCost));
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
     * 风险评估逻辑（包含自驾方案考虑）
     */
    private RiskAssessment assessRisk(long availableMinutes, List<RouteResult> routes, TripRecord previousTrip,
            TripRecord nextTrip) {
        RouteResult subwayRoute = routes.stream().filter(r -> "subway".equals(r.getMode())).findFirst()
                .orElse(new RouteResult("subway", Integer.MAX_VALUE,0, 0, 0));

        RouteResult taxiRoute = routes.stream().filter(r -> "taxi".equals(r.getMode())).findFirst()
                .orElse(new RouteResult("taxi", Integer.MAX_VALUE,0, 0, 0));

        RouteResult drivingRoute = routes.stream().filter(r -> "driving".equals(r.getMode())).findFirst()
                .orElse(new RouteResult("driving", Integer.MAX_VALUE,0, 0, 0));

        // 考虑额外的中转时间
        int subwayTransferTime = 0; // 分钟（出入站、换乘、等车）
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
     * 获取可用站点列表
     */
    public List<String> getAvailableStations() {
        initializeStationData();
        return new ArrayList<>(stationDistances.keySet());
    }

    // 内部辅助类
    public static class TravelTimeResult {
        private int subwayTravelTime;
        private int subwayWithWalkTime;
        private int subwayWithWalkAndWaitTime;
        private int taxiTime;
        private int subwayDistance;
        private int taxiDistance;
        private double subwayCost;
        private double taxiCost;
        private String suggestion;
        private boolean feasible;

        // Getter and Setter methods
        public int getSubwayTravelTime() { return subwayTravelTime; }
        public void setSubwayTravelTime(int subwayTravelTime) { this.subwayTravelTime = subwayTravelTime; }
        public int getSubwayWithWalkTime() { return subwayWithWalkTime; }
        public void setSubwayWithWalkTime(int subwayWithWalkTime) { this.subwayWithWalkTime = subwayWithWalkTime; }
        public int getSubwayWithWalkAndWaitTime() { return subwayWithWalkAndWaitTime; }
        public void setSubwayWithWalkAndWaitTime(int subwayWithWalkAndWaitTime) { this.subwayWithWalkAndWaitTime = subwayWithWalkAndWaitTime; }
        public int getTaxiTime() { return taxiTime; }
        public void setTaxiTime(int taxiTime) { this.taxiTime = taxiTime; }
        public int getSubwayDistance() { return subwayDistance; }
        public void setSubwayDistance(int subwayDistance) { this.subwayDistance = subwayDistance; }
        public int getTaxiDistance() { return taxiDistance; }
        public void setTaxiDistance(int taxiDistance) { this.taxiDistance = taxiDistance; }
        public double getSubwayCost() { return subwayCost; }
        public void setSubwayCost(double subwayCost) { this.subwayCost = subwayCost; }
        public double getTaxiCost() { return taxiCost; }
        public void setTaxiCost(double taxiCost) { this.taxiCost = taxiCost; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
        public boolean isFeasible() { return feasible; }
        public void setFeasible(boolean feasible) { this.feasible = feasible; }
    }

}
package com.grace.ticket.risk;


/**
 * 行程时间计算结果
 */
public class TravelTimeResult {
    private int subwayTravelTime;           // 地铁运行时间（分钟）
    private int subwayWithWalkTime;         // 地铁+步行时间（分钟）
    private int subwayWithWalkAndWaitTime;  // 地铁+步行+候车时间（分钟）
    private int taxiTime;                   // 打车时间（分钟）
    private int subwayDistance;             // 地铁距离（米）
    private int taxiDistance;               // 打车距离（米）
    private double subwayCost;              // 地铁费用（元）
    private double taxiCost;                // 打车费用（元）
    private String suggestion;              // 通行建议
    private boolean feasible;               // 是否可行

    // 构造函数
    public TravelTimeResult() {}

    // Getter 和 Setter 方法
    public int getSubwayTravelTime() {
        return subwayTravelTime;
    }

    public void setSubwayTravelTime(int subwayTravelTime) {
        this.subwayTravelTime = subwayTravelTime;
    }

    public int getSubwayWithWalkTime() {
        return subwayWithWalkTime;
    }

    public void setSubwayWithWalkTime(int subwayWithWalkTime) {
        this.subwayWithWalkTime = subwayWithWalkTime;
    }

    public int getSubwayWithWalkAndWaitTime() {
        return subwayWithWalkAndWaitTime;
    }

    public void setSubwayWithWalkAndWaitTime(int subwayWithWalkAndWaitTime) {
        this.subwayWithWalkAndWaitTime = subwayWithWalkAndWaitTime;
    }

    public int getTaxiTime() {
        return taxiTime;
    }

    public void setTaxiTime(int taxiTime) {
        this.taxiTime = taxiTime;
    }

    public int getSubwayDistance() {
        return subwayDistance;
    }

    public void setSubwayDistance(int subwayDistance) {
        this.subwayDistance = subwayDistance;
    }

    public int getTaxiDistance() {
        return taxiDistance;
    }

    public void setTaxiDistance(int taxiDistance) {
        this.taxiDistance = taxiDistance;
    }

    public double getSubwayCost() {
        return subwayCost;
    }

    public void setSubwayCost(double subwayCost) {
        this.subwayCost = subwayCost;
    }

    public double getTaxiCost() {
        return taxiCost;
    }

    public void setTaxiCost(double taxiCost) {
        this.taxiCost = taxiCost;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public void setFeasible(boolean feasible) {
        this.feasible = feasible;
    }

    @Override
    public String toString() {
        return String.format(
            "TravelTimeResult{地铁运行=%d分钟, 地铁+步行=%d分钟, 地铁+步行+候车=%d分钟, 打车=%d分钟, 建议=%s}",
            subwayTravelTime, subwayWithWalkTime, subwayWithWalkAndWaitTime, taxiTime, suggestion
        );
    }
}
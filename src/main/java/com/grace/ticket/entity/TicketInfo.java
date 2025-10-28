package com.grace.ticket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_info")
public class TicketInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_number")
    private String ticketNumber;
    
    @Column(name = "boarding_station")
    private String boardingStation;
    
    @Column(name = "boarding_time")
    private LocalDateTime boardingTime;
    
    @Column(name = "alighting_station")
    private String alightingStation;
    
    @Column(name = "alighting_time")
    private LocalDateTime alightingTime;
    
    @Column(name = "subway_travel_time")
    private Integer subwayTravelTime; // 地铁运行时间
    
    @Column(name = "subway_walk_time")
    private Integer subwayWalkTime;   // 地铁+步行时间
    
    @Column(name = "subway_wait_time")
    private Integer subwayWaitTime;   // 地铁+步行+候车时间
    
    @Column(name = "taxi_time")
    private Integer taxiTime;         // 打车时间
    
    @Column(name = "travel_suggestion")
    private String travelSuggestion;  // 通行建议

    // 构造函数
    public TicketInfo() {}

    public TicketInfo(String ticketNumber, String alightingStation, LocalDateTime alightingTime) {
        this.ticketNumber = ticketNumber;
        this.alightingStation = alightingStation;
        this.alightingTime = alightingTime;
    }

    // Getter 和 Setter 方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getBoardingStation() { return boardingStation; }
    public void setBoardingStation(String boardingStation) { this.boardingStation = boardingStation; }

    public LocalDateTime getBoardingTime() { return boardingTime; }
    public void setBoardingTime(LocalDateTime boardingTime) { this.boardingTime = boardingTime; }

    public String getAlightingStation() { return alightingStation; }
    public void setAlightingStation(String alightingStation) { this.alightingStation = alightingStation; }

    public LocalDateTime getAlightingTime() { return alightingTime; }
    public void setAlightingTime(LocalDateTime alightingTime) { this.alightingTime = alightingTime; }

    public Integer getSubwayTravelTime() { return subwayTravelTime; }
    public void setSubwayTravelTime(Integer subwayTravelTime) { this.subwayTravelTime = subwayTravelTime; }

    public Integer getSubwayWalkTime() { return subwayWalkTime; }
    public void setSubwayWalkTime(Integer subwayWalkTime) { this.subwayWalkTime = subwayWalkTime; }

    public Integer getSubwayWaitTime() { return subwayWaitTime; }
    public void setSubwayWaitTime(Integer subwayWaitTime) { this.subwayWaitTime = subwayWaitTime; }

    public Integer getTaxiTime() { return taxiTime; }
    public void setTaxiTime(Integer taxiTime) { this.taxiTime = taxiTime; }

    public String getTravelSuggestion() { return travelSuggestion; }
    public void setTravelSuggestion(String travelSuggestion) { this.travelSuggestion = travelSuggestion; }

    @Override
    public String toString() {
        return "TicketInfo{" +
                "id=" + id +
                ", ticketNumber='" + ticketNumber + '\'' +
                ", boardingStation='" + boardingStation + '\'' +
                ", boardingTime=" + boardingTime +
                ", alightingStation='" + alightingStation + '\'' +
                ", alightingTime=" + alightingTime +
                ", subwayTravelTime=" + subwayTravelTime +
                ", subwayWalkTime=" + subwayWalkTime +
                ", subwayWaitTime=" + subwayWaitTime +
                ", taxiTime=" + taxiTime +
                ", travelSuggestion='" + travelSuggestion + '\'' +
                '}';
    }
}
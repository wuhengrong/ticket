package com.grace.ticket.config;

public class Constants {
	public static final String DOMAIN = "https://ticket-1-6sz7.onrender.com";
    //public static final String DOMAIN = "http://localhost:8080";
    
	
	public static final String BASE_URL = "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html";
	//public static final String USER_URL_TEMPLATE = "https://unconcatenating-ken-unimaginably.ngrok-free.dev/mobile/ticket/count-card-link.html?code=";
	public static final String USER_URL_TEMPLATE = "https://ticket-1-6sz7.onrender.com/ticket/count-card-link.html?code=";    
	public static final String MANAGEMENT_ACCESS_PASSWORD="Edwin*2015";
    //初始化时当前时间大于如下设置，则初始化为第二天
    public static final int RESET_HOUR=21;
    public static final int RESET_MIN=30;
}

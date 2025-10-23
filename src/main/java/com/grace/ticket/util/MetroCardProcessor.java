package com.grace.ticket.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MetroCardProcessor {
    
    // 第一步：获取可用状态
    public static String getStatusFromUrl(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        Element statusElement = doc.selectFirst("div.flex.items-center i.fa-clock").parent();
        return statusElement.text().trim();
    }
    
    // 第二步：遍历URL数组，找到count > 0的URL
    public static List<String> findValidUrls(String[] urls) throws IOException {
        List<String> validUrls = new ArrayList<>();
        
        for (String url : urls) {
            try {
                Document doc = Jsoup.connect(url).get();
                Element countElement = doc.selectFirst("span#card-count");
                
                if (countElement != null) {
                    String countText = countElement.text().trim();
                    int count = Integer.parseInt(countText);
                    
                    if (count > 0) {
                        validUrls.add(url);
                    }
                }
            } catch (Exception e) {
                System.err.println("处理URL时出错: " + url + ", 错误: " + e.getMessage());
            }
        }
        
        return validUrls;
    }
    
    // 生成HTML页面
    public static String generateHtmlPage(String redirectUrl) {
        return """
            <!DOCTYPE html>
            <html lang="zh-CN">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>地铁乘车服务</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .section {
                        background: white;
                        border-radius: 10px;
                        padding: 20px;
                        margin-bottom: 20px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .wechat-section {
                        text-align: center;
                        background: linear-gradient(135deg, #07c160, #05a050);
                        color: white;
                    }
                    .wechat-id {
                        font-size: 24px;
                        font-weight: bold;
                        margin: 10px 0;
                    }
                    .rules-section h3 {
                        color: #333;
                        border-bottom: 2px solid #07c160;
                        padding-bottom: 10px;
                    }
                    .rules-list {
                        list-style-type: none;
                        padding: 0;
                    }
                    .rules-list li {
                        padding: 8px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .rules-list li:last-child {
                        border-bottom: none;
                    }
                    .ride-button {
                        display: block;
                        width: 100%;
                        padding: 15px;
                        background: linear-gradient(135deg, #ff6b35, #ff8c42);
                        color: white;
                        border: none;
                        border-radius: 25px;
                        font-size: 18px;
                        font-weight: bold;
                        cursor: pointer;
                        text-decoration: none;
                        text-align: center;
                        transition: all 0.3s ease;
                    }
                    .ride-button:hover {
                        background: linear-gradient(135deg, #e55a2e, #e57a35);
                        transform: translateY(-2px);
                        box-shadow: 0 5px 15px rgba(255, 107, 53, 0.4);
                    }
                </style>
            </head>
            <body>
                <!-- 第一部分：微信联系方式 -->
                <div class="section wechat-section">
                    <h2>请联系客服微信</h2>
                    <div class="wechat-id">whengrong</div>
                    <p>如有问题请添加微信咨询</p>
                </div>
                
                <!-- 第二部分：使用规则 -->
                <div class="section rules-section">
                    <h3>使用规则：</h3>
                    <ul class="rules-list">
                        <li>a. 单次乘车不限里程，乘车时长最长为150分钟，超出时间乘车码将自动失效，届时将无法出站；</li>
                        <li>b. 电子旅游票仅限用于乘坐市内地铁普通车厢，请勿乘坐商务车厢；</li>
                        <li>c. 请妥善保管不记名次卡，遵守一票一行、一票一用原则，请勿与他人共享；</li>
                        <li>d. 由于网络卡顿导致闸机显示无效票，请刷新页面或二维码后重试；</li>
                    </ul>
                </div>
                
                <!-- 第三部分：乘车按钮 -->
                <div class="section">
                    <a href="%s" class="ride-button" id="ride-button">
                        点击乘车
                    </a>
                </div>
                
                <script>
                    document.getElementById('ride-button').addEventListener('click', function(e) {
                        if (!confirm('确定要开始乘车吗？')) {
                            e.preventDefault();
                        }
                    });
                </script>
            </body>
            </html>
            """.formatted(redirectUrl);
    }
    
    public static void main(String[] args) {
        try {
            // 第一步：获取状态
            String targetUrl = "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=KEVTATSFIX19HAFA";
            String status = getStatusFromUrl(targetUrl);
            System.out.println("可用状态: " + status);
            
            // 第二步：示例URL数组（请替换为实际的URL数组）
            String[] urls = {
                "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=KEVTATSFIX19HAFA",
                "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=5IVFVTROWKKXUJAP",
                "http://example.com/url3"
                // 添加更多URL...
            };
            
            List<String> validUrls = findValidUrls(urls);
            
            if (!validUrls.isEmpty()) {
                String redirectUrl = validUrls.get(0); // 使用第一个有效的URL
                
                // 第三步：生成HTML页面
                String htmlPage = generateHtmlPage(redirectUrl);
                
                // 可以将HTML保存到文件或直接返回
                System.out.println("HTML页面已生成，重定向URL: " + redirectUrl);
                System.out.println("HTML内容长度: " + htmlPage.length() + " 字符");
                
                // 保存到文件（可选）
                // Files.write(Paths.get("metro_card_page.html"), htmlPage.getBytes());
            } else {
                System.out.println("没有找到有效的URL");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
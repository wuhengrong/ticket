package com.grace.ticket.util;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RelaxedSecurityMetroCardProcessor {
    
    // 获取状态信息
    public static String getStatusWithRelaxedSecurity(String url) {
        WebDriver driver = null;
        try {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            
            // 宽松的安全设置
            options.addArguments("--ignore-certificate-errors-spki-list");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--ignore-ssl-errors");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--disable-features=VizDisplayCompositor");
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            
            // 实验性选项
            options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
            options.setExperimentalOption("useAutomationExtension", false);
            
            driver = new ChromeDriver(options);
            
            // 隐藏webdriver属性
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
            
            System.out.println("正在访问URL: " + url);
            driver.get(url);
            
            // 给页面足够时间加载（包括可能的安全警告和动态内容）
            Thread.sleep(10000);
            
            // 打印调试信息
            System.out.println("页面标题: " + driver.getTitle());
            System.out.println("当前URL: " + driver.getCurrentUrl());
            
            // 方法1: 直接搜索页面文本
            String pageText = driver.findElement(By.tagName("body")).getText();
            System.out.println("页面完整文本长度: " + pageText.length());
            
            // 在文本中搜索状态信息
            if (pageText.contains("可用状态")) {
                int startIndex = pageText.indexOf("可用状态");
                int endIndex = Math.min(startIndex + 50, pageText.length());
                String status = pageText.substring(startIndex, endIndex).trim();
                System.out.println("从页面文本中提取的状态: " + status);
                return status;
            }
            
            // 方法2: 使用多种XPath选择器尝试定位元素
            String status = findStatusWithMultipleSelectors(driver);
            if (status != null) {
                return status;
            }
            
            // 方法3: 如果还是找不到，尝试执行JavaScript来获取动态内容
            return findStatusWithJavaScript(driver);
            
        } catch (Exception e) {
            System.err.println("获取状态失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    
    private static String findStatusWithMultipleSelectors(WebDriver driver) {
        String[] xpathSelectors = {
            "//*[contains(text(), '可用状态')]",
            "//*[contains(text(), '随时可用')]",
            "//*[contains(., '可用状态')]",
            "//div[contains(text(), '可用状态')]",
            "//span[contains(text(), '可用状态')]",
            "//p[contains(text(), '可用状态')]",
            "//*[@id='node-list']//*[contains(text(), '可用状态')]",
            "//div[contains(@class, 'flex')]",
            "//div[contains(@class, 'items-center')]",
            "//i[contains(@class, 'fa-clock')]",
            "//i[contains(@class, 'clock')]"
        };
        
        for (String xpath : xpathSelectors) {
            try {
                List<WebElement> elements = driver.findElements(By.xpath(xpath));
                if (!elements.isEmpty()) {
                    for (WebElement element : elements) {
                        String text = element.getText().trim();
                        if (!text.isEmpty() && (text.contains("可用状态") || text.contains("随时可用"))) {
                            System.out.println("使用XPath找到元素: " + xpath);
                            System.out.println("元素文本: " + text);
                            System.out.println("元素HTML: " + element.getAttribute("outerHTML"));
                            return text;
                        }
                    }
                }
            } catch (Exception e) {
                // 忽略选择器错误，继续尝试下一个
            }
        }
        
        return null;
    }
    
    private static String findStatusWithJavaScript(WebDriver driver) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 尝试通过JavaScript查找包含特定文本的元素
            String script = """
                var elements = document.querySelectorAll('*');
                var result = [];
                for (var i = 0; i < elements.length; i++) {
                    var text = elements[i].textContent || elements[i].innerText;
                    if (text && text.includes('可用状态')) {
                        result.push({
                            tag: elements[i].tagName,
                            text: text.trim(),
                            html: elements[i].outerHTML
                        });
                    }
                }
                return result;
                """;
            
            @SuppressWarnings("unchecked")
            List<Object> results = (List<Object>) js.executeScript(script);
            
            if (results != null && !results.isEmpty()) {
                for (Object result : results) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
                    String text = (String) map.get("text");
                    if (text.contains("可用状态")) {
                        System.out.println("通过JavaScript找到状态: " + text);
                        return text;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("JavaScript查找失败: " + e.getMessage());
        }
        
        return null;
    }
    
    // 查找有效的URL（count > 0的URL）
    public static List<String> findValidUrlsWithRelaxedSecurity(String[] urls) {
        List<String> validUrls = new ArrayList<>();
        WebDriver driver = null;
        
        try {
            io.github.bonigarcia.wdm.WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--ignore-ssl-errors");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            driver = new ChromeDriver(options);
            
            for (String url : urls) {
                try {
                    System.out.println("正在检查URL: " + url);
                    driver.get(url);
                    
                    // 等待页面加载
                    Thread.sleep(8000);
                    
                    // 方法1: 通过ID查找card-count
                    String countText = null;
                    try {
                        WebElement countElement = driver.findElement(By.id("card-count"));
                        countText = countElement.getText().trim();
                    } catch (Exception e) {
                        // 方法2: 通过文本内容查找
                        String pageText = driver.findElement(By.tagName("body")).getText();
                        if (pageText.contains("card-count")) {
                            // 尝试通过JavaScript获取
                            JavascriptExecutor js = (JavascriptExecutor) driver;
                            countText = (String) js.executeScript(
                                "return document.getElementById('card-count')?.textContent || ''");
                        }
                    }
                    
                    if (countText != null && !countText.isEmpty()) {
                        try {
                            int count = Integer.parseInt(countText.trim());
                            System.out.println("找到票卡数量: " + count);
                            
                            if (count > 0) {
                                validUrls.add(url);
                                System.out.println("✓ URL有效，已添加到列表");
                            } else {
                                System.out.println("✗ URL无效（票卡数量为0）");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("票卡数量格式错误: " + countText);
                        }
                    } else {
                        System.out.println("未找到票卡数量信息");
                    }
                    
                } catch (Exception e) {
                    System.err.println("处理URL失败 (" + url + "): " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("初始化WebDriver失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
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
    
    // 主方法 - 完整工作流程
    public static void main(String[] args) {
        System.out.println("=== 开始处理地铁票卡信息 ===");
        
        // 目标URL
        String targetUrl = "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=KEVTATSFIX19HAFA";
        
        // 1. 获取状态信息
        System.out.println("\n--- 步骤1: 获取状态信息 ---");
        String status = getStatusWithRelaxedSecurity(targetUrl);
        if (status != null) {
            System.out.println("成功获取状态: " + status);
        } else {
            System.out.println("获取状态失败");
        }
        
        // 2. 查找有效的URL
        System.out.println("\n--- 步骤2: 查找有效URL ---");
        String[] testUrls = {
            targetUrl
            // 可以添加更多测试URL
        };
        
        List<String> validUrls = findValidUrlsWithRelaxedSecurity(testUrls);
        System.out.println("找到的有效URL数量: " + validUrls.size());
        System.out.println("有效URL列表: " + validUrls);
        
        // 3. 生成HTML页面
        if (!validUrls.isEmpty()) {
            System.out.println("\n--- 步骤3: 生成HTML页面 ---");
            String redirectUrl = validUrls.get(0);
            String htmlPage = generateHtmlPage(redirectUrl);
            
            System.out.println("HTML页面生成成功！");
            System.out.println("重定向URL: " + redirectUrl);
            System.out.println("HTML内容长度: " + htmlPage.length() + " 字符");
            
            // 可以保存到文件
            // Files.write(Paths.get("metro_service.html"), htmlPage.getBytes());
        } else {
            System.out.println("没有找到有效的URL，无法生成页面");
        }
        
        System.out.println("\n=== 处理完成 ===");
    }
}
package com.grace.ticket.util;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DynamicContentProcessor {
    
    private WebDriver driver;
    
    public void initDriver() {
        // 设置 Firefox 驱动
        io.github.bonigarcia.wdm.WebDriverManager.firefoxdriver().setup();
        
        FirefoxOptions options = new FirefoxOptions();
        FirefoxProfile profile = new FirefoxProfile();
        
        // 配置 Firefox 偏好设置
        profile.setPreference("dom.webdriver.enabled", false);
        profile.setPreference("useAutomationExtension", false);
        profile.setPreference("browser.privatebrowsing.autostart", true); // 隐私模式
        profile.setPreference("permissions.default.image", 1); // 允许图片
        profile.setPreference("dom.disable_beforeunload", true);
        profile.setPreference("dom.popup_maximum", 0);
        profile.setPreference("dom.disable_open_during_load", false);
        
        // 忽略证书错误
        profile.setAcceptUntrustedCertificates(true);
        profile.setAssumeUntrustedCertificateIssuer(false);
        
        options.setProfile(profile);
        
        // 设置 Firefox 选项
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        // 如果需要无头模式，取消注释下面这行
        // options.addArguments("--headless");
        
        driver = new FirefoxDriver(options);
    }
    
    public String getStatusFromDynamicContent(String url) {
        try {
            driver.get(url);
            
            // 等待动态内容加载
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            // 等待票卡列表出现 - 使用更灵活的选择器
            try {
                // 方法1: 等待包含"可用状态"的元素
                wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), '可用状态')]")));
                
                // 获取状态文本
                WebElement statusElement = driver.findElement(
                    By.xpath("//*[contains(text(), '可用状态')]"));
                
                String status = statusElement.getText().trim();
                System.out.println("成功获取状态: " + status);
                return status;
                
            } catch (Exception e) {
                System.out.println("方法1失败，尝试方法2: " + e.getMessage());
                
                // 方法2: 直接搜索页面文本
                String pageText = driver.findElement(By.tagName("body")).getText();
                System.out.println("页面文本内容: " + pageText);
                
                if (pageText.contains("可用状态")) {
                    String[] lines = pageText.split("\\n");
                    for (String line : lines) {
                        if (line.contains("可用状态") && line.contains("随时可用")) {
                            System.out.println("从页面文本中找到状态: " + line.trim());
                            return line.trim();
                        }
                    }
                }
                
                // 方法3: 使用 JavaScript 查找
                return findStatusWithJavaScript();
            }
            
        } catch (Exception e) {
            System.err.println("获取状态失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private String findStatusWithJavaScript() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // 使用 JavaScript 在页面中搜索包含"可用状态"的元素
            String script = """
                // 查找所有包含'可用状态'文本的元素
                var elements = document.querySelectorAll('*');
                var results = [];
                
                for (var i = 0; i < elements.length; i++) {
                    var element = elements[i];
                    var text = element.textContent || element.innerText;
                    
                    if (text && text.includes('可用状态')) {
                        results.push({
                            tagName: element.tagName,
                            className: element.className,
                            text: text.trim(),
                            fullText: text,
                            html: element.outerHTML
                        });
                    }
                }
                
                return results;
                """;
            
            @SuppressWarnings("unchecked")
            List<Object> results = (List<Object>) js.executeScript(script);
            
            if (results != null && !results.isEmpty()) {
                for (Object result : results) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> map = (java.util.Map<String, Object>) result;
                    String text = (String) map.get("text");
                    String fullText = (String) map.get("fullText");
                    
                    System.out.println("JavaScript找到: " + text);
                    System.out.println("完整文本: " + fullText);
                    
                    if (text.contains("可用状态") && text.contains("随时可用")) {
                        return text;
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            System.err.println("JavaScript查找失败: " + e.getMessage());
            return null;
        }
    }
    
    public List<String> findValidUrlsFromDynamicContent(String[] urls) {
        List<String> validUrls = new ArrayList<>();
        
        for (String url : urls) {
            try {
                System.out.println("正在检查URL: " + url);
                driver.get(url);
                
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
                
                // 等待 card-count 元素出现
                try {
                    // 方法1: 通过ID查找
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("card-count")));
                    
                    WebElement countElement = driver.findElement(By.id("card-count"));
                    String countText = countElement.getText().trim();
                    
                    System.out.println("获取到的count值: " + countText);
                    
                    int count = Integer.parseInt(countText);
                    if (count >= 0) {
                        validUrls.add(url);
                        System.out.println("✓ URL有效，已添加到列表");
                    } else {
                        System.out.println("✗ URL无效（票卡数量为0）");
                    }
                    
                } catch (Exception e) {
                    System.out.println("通过ID查找失败，尝试其他方法: " + e.getMessage());
                    
                    // 方法2: 通过文本内容查找
                    String pageText = driver.findElement(By.tagName("body")).getText();
                    if (pageText.contains("card-count")) {
                        // 使用 JavaScript 查找
                        JavascriptExecutor js = (JavascriptExecutor) driver;
                        String countText = (String) js.executeScript(
                            "var elem = document.getElementById('card-count'); " +
                            "return elem ? elem.textContent.trim() : '';");
                        
                        if (!countText.isEmpty()) {
                            int count = Integer.parseInt(countText);
                            if (count > 0) {
                                validUrls.add(url);
                                System.out.println("✓ URL有效（通过JavaScript），已添加到列表");
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                System.err.println("处理URL失败 (" + url + "): " + e.getMessage());
            }
        }
        
        return validUrls;
    }
    
    public void takeScreenshot(String filename) {
        try {
            // 如果需要截图功能，可以使用以下代码
            // File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            // FileUtils.copyFile(screenshot, new File(filename));
            System.out.println("截图功能需要额外的依赖配置");
        } catch (Exception e) {
            System.err.println("截图失败: " + e.getMessage());
        }
    }
    
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    public static String generateHtmlPage(String redirectUrl) {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"zh-CN\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>地铁乘车服务</title>\n" +
            "    <style>\n" +
            "        body {\n" +
            "            font-family: Arial, sans-serif;\n" +
            "            max-width: 600px;\n" +
            "            margin: 0 auto;\n" +
            "            padding: 20px;\n" +
            "            background-color: #f5f5f5;\n" +
            "        }\n" +
            "        .section {\n" +
            "            background: white;\n" +
            "            border-radius: 10px;\n" +
            "            padding: 20px;\n" +
            "            margin-bottom: 20px;\n" +
            "            box-shadow: 0 2px 10px rgba(0,0,0,0.1);\n" +
            "        }\n" +
            "        .wechat-section {\n" +
            "            text-align: center;\n" +
            "            background: linear-gradient(135deg, #07c160, #05a050);\n" +
            "            color: white;\n" +
            "        }\n" +
            "        .wechat-id {\n" +
            "            font-size: 24px;\n" +
            "            font-weight: bold;\n" +
            "            margin: 10px 0;\n" +
            "        }\n" +
            "        .iframe-container {\n" +
            "            width: 100%;\n" +
            "            height: 400px;\n" +
            "            border: 1px solid #ddd;\n" +
            "            border-radius: 8px;\n" +
            "            overflow: hidden;\n" +
            "            margin: 0;\n" +
            "        }\n" +
            "        .iframe-container iframe {\n" +
            "            width: 100%;\n" +
            "            height: 100%;\n" +
            "            border: none;\n" +
            "        }\n" +
            "        .iframe-title {\n" +
            "            font-size: 16px;\n" +
            "            font-weight: bold;\n" +
            "            margin-bottom: 8px;\n" +
            "            color: #333;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <!-- 第一部分：微信联系方式 -->\n" +
            "    <div class=\"section wechat-section\">\n" +
            "        <h2>请联系客服微信</h2>\n" +
            "        <div class=\"wechat-id\">whengrong</div>\n" +
            "        <p>如有问题请添加微信咨询</p>\n" +
            "    </div>\n" +
            "    \n" +
            "    <!-- 第二部分：直接显示票卡信息 -->\n" +
            "    <div class=\"section\">\n" +
            "        <div class=\"iframe-title\">票卡信息：</div>\n" +
            "        <div class=\"iframe-container\">\n" +
            "            <iframe src=\"" + redirectUrl + "\" title=\"地铁票卡信息\" id=\"ticket-frame\"></iframe>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    \n" +
            "    <script>\n" +
            "        // 监听iframe加载完成\n" +
            "        document.getElementById('ticket-frame').addEventListener('load', function() {\n" +
            "            console.log('票卡页面加载完成');\n" +
            "            \n" +
            "            // 可以在这里添加与iframe内页面的交互代码\n" +
            "            try {\n" +
            "                var iframe = document.getElementById('ticket-frame');\n" +
            "                var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;\n" +
            "                \n" +
            "                // 检查iframe内是否包含乘车按钮\n" +
            "                var rideButton = iframeDoc.getElementById('start-ride');\n" +
            "                if (rideButton) {\n" +
            "                    console.log('找到乘车按钮');\n" +
            "                }\n" +
            "            } catch (e) {\n" +
            "                // 跨域限制，无法访问iframe内容\n" +
            "                console.log('由于安全限制，无法访问iframe内部内容');\n" +
            "            }\n" +
            "        });\n" +
            "        \n" +
            "        // 自动调整iframe高度（如果需要）\n" +
            "        function adjustIframeHeight() {\n" +
            "            var iframe = document.getElementById('ticket-frame');\n" +
            "            try {\n" +
            "                var iframeDoc = iframe.contentDocument || iframe.contentWindow.document;\n" +
            "                var height = iframeDoc.body.scrollHeight;\n" +
            "                iframe.style.height = height + 'px';\n" +
            "            } catch (e) {\n" +
            "                // 跨域限制\n" +
            "            }\n" +
            "        }\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
    
    public static void main(String[] args) {
        DynamicContentProcessor processor = new DynamicContentProcessor();
        
        try {
            processor.initDriver();
            
            // 测试获取状态
            String status = processor.getStatusFromDynamicContent(
                "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=5IVFVTROWKKXUJAP");
            System.out.println("动态获取的状态: " + status);
            
            // 测试URL验证
            String[] testUrls = {
            		"http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=5IVFVTROWKKXUJAP",
                "http://19.nat0.cn:29027/mobile/shenzhen/pages/count-card.html?code=KEVTATSFIX19HAFA"
                // 添加更多测试URL
            };
            
            List<String> validUrls = processor.findValidUrlsFromDynamicContent(testUrls);
            System.out.println("有效的URL: " + validUrls);
            
            // 生成HTML页面
            if (!validUrls.isEmpty()) {
                String html = generateHtmlPage(validUrls.get(0));
                System.out.println("HTML页面生成成功！");
                // 可以保存到文件
                 Files.write(Paths.get("d:\\metro_service_firefox.html"), html.getBytes());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            processor.close();
        }
    }
}
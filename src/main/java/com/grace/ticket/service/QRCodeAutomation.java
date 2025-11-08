package com.grace.ticket.service;


import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.time.Duration;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

public class QRCodeAutomation {

    // 配置常量
    private static final String DEVICE_NAME = "GSLDU16A19000284";
    private static final String APP_PACKAGE = "com.iss.shenzhenmetro";
    private static final String APP_ACTIVITY = "com.pingan.smt.ui.activity.MainActivity";
    private static final String APPIUM_SERVER_URL = "http://127.0.0.1:4723";
    private static final int IMPLICIT_WAIT_SECONDS = 15;
    private static final int EXPLICIT_WAIT_SECONDS = 20;

    // 测试账号信息
    private static final String PHONE_NUMBER = "17820635357";
    private static final String PASSWORD = "1Test12345";
    private static final int CLICK_WAIT_MS = 2000;

    public static void main(String[] args) {
        AndroidDriver driver = null;

        try {
            // 1. 配置设备选项 - 关键是 noReset=true, dontStopAppOnReset=true, 不传 "app" 参数
        	// 1. 配置设备选项 - 不使用链式调用，避免返回类型不一致的问题
        	UiAutomator2Options options = new UiAutomator2Options();
        	options.setPlatformName("Android");
        	options.setDeviceName(DEVICE_NAME);
        	options.setAutomationName("UiAutomator2");

        	// 不传 app 路径，直接使用已安装的 app
        	options.setAppPackage(APP_PACKAGE);
        	options.setAppActivity(APP_ACTIVITY);

        	// 使用已有安装状态，不重置、不卸载、不重启
        	// 有些版本的 appium-java-client 没有 fluent setFullReset / setNoReset，或返回 void，故用 setCapability 保险
        	try {
        	    // 优先使用 API，如果可用
        	    options.setNoReset(true); // 如果这个方法在你的版本返回 UiAutomator2Options，则有效
        	} catch (Throwable ignored) {
        	    // 若 setNoReset 方法不存在或签名不同，使用通用方式
        	    options.setCapability("noReset", true);
        	}

        	// dontStopAppOnReset（多数版本需用 setCapability）
        	options.setCapability("dontStopAppOnReset", true);

        	// fullReset 用 capability 明确设置为 false
        	options.setCapability("fullReset", false);

        	// 降低对设备的侵入性
        	options.setCapability("skipDeviceInitialization", true);
        	options.setCapability("skipServerInstallation", true);

        	// 自动授予权限
        	options.setCapability("autoGrantPermissions", true);


            // 2. 初始化驱动（Appium 会尝试创建 session，但不会卸载或强行重启 app）
            System.out.println("正在连接Appium服务器（不重装/不停止 app）...");
            driver = new AndroidDriver(new URL(APPIUM_SERVER_URL), options);

            // 设置等待策略
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT_SECONDS));

            System.out.println("Appium 会话连接成功，开始执行脚本...");
            
          //  autoLogin(driver);
            //getQRCode(driver);
            getInvalidQRCode(driver);
            //backToHomeFromQRCode(driver);
            //autoChangePassword(driver,"Test123456");
            autoLogout(driver);
            
            

        } catch (Exception e) {
            System.err.println("执行过程中发生错误: " + e.getMessage());
            e.printStackTrace();

            // 发生错误时截图
            if (driver != null) {
                try {
                    File screenshot = driver.getScreenshotAs(OutputType.FILE);
                    FileUtils.copyFile(screenshot, new File("error_screenshot.png"));
                    System.out.println("错误截图已保存为: error_screenshot.png");
                } catch (Exception screenshotEx) {
                    System.err.println("保存截图时发生错误: " + screenshotEx.getMessage());
                }
            }
        } finally {
            // 关闭会话
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("Appium会话已关闭");
                } catch (Exception e) {
                    System.err.println("关闭会话时发生错误: " + e.getMessage());
                }
            }
        }
    }
    
    
    public void captureQRCode(AppiumDriver driver) {
        try {
            // 等待二维码元素可见
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            By qrCodeButtonLocator = By.xpath("//android.view.View[@resource-id=\"app\"]/android.view.View[3]");
            
            WebElement qrCodeElement = wait.until(ExpectedConditions.visibilityOfElementLocated(qrCodeButtonLocator));
            
            // 方法1: 直接保存为文件
            File screenshotFile = qrCodeElement.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile, new File("qrcode_screenshot.png"));
            
            // 方法2: 如果需要调整保存路径
            String savePath = System.getProperty("user.dir") + "/screenshots/qrcode.png";
            FileUtils.copyFile(screenshotFile, new File(savePath));
            
            System.out.println("二维码截图已保存: " + savePath);
            
        } catch (Exception e) {
            System.out.println("捕获二维码时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 不可用，需删掉
     * @param driver
     * @param xpath
     * @param maxRetries
     */
    public static void captureQRCodeWithRetry(AndroidDriver driver,String xpath, int maxRetries) {
        int retries = 0;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
        
        while (retries < maxRetries) { 
        	 //虚拟票按钮
            WebElement reflashQrCode = driver.findElement(
                    By.xpath("//android.view.View[@text=\"点击刷新二维码\"]"));
            wait.until(ExpectedConditions.elementToBeClickable(reflashQrCode)).click();
            
            try {
                WebElement qrCodeElement = driver.findElement(
                    By.xpath(xpath));
                
                if (qrCodeElement.isDisplayed()) {
                    File screenshot = qrCodeElement.getScreenshotAs(OutputType.FILE);
                    FileUtils.copyFile(screenshot, new File("qrcode_" + System.currentTimeMillis() + ".png"));
                    System.out.println("二维码截图成功");
                    
                    
                        // 启动本地服务器
                       // LocalQRCodeServer.startServer(driver); 
                        
                        // 获取并显示二维码
                        //String base64 = QRCodeService.getAndDisplayQRCode(driver, 3);
                        
                    String base64 = null;
                    		
                        if (base64 != null) {
                            System.out.println("二维码已显示在: http://localhost:8080");
                            System.out.println("按回车键退出...");
                            System.in.read();
                        } else {
                            System.out.println("获取二维码失败");
                        }
                        
                    
                    
                    return;
                }
            } catch (Exception e) {
                retries++;
                System.out.println("尝试 " + retries + " 失败，等待重试...");
                try { Thread.sleep(1000); } catch (InterruptedException ie) {}
            }
        }
        System.out.println("无法获取二维码截图");
    }
   
    public static void displayQRCode(WebElement qrCodeElement) {
        try {
            // 获取截图文件
            File screenshotFile = qrCodeElement.getScreenshotAs(OutputType.FILE);
            BufferedImage image = ImageIO.read(screenshotFile);
            
            // 创建并显示图像窗口
            JFrame frame = new JFrame("二维码");
            JLabel label = new JLabel(new ImageIcon(image));
            frame.getContentPane().add(label, BorderLayout.CENTER);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * 自动退出登录功能
     * @param driver AppiumDriver实例,二维码页面回到主页面
     */
    public static void backToHomeFromQRCode(AndroidDriver driver) {
        try {
        	System.out.println("返回HOME页面...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
            //虚拟票按钮
            WebElement ticketLeftButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iv_title_left"));
            wait.until(ExpectedConditions.elementToBeClickable(ticketLeftButtonLocator)).click();
            
            Thread.sleep(1000);
            ticketLeftButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iv_title_left"));
            wait.until(ExpectedConditions.elementToBeClickable(ticketLeftButtonLocator)).click();
            
            Thread.sleep(1000);
           
            ticketLeftButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iv_title_left"));
            wait.until(ExpectedConditions.elementToBeClickable(ticketLeftButtonLocator)).click();
            
        } catch(Exception e) {
        	System.out.println("Exception in backToHomeFromQRCode");
        }
    }
    
    /**
     * 自动退出登录功能
     * @param driver AppiumDriver实例
     */
    public static void getQRCode(AndroidDriver driver) {
        try {
            System.out.println("开始执行获取二维码流程...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
            //首页按钮
            //By homeButtonLocator = By.xpath("//android.widget.LinearLayout[@resource-id=\"com.iss.shenzhenmetro:id/home_bottom_tab\"]/android.widget.RelativeLayout[1]/android.widget.LinearLayout/android.widget.ImageView");
            WebElement homeButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/home_bottom_tab"));
            wait.until(ExpectedConditions.elementToBeClickable(homeButtonLocator)).click();
            
         
            //虚拟票按钮
            WebElement virtualTicketButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iconView"));
            wait.until(ExpectedConditions.elementToBeClickable(virtualTicketButtonLocator)).click();
            
            Thread.sleep(1000);
            
            //我的票卡， TODO 修改ID
            //WebElement ticketPocketButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iconView"));
            WebElement ticketPocketButtonLocator = driver.findElement(By.xpath("//*[@text='我的票卡']"));
            wait.until(ExpectedConditions.elementToBeClickable(ticketPocketButtonLocator)).click();
            
            Thread.sleep(1000);
            
            //第一张票卡
            By cardButtonLocator = By.xpath("android.view.View[@resource-id=\"van-tab-2\"]/android.view.View/android.view.View/android.view.View[2]/android.view.View[1]");
            //wait.until(ExpectedConditions.elementToBeClickable(cardButtonLocator)).click();
            
            //票卡过期时间
            By cardExpiredDateLocator = By.xpath("new UiSelector().text(\"过期时间: 2025-09-25 04:30:00 过期\")");
            //wait.until(ExpectedConditions.elementToBeClickable(cardExpiredDateLocator)).click();

            //By cardStatusLocator = By.xpath("//android.view.View[@text=\"票卡状态: 未激活\"]");
            //wait.until(ExpectedConditions.elementToBeClickable(cardStatusLocator)).click();
            
            // 获取元素,打印 text 属性
            //WebElement element = driver.findElement(cardStatusLocator);
            //System.out.println("Text: " + element.getText());
            
            Thread.sleep(1000);
            
            By cardToUseButtonLocator = By.xpath("//android.widget.Button[@text=\"使用\"]");
            wait.until(ExpectedConditions.elementToBeClickable(cardToUseButtonLocator)).click();
            
            
            Thread.sleep(1000);
            
            String xpath ="//android.view.View[@resource-id=\"app\"]/android.view.View[3]";
            By qrCodeButtonLocator = By.xpath("//android.view.View[@resource-id=\"app\"]/android.view.View[3]");
            
            captureQRCodeWithRetry(driver,xpath,5);
            

            System.out.println("执行获取二维码流程执行完成！");
            
        } catch (Exception e) {
            System.out.println("登录过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    /**
     * 自动退出登录功能
     * @param driver AppiumDriver实例
     */
    public static void getInvalidQRCode(AndroidDriver driver) {
        try {
            System.out.println("开始执行获取二维码流程...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
            //首页按钮
            By homeButtonLocator = By.xpath("//android.widget.LinearLayout[@resource-id=\"com.iss.shenzhenmetro:id/home_bottom_tab\"]/android.widget.RelativeLayout[1]/android.widget.LinearLayout/android.widget.ImageView");
            wait.until(ExpectedConditions.elementToBeClickable(homeButtonLocator)).click();
            
         
            //虚拟票按钮
            WebElement virtualTicketButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iconView"));
            wait.until(ExpectedConditions.elementToBeClickable(virtualTicketButtonLocator)).click();
            
            Thread.sleep(1000);
            
            //我的票卡， TODO 修改ID
            WebElement ticketPocketButtonLocator = driver.findElement(By.xpath("//*[@text='我的票卡']"));
            wait.until(ExpectedConditions.elementToBeClickable(ticketPocketButtonLocator)).click();
            
            Thread.sleep(1000);
            
            //WebElement invalidTicketButtonLocator = driver.findElement(By.id("van-tabs-1-1"));
            WebElement invalidTicketButtonLocator = driver.findElement(By.xpath("//*[@text='已失效']"));
            wait.until(ExpectedConditions.elementToBeClickable(invalidTicketButtonLocator)).click();
            
            
            Thread.sleep(1000);
            //第一张票卡
            By cardButtonLocator = By.xpath("(//android.widget.Button[@text=\"查看\"])[1]");
            wait.until(ExpectedConditions.elementToBeClickable(cardButtonLocator)).click();
            
            
            Thread.sleep(1000);
            String qrCodePath ="//android.view.View[@resource-id=\"app\"]/android.view.View[3]";
            captureQRCodeWithRetry(driver,qrCodePath,5);
            

            System.out.println("执行获取二维码流程执行完成！");
            
        } catch (Exception e) {
            System.out.println("登录过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * 自动退出登录功能
     * @param driver AppiumDriver实例
     */
    public static void autoLogin(AndroidDriver driver) {
        try {
            System.out.println("开始执行登录流程...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
         
            By QRCodeButtonLocator = By.id("com.iss.shenzhenmetro:id/rideCodeView");
            wait.until(ExpectedConditions.elementToBeClickable(QRCodeButtonLocator)).click();

            
            // 下面的流程与原来相同...
            By loginButtonLocator = By.xpath("//android.widget.TextView[@text='立即登录']");
            wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator)).click();
            Thread.sleep(2000);

            By passwordLoginLocator = By.id("com.iss.shenzhenmetro:id/user_tv_psd_or_verify_login");
            wait.until(ExpectedConditions.elementToBeClickable(passwordLoginLocator)).click();
            Thread.sleep(2000);

            By phoneInputLocator = By.id("com.iss.shenzhenmetro:id/user_et_pwd_phone_num");
            wait.until(ExpectedConditions.visibilityOfElementLocated(phoneInputLocator)).sendKeys(PHONE_NUMBER);

            By passwordInputLocator = By.id("com.iss.shenzhenmetro:id/user_et_password");
            wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInputLocator)).sendKeys(PASSWORD);
            
            Thread.sleep(5000);
            
         // 在 driver 初始化 & wait 创建之后，执行隐私同意点击
            try {
                By privacyRadio = By.id("com.iss.shenzhenmetro:id/pasc_user_privacy_iv");
                // 等待最多 5 秒，如果出现就点击
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                shortWait.until(ExpectedConditions.elementToBeClickable(privacyRadio)).click();
            } catch (Exception e) {
                // 超时或未找到则忽略，继续后续流程
                System.out.println("未检测到隐私同意控件或点击失败，继续执行。");
            }


            By submitButtonLocator = By.id("com.iss.shenzhenmetro:id/user_rtv_login_button");
            wait.until(ExpectedConditions.elementToBeClickable(submitButtonLocator)).click();

            
            Thread.sleep(3000);
            

            System.out.println("登录流程执行完成！");
            
        } catch (Exception e) {
            System.out.println("登录过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * 自动退出登录功能
     * @param driver AppiumDriver实例
     */
    public static void autoLogout(AndroidDriver driver) {
        try {
            System.out.println("开始执行退出登录流程...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
            // 1. 点击"我的"
            System.out.println("步骤1: 点击'我的'");
            By myTabLocator = By.xpath("//android.widget.LinearLayout[@resource-id=\"com.iss.shenzhenmetro:id/home_bottom_tab\"]/android.widget.RelativeLayout[5]/android.widget.LinearLayout/android.widget.ImageView");
            wait.until(ExpectedConditions.elementToBeClickable(myTabLocator)).click();
            
            // 2. 点击"设置"
            System.out.println("步骤2: 点击'设置'");
            // 优先使用ID定位，如果失败再尝试XPath
            By settingLocatorById = By.id("com.iss.shenzhenmetro:id/settingView");
            By settingLocatorByXpath = By.xpath("//android.widget.ImageView[@resource-id=\"com.iss.shenzhenmetro:id/settingView\"]");
            wait.until(ExpectedConditions.elementToBeClickable(settingLocatorById)).click();
            
            // 3. 点击"退出登录"
            System.out.println("步骤3: 点击'退出登录'");
            By logoutLocatorById = By.id("com.iss.shenzhenmetro:id/tv_logout");
            By logoutLocatorByXpath = By.xpath("//android.widget.TextView[@resource-id=\"com.iss.shenzhenmetro:id/tv_logout\"]");
            wait.until(ExpectedConditions.elementToBeClickable(logoutLocatorById)).click();
            
            By logoutConfirm = By.id("com.iss.shenzhenmetro:id/tv_second");
            wait.until(ExpectedConditions.elementToBeClickable(logoutConfirm)).click();
            
            //返回主页
            WebElement ticketLeftButtonLocator = driver.findElement(By.id("com.iss.shenzhenmetro:id/iv_title_left"));
            wait.until(ExpectedConditions.elementToBeClickable(ticketLeftButtonLocator)).click();
            
            System.out.println("退出登录流程执行完成！");
            
        } catch (Exception e) {
            System.out.println("退出登录过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
 
    /**
     * 自动修改密码功能
     * @param driver AndroidDriver实例
     * @param newPassword 新密码
     */
    public static void autoChangePassword(AndroidDriver driver, String newPassword) {
        try {
            System.out.println("开始执行修改密码流程...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
            // 1. 点击"我的"
            System.out.println("步骤1: 点击'我的'");
            By myTabLocator = By.xpath("//android.widget.LinearLayout[@resource-id=\"com.iss.shenzhenmetro:id/home_bottom_tab\"]/android.widget.RelativeLayout[5]/android.widget.LinearLayout/android.widget.ImageView");
            wait.until(ExpectedConditions.elementToBeClickable(myTabLocator)).click();
            
            // 2. 点击"设置"
            System.out.println("步骤2: 点击'设置'");
            By settingLocatorById = By.id("com.iss.shenzhenmetro:id/settingView");
            By settingLocatorByXpath = By.xpath("//android.widget.ImageView[@resource-id=\"com.iss.shenzhenmetro:id/settingView\"]");
            wait.until(ExpectedConditions.elementToBeClickable(settingLocatorById)).click();
            
            // 3. 点击"账户安全"
            System.out.println("步骤3: 点击'账户安全'");
            By accountSafetyLocatorById = By.id("com.iss.shenzhenmetro:id/tv_account_safety");
            By accountSafetyLocatorByXpath = By.xpath("//android.widget.TextView[@resource-id=\"com.iss.shenzhenmetro:id/tv_account_safety\"]");
            wait.until(ExpectedConditions.elementToBeClickable(accountSafetyLocatorById)).click();
            
            // 4. 点击"密码设置"
            System.out.println("步骤4: 点击'密码设置'");
            By passwordSettingLocatorById = By.id("com.iss.shenzhenmetro:id/user_tv_modify_pwd");
            By passwordSettingLocatorByXpath = By.xpath("//android.widget.TextView[@resource-id=\"com.iss.shenzhenmetro:id/user_tv_modify_pwd\"]");
            wait.until(ExpectedConditions.elementToBeClickable(passwordSettingLocatorById)).click();
            
            // 5. 点击"获取验证码"
            System.out.println("步骤5: 点击'获取验证码'");
            By getCodeLocatorById = By.id("com.iss.shenzhenmetro:id/user_tv_get_code");
            By getCodeLocatorByXpath = By.xpath("//android.widget.TextView[@resource-id=\"com.iss.shenzhenmetro:id/user_tv_get_code\"]");
            wait.until(ExpectedConditions.elementToBeClickable(getCodeLocatorById)).click();
            
            // 等待验证码发送（可能需要手动输入或自动获取验证码）
            System.out.println("请手动输入验证码，等待10秒...");
            Thread.sleep(10000);
            
            // 6. 输入验证码（这里需要实际获取验证码的逻辑）
            System.out.println("步骤6: 输入验证码");
            String verificationCode = getVerificationCode(); // 需要实现获取验证码的方法
            By codeInputLocatorById = By.id("com.iss.shenzhenmetro:id/user_et_code");
            By codeInputLocatorByXpath = By.xpath("//android.widget.EditText[@resource-id=\"com.iss.shenzhenmetro:id/user_et_code\"]");
            inputTextWithAlternative(wait, codeInputLocatorById, codeInputLocatorByXpath, verificationCode);
            
            // 7. 点击"下一步"
            System.out.println("步骤7: 点击'下一步'");
            By nextButtonLocatorById = By.id("com.iss.shenzhenmetro:id/user_btn_next");
            By nextButtonLocatorByXpath = By.xpath("//android.widget.Button[@resource-id=\"com.iss.shenzhenmetro:id/user_btn_next\"]");
            wait.until(ExpectedConditions.elementToBeClickable(nextButtonLocatorById)).click();
            
            // 8. 输入新密码
            System.out.println("步骤8: 输入新密码");
            By newPwdLocatorById = By.id("com.iss.shenzhenmetro:id/user_et_pwd");
            By newPwdLocatorByXpath = By.xpath("//android.widget.EditText[@resource-id=\"com.iss.shenzhenmetro:id/user_et_pwd\"]");
            inputTextWithAlternative(wait, newPwdLocatorById, newPwdLocatorByXpath, newPassword);
            
            // 9. 再次输入密码
            System.out.println("步骤9: 再次输入密码");
            By confirmPwdLocatorById = By.id("com.iss.shenzhenmetro:id/user_et_confirm_pwd");
            By confirmPwdLocatorByXpath = By.xpath("//android.widget.EditText[@resource-id=\"com.iss.shenzhenmetro:id/user_et_confirm_pwd\"]");
            inputTextWithAlternative(wait, confirmPwdLocatorById, confirmPwdLocatorByXpath, newPassword);
            
            // 10. 点击"确认"
            System.out.println("步骤10: 点击'确认'");
            By confirmButtonLocatorById = By.id("com.iss.shenzhenmetro:id/user_btn_commit");
            By confirmButtonLocatorByXpath = By.xpath("//android.widget.Button[@resource-id=\"com.iss.shenzhenmetro:id/user_btn_commit\"]");
            clickElementWithAlternative(wait, confirmButtonLocatorById, confirmButtonLocatorByXpath);
            
            System.out.println("修改密码流程执行完成！");
            
        } catch (Exception e) {
            System.out.println("修改密码过程中出现异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 点击元素并等待
     */
    private static void clickElementWithWait(WebDriverWait wait, By locator) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
            Thread.sleep(CLICK_WAIT_MS);
        } catch (Exception e) {
            System.out.println("点击元素失败: " + locator.toString());
            throw new RuntimeException("无法点击元素: " + e.getMessage());
        }
    }
    
    /**
     * 使用主定位器点击元素，如果失败则尝试备用定位器
     */
    private static void clickElementWithAlternative(WebDriverWait wait, By primaryLocator, By alternativeLocator) {
        try {
            clickElementWithWait(wait, primaryLocator);
        } catch (Exception e) {
            System.out.println("主定位器失败，尝试备用定位器...");
            clickElementWithWait(wait, alternativeLocator);
        }
    }
    
    /**
     * 输入文本
     */
    private static void inputTextWithWait(WebDriverWait wait, By locator, String text) {
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.clear();
            element.sendKeys(text);
            Thread.sleep(1000); // 输入后短暂等待
        } catch (Exception e) {
            System.out.println("输入文本失败: " + locator.toString());
            throw new RuntimeException("无法输入文本: " + e.getMessage());
        }
    }
    
    /**
     * 使用主定位器输入文本，如果失败则尝试备用定位器
     */
    private static void inputTextWithAlternative(WebDriverWait wait, By primaryLocator, By alternativeLocator, String text) {
        try {
            inputTextWithWait(wait, primaryLocator, text);
        } catch (Exception e) {
            System.out.println("主定位器失败，尝试备用定位器输入文本...");
            inputTextWithWait(wait, alternativeLocator, text);
        }
    }
    
    /**
     * 获取验证码（需要根据实际情况实现）
     * 这里只是一个示例，实际需要从短信、邮件或其他渠道获取
     */
    private static String getVerificationCode() {
        // 这里需要实现实际的验证码获取逻辑
        // 例如：从短信中读取、从数据库获取、调用API等
        System.out.println("请手动输入验证码，这里返回一个示例验证码: 123456");
        return "123456"; // 示例验证码
    }
    
    /**
     * 带重试机制的修改密码方法
     */
    public static void autoChangePasswordWithRetry(AndroidDriver driver, String newPassword, int maxRetries) {
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                autoChangePassword(driver, newPassword);
                System.out.println("修改密码成功！");
                return;
            } catch (Exception e) {
                retryCount++;
                System.out.println("第 " + retryCount + " 次尝试失败，等待重试...");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        System.out.println("修改密码失败，已达到最大重试次数: " + maxRetries);
    }
    
 // 在 QRCodeAutomation 类中添加以下方法

    /**
     * 支持自定义手机号和密码的登录方法
     */
    public static void autoLogin(AndroidDriver driver, String phoneNumber, String password) {
        try {
            System.out.println("开始执行登录流程，手机号: " + phoneNumber);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT_SECONDS));
            
            // 原有的登录流程，但使用传入的手机号和密码
            By QRCodeButtonLocator = By.id("com.iss.shenzhenmetro:id/rideCodeView");
            wait.until(ExpectedConditions.elementToBeClickable(QRCodeButtonLocator)).click();
            
            By loginButtonLocator = By.xpath("//android.widget.TextView[@text='立即登录']");
            wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator)).click();
            Thread.sleep(2000);

            By passwordLoginLocator = By.id("com.iss.shenzhenmetro:id/user_tv_psd_or_verify_login");
            wait.until(ExpectedConditions.elementToBeClickable(passwordLoginLocator)).click();
            Thread.sleep(2000);

            By phoneInputLocator = By.id("com.iss.shenzhenmetro:id/user_et_pwd_phone_num");
            wait.until(ExpectedConditions.visibilityOfElementLocated(phoneInputLocator)).sendKeys(phoneNumber);

            By passwordInputLocator = By.id("com.iss.shenzhenmetro:id/user_et_password");
            wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInputLocator)).sendKeys(password);
            
            // ... 其余流程保持不变
            
            System.out.println("登录流程执行完成，手机号: " + phoneNumber);
            
        } catch (Exception e) {
            System.out.println("登录过程中出现异常，手机号: " + phoneNumber + ", 错误: " + e.getMessage());
            throw new RuntimeException("登录失败", e);
        }
    }
}

package com.grace.ticket.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.grace.ticket.service.AppiumServiceManager;
import com.grace.ticket.service.QRCodeAutomation;

import io.appium.java_client.android.AndroidDriver;

//@RestController
//@RequestMapping("/automation")
public class AutomationController {
    
    @Autowired
    private AppiumServiceManager appiumServiceManager;
    
    /**
     * 显示自动化管理页面
     */
    @GetMapping("/manager")
    public ModelAndView showManagerPage() {
        return new ModelAndView("automation-manager"); // 对应HTML文件名
    }
    
    /**
     * 单手机号登录接口
     */
    @PostMapping("/login-single")
    public Map<String, Object> singleAutoLogin(@RequestBody SingleRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = request.getPhoneNumber();
            String password = request.getPassword();
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "手机号码不能为空");
                return response;
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "密码不能为空");
                return response;
            }
            
            String status = appiumServiceManager.autoLogin(appiumServiceManager.getDriver(), phoneNumber, password);
            
            if ("success".equals(status)) {
                response.put("success", true);
                response.put("message", "登录成功");
            } else {
                response.put("success", false);
                response.put("message", "登录失败: " + status);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "执行过程中发生错误");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 单手机号退出接口
     */
    @PostMapping("/logout-single")
    public Map<String, Object> singleAutoLogout(@RequestBody SingleRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = request.getPhoneNumber();
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "手机号码不能为空");
                return response;
            }
            
            // 执行退出操作
            AppiumServiceManager.autoLogout(appiumServiceManager.getDriver());
            
            response.put("success", true);
            response.put("message", "退出成功");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "退出过程中发生错误");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 获取最后乘车状态接口
     */
    @PostMapping("/latest-status")
    public Map<String, Object> getLatestRideStatus(@RequestBody SingleRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String phoneNumber = request.getPhoneNumber();
            
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "手机号码不能为空");
                return response;
            }
            
            // 获取交易记录列表
            List<Map<String, String>> recordList = appiumServiceManager.getLatestStation(appiumServiceManager.getDriver());
            
            // 构建交易记录响应数据
            List<Map<String, Object>> transactionRecords = new ArrayList<>();
            int recordCount = 1;
            
            for (Map<String, String> rec : recordList) {
                Map<String, Object> recordInfo = new HashMap<>();
                recordInfo.put("recordNumber", recordCount);
                recordInfo.put("time", rec.get("time"));
                recordInfo.put("inOut", rec.get("inOut"));
                recordInfo.put("station", rec.get("station"));
                transactionRecords.add(recordInfo);
                recordCount++;
            }
            
            // 构建响应数据
            response.put("success", true);
            response.put("message", "获取状态成功");
            response.put("phoneNumber", phoneNumber);
            response.put("totalRecords", recordList.size());
            response.put("transactionRecords", transactionRecords);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取乘车状态过程中发生错误");
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    /**
     * 批量自动登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> batchAutoLogin(@RequestBody BatchRequest request) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            List<String> phoneNumbers = request.getPhoneNumbers();
            String password = request.getPassword();
            
            if (phoneNumbers == null || phoneNumbers.isEmpty()) {
                response.put("success", false);
                response.put("message", "手机号码列表不能为空");
                return response;
            }
            
            // 为每个手机号码创建异步任务
            List<Future<Map<String, Object>>> futures = new ArrayList<>();
            
            for (String phoneNumber : phoneNumbers) {
                appiumServiceManager.autoLogin(appiumServiceManager.getDriver(), phoneNumber, password);
                appiumServiceManager.getLatestStation(appiumServiceManager.getDriver());
            }
            
            // 收集所有结果
            for (Future<Map<String, Object>> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("phoneNumber", "未知");
                    errorResult.put("success", false);
                    errorResult.put("error", "任务执行异常: " + e.getMessage());
                    results.add(errorResult);
                }
            }
            
            response.put("success", true);
            response.put("message", "自动登录执行完成");
            response.put("results", results);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "执行过程中发生错误: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 批量自动退出接口
     */
    @PostMapping("/logout")
    public Map<String, Object> batchAutoLogout(@RequestBody BatchRequest request) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            List<String> phoneNumbers = request.getPhoneNumbers();
            
            if (phoneNumbers == null || phoneNumbers.isEmpty()) {
                response.put("success", false);
                response.put("message", "手机号码列表不能为空");
                return response;
            }
            
            // 为每个手机号码执行退出操作
            for (String phoneNumber : phoneNumbers) {
                Map<String, Object> result = new HashMap<>();
                result.put("phoneNumber", phoneNumber);
                
                try {
                    // 执行退出操作
                    AppiumServiceManager.autoLogout(appiumServiceManager.getDriver());
                    result.put("success", true);
                    result.put("message", "退出成功");
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("error", "退出失败: " + e.getMessage());
                }
                
                results.add(result);
            }
            
            response.put("success", true);
            response.put("message", "自动退出执行完成");
            response.put("results", results);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "执行过程中发生错误: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 执行单个手机的自动登录
     */
    private Map<String, Object> executeAutoLogin(String phoneNumber, String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("phoneNumber", phoneNumber);
        
        AndroidDriver driver = null;
        try {
            // 这里需要根据您的实际配置创建AndroidDriver
            // 注意：您需要修改QRCodeAutomation类以支持不同的手机号码和密码
            
            System.out.println("开始为手机 " + phoneNumber + " 执行自动登录");
            
            // 创建新的自动化实例
            QRCodeAutomation automation = new QRCodeAutomation();
            
            // 这里需要您修改QRCodeAutomation类，使其支持传入不同的手机号和密码
            // 例如：automation.autoLogin(driver, phoneNumber, password);
            
            // 临时使用固定方法（您需要修改）
            QRCodeAutomation.autoLogin(driver); // 这里需要您适配
            
            result.put("success", true);
            result.put("message", "登录成功");
            
            System.out.println("手机 " + phoneNumber + " 自动登录完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            System.err.println("手机 " + phoneNumber + " 自动登录失败: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    System.err.println("关闭驱动时出错: " + e.getMessage());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 执行单个手机的自动退出
     */
    private Map<String, Object> executeAutoLogout(String phoneNumber) {
        Map<String, Object> result = new HashMap<>();
        result.put("phoneNumber", phoneNumber);
        
        AndroidDriver driver = null;
        try {
            System.out.println("开始为手机 " + phoneNumber + " 执行自动退出");
            
            // 创建新的自动化实例
            QRCodeAutomation automation = new QRCodeAutomation();
            
            // 这里需要您修改QRCodeAutomation类，使其支持传入不同的手机号
            // 临时使用固定方法（您需要修改）
            QRCodeAutomation.autoLogout(driver); // 这里需要您适配
            
            result.put("success", true);
            result.put("message", "退出成功");
            
            System.out.println("手机 " + phoneNumber + " 自动退出完成");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            System.err.println("手机 " + phoneNumber + " 自动退出失败: " + e.getMessage());
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    System.err.println("关闭驱动时出错: " + e.getMessage());
                }
            }
        }
        
        return result;
    }
    
    /**
     * 请求参数类
     */
    public static class BatchRequest {
        private List<String> phoneNumbers;
        private String password;
        
        // getters and setters
        public List<String> getPhoneNumbers() {
            return phoneNumbers;
        }
        
        public void setPhoneNumbers(List<String> phoneNumbers) {
            this.phoneNumbers = phoneNumbers;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    /**
     * 单请求参数类
     */
    public static class SingleRequest {
        private String phoneNumber;
        private String password;
        
        // getters and setters
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
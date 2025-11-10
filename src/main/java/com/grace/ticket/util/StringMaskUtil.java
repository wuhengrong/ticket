package com.grace.ticket.util;

public class StringMaskUtil {
    
	  /**
     * 将字符串中间部分替换为星号
     * 偶数长度替换2个，奇数长度替换3个
     * @param input 输入字符串
     * @return 处理后的字符串
     */
    public static String maskMiddle(String input) {
        if (input == null || input.length() < 6) {
            return input; // 长度不足6位，直接返回原字符串
        }
        
        int length = input.length();
        int maskCount = (length % 2 == 0) ? 2 : 3; // 偶数替换2个，奇数替换3个
        int startIndex = (length - maskCount) / 2;
        
        StringBuilder sb = new StringBuilder(input);
        for (int i = 0; i < maskCount; i++) {
            sb.setCharAt(startIndex + i, '*');
        }
        
        return sb.toString();
    }
    /**
     * 将字符串中间3个字符替换为*
     * @param input 原始字符串
     * @return 替换后的字符串
     */
    public static String maskMiddleThree(String input) {
        if (input == null || input.length() < 3) {
            return input;
        }
        
        int length = input.length();
        int startMaskIndex = (length - 3) / 2;
        
        // 构建新字符串：前部分 + *** + 后部分
        return input.substring(0, startMaskIndex) + 
               "***" + 
               input.substring(startMaskIndex + 3);
    }
    
    /**
     * 专门处理6位字符串（替换中间3位）
     */
    public static String maskSixDigit(String input) {
        if (input == null || input.length() != 6) {
            return input;
        }
        return input.substring(0, 1) + "***" + input.substring(4);
    }
    
    /**
     * 专门处理11位字符串（替换中间3位）
     */
    public static String maskElevenDigit(String input) {
        if (input == null || input.length() != 11) {
            return input;
        }
        return input.substring(0, 4) + "***" + input.substring(7);
    }
}
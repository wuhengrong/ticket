package com.grace.ticket.service;

import java.util.ArrayList;
import java.util.List;

import com.grace.ticket.entity.GroupMember;

public class TicketUtilService {
	  /**
     * 检查下一个用户是否休假
     */
    public static  boolean isNextUserOnLeave(List<GroupMember> groupMembers, int nextUserOrder) {
        for (GroupMember member : groupMembers) {
            // 解析用户的使用顺序
            List<Integer> userOrders = parseUseOrder(member.getUseOrder());
            
            // 检查用户是否包含下一个使用序号且处于休假状态
            if (userOrders.contains(nextUserOrder) && "ON_LEAVE".equals(member.getStatus())) {
                System.out.println("用户 " + member.getUserName() + " (使用顺序: " + member.getUseOrder() + 
                                 ") 在序号 " + nextUserOrder + " 处于休假状态");
                return true;
            }
        }
        
        System.out.println("序号 " + nextUserOrder + " 的用户处于正常状态");
        return false;
    }
    
    /**
     * 解析使用顺序（兼容两种格式）
     * 格式1: 13 -> [1, 3] (每个数字代表一个使用顺序)
     * 格式2: 24 -> [2, 4]
     */
    private static List<Integer> parseUseOrder(Integer useOrder) {
        List<Integer> result = new ArrayList<>();
        
        if (useOrder == null) {
            return result;
        }
        
        String useOrderStr = String.valueOf(useOrder);
        
        try {
            // 将数字字符串分解为单个数字
            for (char c : useOrderStr.toCharArray()) {
                result.add(Character.getNumericValue(c));
            }
        } catch (NumberFormatException e) {
            System.err.println("解析使用顺序失败: " + useOrderStr);
        }
        
        return result;
    }
}

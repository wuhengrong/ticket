package com.grace.ticket.risk;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class MetroTransferValidator {
    
    private static final String FILE_PATH = "D:\\tools\\metro\\customer_test.xlsx";
    private MetroTripAllValidator metroTripValidator = new MetroTripAllValidator();
    
    // 定义表格列索引
    private static final int COL_NAME = 0;
    private static final int COL_PLATFORM = 1;
    private static final int COL_CUSTOMER_TYPE = 2;
    private static final int COL_WORK_TIME = 3;
    private static final int COL_WORK_START_STATION = 4;
    private static final int COL_WORK_END_STATION = 5;
    private static final int COL_WORK_DURATION = 6;
    private static final int COL_WORK_ORIGINAL_PRICE = 7;
    private static final int COL_OFF_TIME = 8;
    private static final int COL_OFF_START_STATION = 9;
    private static final int COL_OFF_END_STATION = 10;
    private static final int COL_OFF_ORIGINAL_PRICE = 11;
    private static final int COL_OFF_DURATION = 12;
    private static final int COL_REMARK = 13;
    
    /**
     * 验证换乘时间是否充足
     * @param givenWorkTime 给定的上班时间
     * @param givenWorkStartStation 给定的上班始发站
     * @return 验证结果列表
     */
    public List<TransferValidationResult> validateTransferTime(String givenWorkTime, String givenWorkStartStation) {
        List<TransferValidationResult> results = new ArrayList<>();
        
        try (FileInputStream file = new FileInputStream(FILE_PATH);
             Workbook workbook = new XSSFWorkbook(file)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // 从第1行开始，跳过标题行
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue; // 忽略空行
                }
                
                // 获取上班时间、上班始发站、上班目的站
                String workTime = getCellStringValue(row.getCell(COL_WORK_TIME));
                String workStartStation = getCellStringValue(row.getCell(COL_WORK_START_STATION));
                String workEndStation = getCellStringValue(row.getCell(COL_WORK_END_STATION));
                
                // 检查三项信息是否为空
                if (workTime == null || workTime.trim().isEmpty() ||
                    workStartStation == null || workStartStation.trim().isEmpty() ||
                    workEndStation == null || workEndStation.trim().isEmpty()) {
                    continue; // 忽略有空值的行
                }
                
                // 处理时间格式
                String processedWorkTime = processTimeFormat(workTime);
                if (processedWorkTime == null) {
                    continue; // 时间格式无法解析，跳过该行
                }
                
                // 计算换乘时间是否充足
                boolean isTransferTimeSufficient = calculateTransferTimeSufficient(
                    processedWorkTime, workStartStation, workEndStation, givenWorkTime, givenWorkStartStation);
                
                // 创建结果对象
                TransferValidationResult result = new TransferValidationResult();
                result.setRowNumber(i + 1); // Excel行号（从1开始）
                result.setName(getCellStringValue(row.getCell(COL_NAME)));
                result.setWorkTime(workTime);
                result.setProcessedWorkTime(processedWorkTime);
                result.setWorkStartStation(workStartStation);
                result.setWorkEndStation(workEndStation);
                result.setTransferTimeSufficient(isTransferTimeSufficient);
                
                results.add(result);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * 处理时间格式
     */
    private String processTimeFormat(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        String processedTime = timeStr.trim();
        
        // 1. 替换中文冒号为英文冒号
        processedTime = processedTime.replace('：', ':');
        
        // 2. 去掉中文字符
        processedTime = processedTime.replaceAll("[\\u4e00-\\u9fa5]", "");
        
        // 3. 处理时间范围格式，取前面的时间
        if (processedTime.contains("-")) {
            String[] times = processedTime.split("-");
            if (times.length > 0) {
                processedTime = times[0].trim();
            }
        }
        
        // 4. 验证时间格式是否正确
        try {
            // 尝试解析时间，如果失败则返回null
            LocalTime.parse(processedTime, DateTimeFormatter.ofPattern("H:mm"));
            return processedTime;
        } catch (DateTimeParseException e) {
            System.err.println("时间格式解析失败: " + processedTime);
            return null;
        }
    }
    
    /**
     * 计算换乘时间是否充足
     * 逻辑：上班时间 + (上班始发站到上班目的站的地铁时间) + (上班目的站到给定上班始发站的交通时间) < 给定上班时间
     */
    private boolean calculateTransferTimeSufficient(String workTime, String workStartStation, String workEndStation,
                                                   String givenWorkTime, String givenWorkStartStation) {
        try {
            // 解析时间
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
            LocalTime startWorkTime = LocalTime.parse(workTime, formatter);
            LocalTime givenTime = LocalTime.parse(givenWorkTime, formatter);
            
            // 计算上班始发站到上班目的站的地铁纯运行时间
            int metroDuration1 = calculateMetroDuration(workStartStation, workEndStation);
            
            // 计算上班目的站到给定上班始发站的交通时间（地铁或打车的最短时间）
            int transferDuration = calculateTransferDuration(workEndStation, givenWorkStartStation);
            
            // 计算总时间：上班时间 + 地铁运行时间 + 交通时间
            LocalTime totalTime = startWorkTime
                .plusMinutes(metroDuration1)
                .plusMinutes(transferDuration);
            
            // 比较是否小于给定上班时间
            return totalTime.isBefore(givenTime);
            
        } catch (Exception e) {
            System.err.println("计算换乘时间失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 计算地铁纯运行时间（上班始发站到上班目的站）
     */
    private int calculateMetroDuration(String fromStation, String toStation) {
        try {
            List<RouteResult> routes = metroTripValidator.calculateAllRoutes(fromStation, toStation);
            
            if (routes != null && !routes.isEmpty()) {
                // 取最短的地铁运行时间
                return routes.stream()
                    .filter(route -> "subway".equals(route.getMode()))
                    .mapToInt(RouteResult::getDuration)
                    .min()
                    .orElse(getFallbackDuration(fromStation, toStation));
            }
        } catch (Exception e) {
            System.err.println("计算地铁运行时间失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return getFallbackDuration(fromStation, toStation);
    }
    
    /**
     * 计算交通时间（从上班目的站到给定上班始发站）- 包含地铁和打车
     */
    private int calculateTransferDuration(String fromStation, String toStation) {
        try {
            List<RouteResult> routes = metroTripValidator.calculateAllRoutes(fromStation, toStation);
            
            if (routes != null && !routes.isEmpty()) {
                // 取最短的交通时间（包括地铁、打车等）
                return routes.stream()
                    .filter(route -> "subway".equals(route.getMode()) || "taxi".equals(route.getMode()))
                    .mapToInt(RouteResult::getDuration)
                    .min()
                    .orElse(getFallbackDuration(fromStation, toStation));
            }
        } catch (Exception e) {
            System.err.println("计算交通时间失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return getFallbackDuration(fromStation, toStation);
    }
    
    /**
     * 降级方案：估算交通时间
     */
    private int getFallbackDuration(String fromStation, String toStation) {
        // 这里可以根据实际情况实现估算逻辑
        // 例如：根据站点数量估算，平均每站2-3分钟
        System.out.println("使用降级方案估算时间: " + fromStation + " -> " + toStation);
        return 30; // 默认返回30分钟
    }
    
    /**
     * 获取单元格字符串值
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        // 根据单元格类型获取值
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是时间格式
                    return cell.getLocalDateTimeCellValue().toLocalTime().format(DateTimeFormatter.ofPattern("H:mm"));
                } else {
                    // 如果是数字
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((int) numValue); // 整数
                    } else {
                        return String.valueOf(numValue); // 小数
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // 对于公式单元格，尝试获取计算后的值
                try {
                    if (cell.getCachedFormulaResultType() == CellType.STRING) {
                        return cell.getStringCellValue();
                    } else if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        return String.valueOf(cell.getNumericCellValue());
                    } else if (cell.getCachedFormulaResultType() == CellType.BOOLEAN) {
                        return String.valueOf(cell.getBooleanCellValue());
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
    
    /**
     * 验证结果类
     */
    public static class TransferValidationResult {
        private int rowNumber;
        private String name;
        private String workTime;
        private String processedWorkTime;
        private String workStartStation;
        private String workEndStation;
        private boolean transferTimeSufficient;
        
        // getters and setters
        public int getRowNumber() { return rowNumber; }
        public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getWorkTime() { return workTime; }
        public void setWorkTime(String workTime) { this.workTime = workTime; }
        
        public String getProcessedWorkTime() { return processedWorkTime; }
        public void setProcessedWorkTime(String processedWorkTime) { this.processedWorkTime = processedWorkTime; }
        
        public String getWorkStartStation() { return workStartStation; }
        public void setWorkStartStation(String workStartStation) { this.workStartStation = workStartStation; }
        
        public String getWorkEndStation() { return workEndStation; }
        public void setWorkEndStation(String workEndStation) { this.workEndStation = workEndStation; }
        
        public boolean isTransferTimeSufficient() { return transferTimeSufficient; }
        public void setTransferTimeSufficient(boolean transferTimeSufficient) { this.transferTimeSufficient = transferTimeSufficient; }
        
        @Override
        public String toString() {
            return String.format("行号: %d, 姓名: %s, 上班时间: %s, 始发站: %s, 目的站: %s, 换乘时间充足: %s",
                rowNumber, name, workTime, workStartStation, workEndStation, transferTimeSufficient ? "是" : "否");
        }
    }
    
    /**
     * 主方法测试
     */
    public static void main(String[] args) {
        MetroTransferValidator validator = new MetroTransferValidator();
        
        // 测试数据
        String givenWorkTime = "9:00";
        String givenWorkStartStation = "马安山";
        
        List<TransferValidationResult> results = validator.validateTransferTime(givenWorkTime, givenWorkStartStation);
        
        // 输出结果
        System.out.println("换乘时间验证结果:");
        System.out.println("给定上班时间: " + givenWorkTime + ", 给定上班始发站: " + givenWorkStartStation);
        System.out.println("=========================================");
        
        for (TransferValidationResult result : results) {
            System.out.println(result);
        }
        
        // 统计
        long sufficientCount = results.stream().filter(TransferValidationResult::isTransferTimeSufficient).count();
        System.out.println("=========================================");
        System.out.println("总计: " + results.size() + " 条记录，换乘时间充足: " + sufficientCount + " 条");
    }
}

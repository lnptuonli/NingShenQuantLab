package com.ningshenquantlab.alphaforge_demo1.quant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据获取请求 DTO
 * 
 * 用途：
 * - 封装前端提交的数据获取参数
 * - 提供参数校验
 * - 支持多种数据获取场景
 * 
 * API 使用示例：
 * POST /api/quant/data/fetch
 * {
 *   "startDate": "20240101",
 *   "endDate": "20240201",
 *   "dataType": "stock_daily",
 *   "symbols": "000001,000002",
 *   "forceRefresh": false
 * }
 * 
 * 对应 Python 命令：
 * python main_data_acq_run_date_range.py 20240101 20240201
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataFetchRequest {
    
    /**
     * 开始日期
     * 
     * 格式：YYYYMMDD（如 20240101）
     * 必填：是
     * 校验：必须是8位数字，且为有效日期
     * 
     * 示例：
     * - "20240101" - 2024年1月1日
     * - "20231231" - 2023年12月31日
     */
    @NotBlank(message = "开始日期不能为空")
    @Pattern(regexp = "^\\d{8}$", message = "开始日期格式错误，应为 YYYYMMDD（如 20240101）")
    private String startDate;
    
    /**
     * 结束日期
     * 
     * 格式：YYYYMMDD（如 20240201）
     * 必填：是
     * 校验：必须是8位数字，且为有效日期，且 >= startDate
     * 
     * 示例：
     * - "20240201" - 2024年2月1日
     * - "20240131" - 2024年1月31日
     */
    @NotBlank(message = "结束日期不能为空")
    @Pattern(regexp = "^\\d{8}$", message = "结束日期格式错误，应为 YYYYMMDD（如 20240201）")
    private String endDate;
    
    /**
     * 数据类型（可选）
     * 
     * 说明：指定要获取的数据类型
     * 可选值：
     * - stock_daily：股票日线数据（默认）
     * - stock_minute：股票分钟数据
     * - financial：财务数据
     * - index：指数数据
     * - fund：基金数据
     * 
     * 用途：
     * - 如果不指定，默认获取所有类型
     * - 可传递给 Python 脚本作为参数
     */
    private String dataType;
    
    /**
     * 股票代码列表（可选）
     * 
     * 格式：逗号分隔的股票代码
     * 示例：
     * - "000001,000002,600519"
     * - "ALL" - 表示所有股票
     * 
     * 用途：
     * - 如果不指定，默认获取所有股票
     * - 可用于增量更新特定股票
     */
    private String symbols;
    
    /**
     * 是否强制刷新（可选）
     * 
     * 说明：
     * - true：即使数据已存在也重新获取（覆盖）
     * - false：跳过已存在的数据（默认）
     * 
     * 用途：修复数据错误、重新拉取历史数据
     */
    @Builder.Default
    private Boolean forceRefresh = false;
    
    /**
     * 任务名称（可选）
     * 
     * 说明：自定义任务名称，便于识别
     * 示例：
     * - "日常数据更新"
     * - "历史数据回溯：2023全年"
     * - "测试数据拉取"
     * 
     * 用途：
     * - 如果不指定，系统自动生成（如："数据获取：20240101-20240201"）
     */
    private String taskName;
    
    /**
     * 任务优先级（可选）
     * 
     * 说明：任务执行优先级
     * 可选值：
     * - HIGH：高优先级（1）
     * - NORMAL：普通优先级（5，默认）
     * - LOW：低优先级（9）
     * 
     * 用途：
     * - 紧急任务优先执行
     * - 后台批量任务降低优先级
     */
    @Builder.Default
    private Integer priority = 5;
    
    /**
     * 超时时间（秒，可选）
     * 
     * 说明：任务执行的最大时长
     * 默认值：3600（1小时）
     * 
     * 用途：
     * - 防止任务长时间占用资源
     * - 全量数据获取可设置更长超时时间（如 12小时 = 43200秒）
     */
    @Builder.Default
    private Integer timeoutSeconds = 3600;
    
    // ==================== 实用方法 ====================
    
    /**
     * 生成默认任务名称
     */
    public String getDefaultTaskName() {
        return String.format("数据获取：%s - %s", startDate, endDate);
    }
    
    /**
     * 获取任务名称（如果未设置则使用默认）
     */
    public String getEffectiveTaskName() {
        return (taskName != null && !taskName.isEmpty()) ? taskName : getDefaultTaskName();
    }
    
    /**
     * 校验日期范围是否合法
     */
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        try {
            return Integer.parseInt(startDate) <= Integer.parseInt(endDate);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 转换为 Python 脚本参数数组
     * 
     * 返回：["20240101", "20240201"]
     */
    public String[] toPythonArgs() {
        return new String[]{startDate, endDate};
    }
}


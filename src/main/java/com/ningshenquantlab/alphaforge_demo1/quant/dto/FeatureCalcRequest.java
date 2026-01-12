package com.ningshenquantlab.alphaforge_demo1.quant.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 特征计算请求 DTO
 * 
 * 用途：
 * - 封装前端提交的特征计算参数
 * - 支持单日计算和批量计算两种模式
 * - 提供参数校验
 * 
 * API 使用示例：
 * 
 * 模式1：单日计算
 * POST /api/quant/features/calculate
 * {
 *   "date": "20250301",
 *   "force": true,
 *   "taskName": "单日特征计算"
 * }
 * 
 * 模式2：批量计算（日期范围）
 * POST /api/quant/features/calculate
 * {
 *   "startDate": "20250301",
 *   "endDate": "20250305",
 *   "force": false,
 *   "taskName": "批量特征计算"
 * }
 * 
 * 对应 Python 命令：
 * - 单日：python feature_manager.py calc --date 20250301 --force
 * - 批量：python feature_manager.py calc --start 20250301 --end 20250305 --force
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureCalcRequest {
    
    /**
     * 单日计算日期
     * 
     * 格式：YYYYMMDD（如 20250301）
     * 说明：与 startDate/endDate 互斥，二选一
     * 
     * 使用场景：
     * - 只计算某一天的特征
     * - 补算某一天的数据
     */
    @Pattern(regexp = "^\\d{8}$", message = "日期格式错误，应为 YYYYMMDD（如 20250301）")
    private String date;
    
    /**
     * 批量计算开始日期
     * 
     * 格式：YYYYMMDD（如 20250301）
     * 说明：与 date 互斥，必须和 endDate 一起使用
     * 
     * 使用场景：
     * - 计算一段时间的特征
     * - 全量铺底（start = 历史最早日期，end = 今天）
     */
    @Pattern(regexp = "^\\d{8}$", message = "开始日期格式错误，应为 YYYYMMDD（如 20250301）")
    private String startDate;
    
    /**
     * 批量计算结束日期
     * 
     * 格式：YYYYMMDD（如 20250305）
     * 说明：必须 >= startDate
     */
    @Pattern(regexp = "^\\d{8}$", message = "结束日期格式错误，应为 YYYYMMDD（如 20250305）")
    private String endDate;
    
    /**
     * 是否强制重新计算
     * 
     * 说明：
     * - true：即使特征已存在也重新计算（覆盖）
     * - false：跳过已计算的特征（默认）
     * 
     * 使用场景：
     * - 修复计算错误
     * - 特征算法更新后重新计算
     * - 数据源更新后重新计算
     */
    @Builder.Default
    private Boolean force = false;
    
    /**
     * 特征类型（可选）
     * 
     * 说明：指定要计算的特征类型
     * 可选值：
     * - alpha：alpha 因子特征
     * - technical：技术指标特征
     * - fundamental：基本面特征
     * - all：所有特征（默认）
     * 
     * 用途：只计算特定类型的特征，节省时间
     */
    private String featureType;
    
    /**
     * 股票代码列表（可选）
     * 
     * 格式：逗号分隔的股票代码
     * 示例：
     * - "000001,000002,600519"
     * - "ALL" - 表示所有股票（默认）
     * 
     * 用途：只计算特定股票的特征
     */
    private String symbols;
    
    /**
     * 任务名称（可选）
     * 
     * 说明：自定义任务名称，便于识别
     * 示例：
     * - "日常特征更新"
     * - "全量特征铺底"
     * - "Alpha 因子回溯计算"
     * 
     * 用途：
     * - 如果不指定，系统自动生成
     */
    private String taskName;
    
    /**
     * 任务优先级（可选）
     * 
     * 说明：任务执行优先级
     * 可选值：
     * - 1：高优先级
     * - 5：普通优先级（默认）
     * - 9：低优先级
     */
    @Builder.Default
    private Integer priority = 5;
    
    /**
     * 超时时间（秒，可选）
     * 
     * 说明：任务执行的最大时长
     * 默认值：7200（2小时）
     * 
     * 用途：
     * - 特征计算通常比数据获取耗时更长
     * - 全量铺底可能需要几个小时
     */
    @Builder.Default
    private Integer timeoutSeconds = 7200;
    
    // ==================== 实用方法 ====================
    
    /**
     * 判断是否为单日计算模式
     */
    public boolean isSingleDateMode() {
        return date != null && !date.isEmpty();
    }
    
    /**
     * 判断是否为批量计算模式
     */
    public boolean isBatchMode() {
        return startDate != null && !startDate.isEmpty() 
            && endDate != null && !endDate.isEmpty();
    }
    
    /**
     * 校验参数是否合法
     */
    public void validate() {
        // 必须指定计算模式（单日或批量）
        if (!isSingleDateMode() && !isBatchMode()) {
            throw new IllegalArgumentException("必须指定计算模式：单日计算（date）或批量计算（startDate + endDate）");
        }
        
        // 不能同时指定两种模式
        if (isSingleDateMode() && isBatchMode()) {
            throw new IllegalArgumentException("不能同时指定单日计算和批量计算模式");
        }
        
        // 批量模式下，结束日期必须 >= 开始日期
        if (isBatchMode()) {
            try {
                int start = Integer.parseInt(startDate);
                int end = Integer.parseInt(endDate);
                if (end < start) {
                    throw new IllegalArgumentException("结束日期必须大于等于开始日期");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("日期格式错误");
            }
        }
    }
    
    /**
     * 生成默认任务名称
     */
    public String getDefaultTaskName() {
        if (isSingleDateMode()) {
            return String.format("特征计算：%s", date);
        } else if (isBatchMode()) {
            return String.format("特征计算：%s - %s", startDate, endDate);
        } else {
            return "特征计算";
        }
    }
    
    /**
     * 获取任务名称（如果未设置则使用默认）
     */
    public String getEffectiveTaskName() {
        return (taskName != null && !taskName.isEmpty()) ? taskName : getDefaultTaskName();
    }
    
    /**
     * 转换为 Python 脚本参数数组
     * 
     * 返回示例：
     * - 单日模式：["calc", "--date", "20250301", "--force"]
     * - 批量模式：["calc", "--start", "20250301", "--end", "20250305", "--force"]
     */
    public List<String> toPythonArgs() {
        List<String> args = new ArrayList<>();
        args.add("calc");  // 子命令
        
        if (isSingleDateMode()) {
            // 单日计算模式
            args.add("--date");
            args.add(date);
        } else if (isBatchMode()) {
            // 批量计算模式
            args.add("--start");
            args.add(startDate);
            args.add("--end");
            args.add(endDate);
        }
        
        // 添加 force 参数
        if (force != null && force) {
            args.add("--force");
        }
        
        return args;
    }
    
    /**
     * 获取日期范围描述（用于日志）
     */
    public String getDateRangeDescription() {
        if (isSingleDateMode()) {
            return date;
        } else if (isBatchMode()) {
            return startDate + " ~ " + endDate;
        } else {
            return "未指定";
        }
    }
}


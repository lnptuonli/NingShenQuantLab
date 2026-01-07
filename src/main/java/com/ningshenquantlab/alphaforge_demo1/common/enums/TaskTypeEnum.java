package com.ningshenquantlab.alphaforge_demo1.common.enums;

import lombok.Getter;

/**
 * 任务类型枚举
 * 
 * 定义系统支持的所有量化任务类型
 * 每种任务类型对应不同的 Python 脚本和业务逻辑
 * 
 * 使用场景：
 * 1. 创建任务时指定任务类型
 * 2. 根据任务类型路由到不同的执行器
 * 3. 任务统计和查询时按类型分组
 */
@Getter
public enum TaskTypeEnum {
    
    /**
     * 特征计算任务
     * 
     * 说明：调用 feature_manager.py 计算量化因子
     * 参数：start_date（开始日期）, end_date（结束日期）, force（是否强制重算）
     * 示例：python feature_manager.py calc --start 20250301 --end 20250305
     * 
     * 适用场景：
     * - 每日定时计算最新特征
     * - 回测前批量计算历史特征
     * - 特征数据有误时强制重算
     */
    FEATURE_CALC("FEATURE_CALC", "特征计算", "feature_manager.py"),
    
    /**
     * 数据获取任务
     * 
     * 说明：调用数据获取脚本下载/更新股票行情数据
     * 参数：start_date（开始日期）, end_date（结束日期）
     * 示例：python main_data_acq_run_date_range.py --start 20250301 --end 20250305
     * 
     * 适用场景：
     * - 每日定时获取最新行情数据
     * - 补充历史缺失数据
     * - 数据源更新后重新获取
     */
    DATA_FETCH("DATA_FETCH", "数据获取", "main_data_acq_run_date_range.py"),
    
    /**
     * 模型训练任务
     * 
     * 说明：调用机器学习模型训练脚本
     * 参数：model_name（模型名称）, train_start（训练开始日期）, train_end（训练结束日期）
     * 示例：python model_trainer.py --model lgb --start 20230101 --end 20231231
     * 
     * 适用场景：
     * - 定期重新训练模型
     * - 模型调参和优化
     * - 新策略开发时的模型训练
     * 
     * 注意：当前脚本尚未实现，预留接口
     */
    MODEL_TRAIN("MODEL_TRAIN", "模型训练", "model_trainer.py"),
    
    /**
     * 回测任务（扩展）
     * 
     * 说明：调用策略回测脚本，评估策略表现
     * 参数：strategy_name（策略名称）, start_date, end_date
     * 
     * 适用场景：
     * - 新策略验证
     * - 参数优化
     * - 历史表现评估
     * 
     * 注意：当前为预留类型，未来扩展使用
     */
    BACKTEST("BACKTEST", "策略回测", "backtest_runner.py");
    
    /**
     * 任务类型代码（用于数据库存储和API传输）
     * 
     * 命名规范：全大写，使用下划线分隔
     * 示例：FEATURE_CALC, DATA_FETCH, MODEL_TRAIN
     */
    private final String code;
    
    /**
     * 任务类型描述（用于前端显示，中文说明）
     * 
     * 说明：简短的中文名称，便于用户理解
     * 示例：特征计算、数据获取、模型训练
     */
    private final String description;
    
    /**
     * 对应的 Python 脚本名称
     * 
     * 说明：执行该类型任务时调用的 Python 脚本文件名
     * 路径：脚本文件位于 ${python.script-path} 目录下
     * 
     * 示例：
     * - feature_manager.py
     * - main_data_acq_run_date_range.py
     * - model_trainer.py
     * 
     * 注意：
     * - 脚本名称不包含路径，只是文件名
     * - 完整路径由 CommandBuilder 根据配置拼接
     */
    private final String scriptName;
    
    /**
     * 构造器
     * 
     * @param code 任务类型代码（数据库存储）
     * @param description 任务类型描述（前端显示）
     * @param scriptName 对应的 Python 脚本名称
     */
    TaskTypeEnum(String code, String description, String scriptName) {
        this.code = code;
        this.description = description;
        this.scriptName = scriptName;
    }
    
    /**
     * 根据任务类型代码获取枚举
     * 
     * 使用场景：
     * - 从数据库读取任务记录时，将 String 类型转为枚举
     * - 解析 API 请求参数时，验证任务类型是否合法
     * 
     * @param code 任务类型代码（不区分大小写）
     * @return 对应的枚举，如果找不到返回 null
     * 
     * 示例：
     * TaskTypeEnum.fromCode("FEATURE_CALC") → FEATURE_CALC 枚举
     * TaskTypeEnum.fromCode("feature_calc") → FEATURE_CALC 枚举（自动转大写）
     * TaskTypeEnum.fromCode("INVALID")      → null
     */
    public static TaskTypeEnum fromCode(String code) {
        // 空值检查：如果传入的 code 为空，直接返回 null
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        // 转大写后匹配（兼容小写输入）
        String upperCode = code.toUpperCase();
        
        // 遍历所有枚举值，找到匹配的返回
        for (TaskTypeEnum type : TaskTypeEnum.values()) {
            if (type.code.equals(upperCode)) {
                return type;
            }
        }
        
        // 未找到匹配的枚举，返回 null
        return null;
    }
    
    /**
     * 根据脚本名称获取任务类型
     * 
     * 使用场景：
     * - 通过脚本名称反向查找任务类型
     * - 日志分析时识别任务类型
     * 
     * @param scriptName 脚本名称
     * @return 对应的枚举，如果找不到返回 null
     * 
     * 示例：
     * TaskTypeEnum.fromScriptName("feature_manager.py") → FEATURE_CALC
     */
    public static TaskTypeEnum fromScriptName(String scriptName) {
        if (scriptName == null || scriptName.trim().isEmpty()) {
            return null;
        }
        
        for (TaskTypeEnum type : TaskTypeEnum.values()) {
            if (type.scriptName.equals(scriptName)) {
                return type;
            }
        }
        
        return null;
    }
    
    /**
     * 判断是否为数据相关任务（需要访问数据库或外部数据源）
     * 
     * 使用场景：
     * - 资源调度时区分计算密集型和IO密集型任务
     * - 监控时统计数据任务的执行情况
     * 
     * @return true-数据相关任务, false-其他任务
     */
    public boolean isDataRelated() {
        return this == DATA_FETCH || this == FEATURE_CALC;
    }
    
    /**
     * 判断是否为计算密集型任务（需要大量CPU/GPU资源）
     * 
     * 使用场景：
     * - 资源调度时限制并发数量
     * - 监控时关注CPU/内存占用
     * 
     * @return true-计算密集型任务, false-其他任务
     */
    public boolean isComputeIntensive() {
        return this == MODEL_TRAIN || this == BACKTEST;
    }
    
    /**
     * 获取任务类型的默认超时时间（秒）
     * 
     * 说明：不同类型任务的执行时长差异很大，设置合理的默认超时时间
     * 
     * @return 默认超时时间（秒）
     * 
     * 超时时间设置依据：
     * - FEATURE_CALC: 1小时（3600秒）- 批量计算特征较慢
     * - DATA_FETCH: 30分钟（1800秒）- 网络下载时间
     * - MODEL_TRAIN: 2小时（7200秒）- 模型训练耗时长
     * - BACKTEST: 1小时（3600秒）- 回测计算量大
     * 
     * 注意：
     * - 这只是默认值，可以在配置文件中覆盖
     * - 实际执行时可以在请求中指定超时时间
     */
    public int getDefaultTimeout() {
        switch (this) {
            case FEATURE_CALC:
                return 3600;  // 1小时
            case DATA_FETCH:
                return 1800;  // 30分钟
            case MODEL_TRAIN:
                return 7200;  // 2小时
            case BACKTEST:
                return 3600;  // 1小时
            default:
                return 3600;  // 默认1小时
        }
    }
}

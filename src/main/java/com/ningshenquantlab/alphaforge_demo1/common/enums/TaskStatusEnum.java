package com.ningshenquantlab.alphaforge_demo1.common.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 * 用于标识任务在执行过程中的各种状态
 */
@Getter
public enum TaskStatusEnum {
    
    /**
     * 待执行 - 任务已创建，等待执行
     */
    PENDING("PENDING", "待执行"),
    
    /**
     * 执行中 - 任务正在执行
     */
    RUNNING("RUNNING", "执行中"),
    
    /**
     * 成功 - 任务执行成功完成
     */
    SUCCESS("SUCCESS", "成功"),
    
    /**
     * 失败 - 任务执行失败（Python脚本返回非0退出码或抛出异常）
     */
    FAILED("FAILED", "失败"),
    
    /**
     * 已取消 - 任务被用户手动取消
     */
    CANCELLED("CANCELLED", "已取消"),
    
    /**
     * 超时 - 任务执行超过设定的超时时间
     */
    TIMEOUT("TIMEOUT", "超时");
    
    /**
     * 状态码（用于数据库存储和API传输）
     */
    private final String code;
    
    /**
     * 状态描述（用于前端显示）
     */
    private final String description;
    
    /**
     * enum枚举构造器
     * @param code 状态码
     * @param description 状态描述
     */
    TaskStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据状态码获取枚举
     * @param code 状态码
     * @return 对应的枚举，如果找不到返回null
     */
    public static TaskStatusEnum fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (TaskStatusEnum status : TaskStatusEnum.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        
        return null;
    }
    
    /**
     * 判断是否为终态（任务已结束，不会再变化）
     * @return true-终态, false-非终态
     */
    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }
    
    /**
     * 判断是否为成功状态
     * @return true-成功, false-其他
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * 判断是否为失败状态（包含FAILED、CANCELLED、TIMEOUT）
     * @return true-失败, false-其他
     */
    public boolean isFailure() {
        return this == FAILED || this == CANCELLED || this == TIMEOUT;
    }
}

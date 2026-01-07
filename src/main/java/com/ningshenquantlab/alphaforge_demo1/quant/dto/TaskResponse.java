package com.ningshenquantlab.alphaforge_demo1.quant.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 任务响应 DTO
 * 
 * 用途：
 * - 封装任务信息返回给前端
 * - 提供友好的展示格式
 * - 隐藏内部实现细节
 * 
 * API 使用示例：
 * GET /api/quant/data/tasks/123
 * {
 *   "taskId": 123,
 *   "taskType": "DATA_FETCH",
 *   "taskName": "数据获取：20240101-20240201",
 *   "status": "RUNNING",
 *   "statusDesc": "执行中",
 *   "progress": 45,
 *   "startTime": "2024-01-01 10:00:00",
 *   "duration": "5分30秒",
 *   "result": null,
 *   "errorMessage": null
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 任务类型代码
     * 
     * 示例：DATA_FETCH, FEATURE_CALC, MODEL_TRAIN
     */
    private String taskType;
    
    /**
     * 任务类型描述
     * 
     * 示例：数据获取、特征计算、模型训练
     */
    private String taskTypeDesc;
    
    /**
     * 任务名称
     * 
     * 示例：数据获取：20240101-20240201
     */
    private String taskName;
    
    /**
     * 任务状态代码
     * 
     * 示例：PENDING, RUNNING, SUCCESS, FAILED, CANCELLED, TIMEOUT
     */
    private String status;
    
    /**
     * 任务状态描述
     * 
     * 示例：待执行、执行中、成功、失败、已取消、超时
     */
    private String statusDesc;
    
    /**
     * 任务进度（百分比，0-100）
     * 
     * 说明：
     * - PENDING：0
     * - RUNNING：根据实际进度（暂时无法获取，固定返回 50）
     * - SUCCESS/FAILED/CANCELLED/TIMEOUT：100
     * 
     * 注意：
     * - Python 脚本需要输出进度信息才能实时更新
     * - 可通过解析 stdout 中的进度标记（如 "[PROGRESS] 45%"）
     */
    private Integer progress;
    
    /**
     * 任务开始时间
     * 
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 任务结束时间
     * 
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 任务执行时长（格式化）
     * 
     * 示例：
     * - "5秒"
     * - "3分30秒"
     * - "2小时15分"
     */
    private String duration;
    
    /**
     * 任务执行时长（秒）
     * 
     * 用途：供前端计算、排序
     */
    private Long durationSeconds;
    
    /**
     * 任务参数（JSON 格式）
     * 
     * 示例：{"startDate":"20240101","endDate":"20240201"}
     */
    private String params;
    
    /**
     * 执行结果摘要
     * 
     * 示例：
     * - "成功获取 1000 条数据"
     * - "计算了 50 个特征"
     */
    private String result;
    
    /**
     * 错误信息
     * 
     * 示例：
     * - "ValueError: 数据为空"
     * - "任务执行超时（3600秒）"
     */
    private String errorMessage;
    
    /**
     * 进程退出码
     * 
     * 0=成功，非0=失败
     */
    private Integer exitCode;
    
    /**
     * 任务创建时间
     * 
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 任务更新时间
     * 
     * 格式：yyyy-MM-dd HH:mm:ss
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 是否可取消
     * 
     * 说明：只有 PENDING 和 RUNNING 状态的任务可以取消
     */
    private Boolean cancellable;
    
    /**
     * 是否可重试
     * 
     * 说明：只有 FAILED、CANCELLED、TIMEOUT 状态的任务可以重试
     */
    private Boolean retriable;
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 从 TaskRecord 实体转换为 TaskResponse
     * 
     * @param record 任务记录实体
     * @return TaskResponse 对象
     */
    public static TaskResponse fromTaskRecord(TaskRecord record) {
        if (record == null) {
            return null;
        }
        
        // 计算执行时长
        String duration = "";
        Long durationSeconds = 0L;
        if (record.getStartTime() != null) {
            LocalDateTime end = record.getEndTime() != null 
                ? record.getEndTime() 
                : LocalDateTime.now();
            durationSeconds = Duration.between(record.getStartTime(), end).getSeconds();
            duration = formatDuration(durationSeconds);
        }
        
        // 计算进度
        Integer progress = calculateProgress(record.getStatus(), durationSeconds);
        
        // 判断是否可取消/重试
        boolean cancellable = "PENDING".equals(record.getStatus()) 
            || "RUNNING".equals(record.getStatus());
        boolean retriable = "FAILED".equals(record.getStatus()) 
            || "CANCELLED".equals(record.getStatus()) 
            || "TIMEOUT".equals(record.getStatus());
        
        // 获取任务类型和状态描述
        String taskTypeDesc = record.getTaskTypeEnum() != null 
            ? record.getTaskTypeEnum().getDescription() 
            : "";
        String statusDesc = record.getTaskStatusEnum() != null 
            ? record.getTaskStatusEnum().getDescription() 
            : "";
        
        return TaskResponse.builder()
                .taskId(record.getId())
                .taskType(record.getTaskType())
                .taskTypeDesc(taskTypeDesc)
                .taskName(record.getTaskName())
                .status(record.getStatus())
                .statusDesc(statusDesc)
                .progress(progress)
                .startTime(record.getStartTime())
                .endTime(record.getEndTime())
                .duration(duration)
                .durationSeconds(durationSeconds)
                .params(record.getParams())
                .result(record.getResult())
                .errorMessage(record.getErrorMessage())
                .exitCode(record.getExitCode())
                .createTime(record.getCreateTime())
                .updateTime(record.getUpdateTime())
                .createdBy(record.getCreatedBy())
                .cancellable(cancellable)
                .retriable(retriable)
                .build();
    }
    
    /**
     * 格式化时长
     * 
     * @param seconds 秒数
     * @return 格式化后的时长字符串
     */
    private static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分" + remainingSeconds + "秒";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + minutes + "分";
        }
    }
    
    /**
     * 计算任务进度
     * 
     * @param status 任务状态
     * @param durationSeconds 已执行时长（秒）
     * @return 进度百分比（0-100）
     */
    private static Integer calculateProgress(String status, Long durationSeconds) {
        if (status == null) {
            return 0;
        }
        
        switch (status) {
            case "PENDING":
                return 0;
            case "RUNNING":
                // 简单估算：假设任务平均需要 5 分钟（300 秒）
                // 实际项目中应该从数据库获取历史平均时长
                if (durationSeconds == null || durationSeconds == 0) {
                    return 10;
                }
                int estimated = (int) (durationSeconds * 100 / 300);
                return Math.min(estimated, 99); // 最多 99%，避免误导
            case "SUCCESS":
            case "FAILED":
            case "CANCELLED":
            case "TIMEOUT":
                return 100;
            default:
                return 0;
        }
    }
}


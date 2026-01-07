-- ============================================================
-- 量化调度平台 - 任务管理表
-- 版本：v1.0
-- 创建时间：2026-01-07
-- 说明：用于记录 Python 任务执行情况
-- ============================================================

USE alphaforge;

-- ============================================================
-- 1. 任务记录表 (task_record)
-- ============================================================
DROP TABLE IF EXISTS task_record;

CREATE TABLE task_record (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID（主键，自增）',
    
    -- 任务基本信息
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型（DATA_FETCH, FEATURE_CALC, MODEL_TRAIN, BACKTEST）',
    task_name VARCHAR(200) NOT NULL COMMENT '任务名称',
    
    -- 任务状态
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态（PENDING, RUNNING, SUCCESS, FAILED, CANCELLED, TIMEOUT）',
    
    -- 时间信息
    start_time DATETIME NULL COMMENT '任务开始时间',
    end_time DATETIME NULL COMMENT '任务结束时间',
    duration BIGINT NULL COMMENT '任务执行时长（秒）',
    
    -- 任务参数和结果
    params TEXT NULL COMMENT '任务参数（JSON 格式）',
    result TEXT NULL COMMENT '执行结果摘要',
    error_message VARCHAR(500) NULL COMMENT '错误信息',
    exit_code INT NULL COMMENT '进程退出码（0=成功，非0=失败）',
    
    -- 审计字段
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    created_by BIGINT NULL COMMENT '创建人ID',
    
    -- 索引
    INDEX idx_task_type (task_type),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time),
    INDEX idx_status_create_time (status, create_time)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务记录表';

-- ============================================================
-- 2. 任务日志表 (task_log)
-- ============================================================
DROP TABLE IF EXISTS task_log;

CREATE TABLE task_log (
    -- 主键
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID（主键，自增）',
    
    -- 关联任务
    task_id BIGINT NOT NULL COMMENT '关联的任务ID',
    
    -- 日志信息
    log_level VARCHAR(20) NOT NULL COMMENT '日志级别（INFO, WARN, ERROR, DEBUG, STDOUT, STDERR）',
    log_source VARCHAR(50) NOT NULL COMMENT '日志来源（SYSTEM, PYTHON, EXECUTOR）',
    log_content TEXT NOT NULL COMMENT '日志内容',
    log_sequence INT NULL COMMENT '日志序号（同一任务内递增）',
    
    -- 时间信息
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引
    INDEX idx_task_id (task_id),
    INDEX idx_task_id_create_time (task_id, create_time),
    INDEX idx_log_level (log_level),
    
    -- 外键约束
    FOREIGN KEY (task_id) REFERENCES task_record(id) ON DELETE CASCADE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务日志表';

-- ============================================================
-- 3. 插入测试数据（可选）
-- ============================================================

-- 插入一条测试任务记录
INSERT INTO task_record (
    task_type, task_name, status, params, create_time, update_time
) VALUES (
    'DATA_FETCH', 
    '测试数据获取：20240101-20240201', 
    'PENDING', 
    '{"startDate":"20240101","endDate":"20240201"}',
    NOW(),
    NOW()
);

-- 插入一条测试日志
INSERT INTO task_log (
    task_id, log_level, log_source, log_content, create_time
) VALUES (
    LAST_INSERT_ID(),
    'INFO',
    'SYSTEM',
    '任务已提交，等待执行',
    NOW()
);

-- ============================================================
-- 4. 查询示例
-- ============================================================

-- 查询最近 10 条任务记录
-- SELECT * FROM task_record ORDER BY create_time DESC LIMIT 10;

-- 查询正在执行的任务
-- SELECT * FROM task_record WHERE status = 'RUNNING';

-- 查询失败的任务
-- SELECT * FROM task_record WHERE status IN ('FAILED', 'TIMEOUT', 'CANCELLED');

-- 查询某个任务的所有日志
-- SELECT * FROM task_log WHERE task_id = 1 ORDER BY create_time ASC;

-- 统计各状态任务数量
-- SELECT status, COUNT(*) as count FROM task_record GROUP BY status;

-- 统计各类型任务的平均执行时长
-- SELECT task_type, AVG(duration) as avg_duration_seconds 
-- FROM task_record 
-- WHERE status = 'SUCCESS' AND duration IS NOT NULL 
-- GROUP BY task_type;

-- ============================================================
-- 5. 数据清理（生产环境谨慎使用）
-- ============================================================

-- 清理 30 天前的成功任务记录
-- DELETE FROM task_record 
-- WHERE status = 'SUCCESS' 
-- AND create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- 清理 30 天前的任务日志
-- DELETE FROM task_log 
-- WHERE create_time < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- ============================================================
-- 6. 性能优化建议
-- ============================================================

-- 如果任务量很大，建议：
-- 1. 添加分区（按月份分区）
--    ALTER TABLE task_record 
--    PARTITION BY RANGE (TO_DAYS(create_time)) (
--        PARTITION p202401 VALUES LESS THAN (TO_DAYS('2024-02-01')),
--        PARTITION p202402 VALUES LESS THAN (TO_DAYS('2024-03-01')),
--        ...
--    );

-- 2. 定期归档历史数据到历史表
--    CREATE TABLE task_record_archive LIKE task_record;
--    INSERT INTO task_record_archive SELECT * FROM task_record WHERE create_time < '2024-01-01';
--    DELETE FROM task_record WHERE create_time < '2024-01-01';

-- 3. 使用 ELK 等日志系统替代数据库存储日志

-- ============================================================
-- 说明文档
-- ============================================================

/*
数据库设计说明：

1. 表关系：
   - task_record (1) : task_log (N)
   - 一个任务可以有多条日志
   - 删除任务时，关联日志会级联删除（CASCADE）

2. 字段说明：
   - task_type：任务类型，对应 TaskTypeEnum
   - status：任务状态，对应 TaskStatusEnum
   - params：JSON 格式，存储任务参数
   - result：简短的结果描述，详细日志在 task_log 表
   - error_message：错误信息摘要，完整堆栈在 task_log 表

3. 索引策略：
   - 主要查询场景：按状态、按时间、按任务类型查询
   - 联合索引 (status, create_time) 覆盖常用查询

4. 数据清理：
   - 建议定期清理历史数据（如 30 天前的成功任务）
   - 可以归档到历史表，保留原始数据

5. 扩展性：
   - 预留 created_by 字段，支持多用户场景
   - log_sequence 字段可用于日志排序
   - 可根据需要添加更多字段（如优先级、标签等）
*/


package com.ningshenquantlab.alphaforge_demo1.quant.dao;

import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 任务日志 DAO
 */
@Mapper
public interface TaskLogDao {
    
    /**
     * 插入任务日志
     */
    @Insert("INSERT INTO task_log (task_id, log_level, log_content, log_source, create_time) " +
            "VALUES (#{taskId}, #{logLevel}, #{logContent}, #{logSource}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskLog log);
    
    /**
     * 根据任务ID查询日志列表
     */
    @Select("SELECT * FROM task_log WHERE task_id = #{taskId} ORDER BY create_time ASC")
    List<TaskLog> selectByTaskId(Long taskId);
    
    /**
     * 删除任务的所有日志
     */
    @Delete("DELETE FROM task_log WHERE task_id = #{taskId}")
    int deleteByTaskId(Long taskId);
}


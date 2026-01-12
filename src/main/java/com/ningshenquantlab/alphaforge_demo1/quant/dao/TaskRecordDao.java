package com.ningshenquantlab.alphaforge_demo1.quant.dao;

import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 任务记录 DAO
 * 
 * 说明：使用 MyBatis 注解方式实现，简化开发
 * 后续可以改为 XML 方式实现复杂查询
 */
@Mapper
public interface TaskRecordDao {
    
    /**
     * 插入任务记录
     */
    @Insert("INSERT INTO task_record (task_type, task_name, status, params, " +
            "create_time, update_time) " +
            "VALUES (#{taskType}, #{taskName}, #{status}, #{params}, " +
            "#{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskRecord record);
    
    /**
     * 根据ID查询任务记录
     */
    @Select("SELECT * FROM task_record WHERE id = #{id}")
    TaskRecord selectById(Long id);
    
    /**
     * 更新任务记录
     */
    @Update("UPDATE task_record SET status = #{status}, start_time = #{startTime}, " +
            "end_time = #{endTime}, result = #{result}, error_message = #{errorMessage}, " +
            "exit_code = #{exitCode}, duration = #{duration}, update_time = #{updateTime} " +
            "WHERE id = #{id}")
    int update(TaskRecord record);
    
    /**
     * 查询所有任务记录（按创建时间倒序）
     */
    @Select("SELECT * FROM task_record ORDER BY create_time DESC LIMIT 100")
    List<TaskRecord> selectAll();
    
    /**
     * 根据状态查询任务记录
     */
    @Select("SELECT * FROM task_record WHERE status = #{status} ORDER BY create_time DESC")
    List<TaskRecord> selectByStatus(String status);
    
    /**
     * 根据任务类型查询任务记录
     */
    @Select("SELECT * FROM task_record WHERE task_type = #{taskType} ORDER BY create_time DESC LIMIT 100")
    List<TaskRecord> selectByTaskType(String taskType);
    
    /**
     * 根据任务类型和状态查询任务记录
     */
    @Select("SELECT * FROM task_record WHERE task_type = #{taskType} AND status = #{status} ORDER BY create_time DESC")
    List<TaskRecord> selectByTaskTypeAndStatus(@Param("taskType") String taskType, @Param("status") String status);
}


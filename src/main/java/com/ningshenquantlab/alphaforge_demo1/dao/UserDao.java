package com.ningshenquantlab.alphaforge_demo1.dao;

import com.ningshenquantlab.alphaforge_demo1.entity.User;
import java.util.List;

/**
 * 用户数据访问层接口
 * 负责与数据库直接交互，执行 SQL 语句
 * 
 * 职责：
 * - 定义所有数据库操作方法
 * - 不包含业务逻辑，只负责数据的 CRUD
 * - 由 UserDaoImpl 实现具体的 SQL 操作
 */
public interface UserDao {
    
    /**
     * 根据客户ID查询用户
     * @param custId 客户ID
     * @return 用户对象，如果不存在返回 null
     */
    User findById(String custId);
    
    /**
     * 查询所有用户（分页）
     * @param offset 偏移量（从第几条开始）
     * @param limit 每页数量
     * @return 用户列表
     */
    List<User> findAll(Integer offset, Integer limit);
    
    /**
     * 搜索用户（根据昵称模糊查询）
     * @param keyword 搜索关键词
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 符合条件的用户列表
     */
    List<User> search(String keyword, Integer offset, Integer limit);
    
    /**
     * 统计用户总数
     * @return 用户总数
     */
    Long count();
    
    /**
     * 统计搜索结果总数
     * @param keyword 搜索关键词
     * @return 符合条件的用户总数
     */
    Long countBySearch(String keyword);
    
    /**
     * 创建用户
     * @param user 用户对象
     * @return 受影响的行数
     */
    int insert(User user);
    
    /**
     * 更新用户
     * @param user 用户对象（必须包含 custId）
     * @return 受影响的行数
     */
    int update(User user);
    
    /**
     * 删除用户
     * @param custId 客户ID
     * @return 受影响的行数
     */
    int deleteById(String custId);
    
    /**
     * 批量删除用户
     * @param custIds 客户ID列表
     * @return 受影响的行数
     */
    int batchDelete(List<String> custIds);
    
    /**
     * 检查客户ID是否存在
     * @param custId 客户ID
     * @return true 存在，false 不存在
     */
    boolean existsById(String custId);
}

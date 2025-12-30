package com.ningshenquantlab.alphaforge_demo1.customer.service;

import com.ningshenquantlab.alphaforge_demo1.customer.entity.User;
import java.util.List;

/**
 * 用户业务逻辑层接口
 * 负责处理业务逻辑，协调 DAO 层完成复杂操作
 * 
 * 职责：
 * - 定义业务方法
 * - 处理业务规则和逻辑
 * - 事务管理（通过 @Transactional）
 * - 异常处理和转换
 * 
 * Service 层和 DAO 层的区别：
 * - DAO：只负责数据库操作，一个方法对应一条 SQL
 * - Service：负责业务逻辑，可能调用多个 DAO 方法，包含业务规则
 */
public interface UserService {
    
    /**
     * 根据客户ID查询用户
     * @param custId 客户ID
     * @return 用户对象
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException 用户不存在时抛出
     */
    User findById(Long custId);
    
    /**
     * 查询所有用户（分页）
     * @param page 页码（从 1 开始）
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> findAll(Integer page, Integer size);
    
    /**
     * 搜索用户（根据昵称模糊查询）
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页数量
     * @return 符合条件的用户列表
     */
    List<User> search(String keyword, Integer page, Integer size);
    
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
     * @return 创建后的用户对象
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.BusinessException 客户ID已存在时抛出
     */
    User save(User user);
    
    /**
     * 更新用户
     * @param user 用户对象（必须包含 custId）
     * @return 更新后的用户对象
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException 用户不存在时抛出
     */
    User update(User user);
    
    /**
     * 删除用户
     * @param custId 客户ID
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException 用户不存在时抛出
     */
    void deleteById(Long custId);
    
    /**
     * 批量删除用户
     * @param custIds 客户ID列表
     * @return 实际删除的数量
     */
    int batchDelete(List<Long> custIds);
}


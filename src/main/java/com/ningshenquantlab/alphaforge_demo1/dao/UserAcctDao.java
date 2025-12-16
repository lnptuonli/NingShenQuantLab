package com.ningshenquantlab.alphaforge_demo1.dao;

import com.ningshenquantlab.alphaforge_demo1.entity.UserAcct;

import java.util.List;

public interface UserAcctDao {
    /**
     * 根据客户ID查询用户
     * @param custId 客户ID
     * @return 用户对象，如果不存在返回 null
     */
    UserAcct findById(Long custId);

    /**
     * 查询所有用户（分页）
     * @param offset 偏移量（从第几条开始）
     * @param limit 每页数量
     * @return 用户列表
     */
    List<UserAcct> findAll(Integer offset, Integer limit);

    /**
     * 搜索用户（根据昵称模糊查询）
     * @param keyword 搜索关键词
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 符合条件的用户列表
     */
    List<UserAcct> search(String keyword, Integer offset, Integer limit);

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
    int insert(UserAcct user);

    /**
     * 更新用户
     * @param user 用户对象（必须包含 custId）
     * @return 受影响的行数
     */
    int update(UserAcct user);

    /**
     * 删除用户
     * @param custId 客户ID
     * @return 受影响的行数
     */
    int deleteById(Long custId);

    /**
     * 批量删除用户
     * @param custIds 客户ID列表
     * @return 受影响的行数
     */
    int batchDelete(List<Long> custIds);

    /**
     * 检查客户ID是否存在
     * @param custId 客户ID
     * @return true 存在，false 不存在
     */
    boolean existsById(Long custId);
}

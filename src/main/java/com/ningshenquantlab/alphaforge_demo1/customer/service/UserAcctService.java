package com.ningshenquantlab.alphaforge_demo1.customer.service;

import com.ningshenquantlab.alphaforge_demo1.customer.entity.UserAcct;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserAcctService {
    /**
     * 根据客户ID查询用户
     * @param custId 客户ID
     * @return 用户对象
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException 用户不存在时抛出
     */
    UserAcct findByCustId(Long custId);

    /**
     * 查询所有用户（分页）
     * @param page 页码（从 1 开始）
     * @param size 每页数量
     * @return 用户列表
     */
    List<UserAcct> findPage(Integer page, Integer size);
    List<UserAcct> findAll();
    /**
     * 搜索用户（根据昵称模糊查询）
     * @param keyword 搜索关键词
     * @param page 页数
     * @param size 每页数量
     * @return 符合条件的用户列表
     */
    List<UserAcct> search(@Param("keyword") String keyword, @Param("page") Integer page, @Param("size") Integer size);
    /**
     * 统计搜索结果总数
     * @param keyword 搜索关键词
     * @return 符合条件的用户总数
     */
    Long countBySearch(String keyword);
    /**
     * 统计用户总数
     * @return 用户总数
     */
    Long count();

    /**
     * 创建用户
     * @param userAcct 用户对象
     * @return 创建后的用户对象
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.BusinessException 客户ID已存在时抛出
     */
    UserAcct save(UserAcct userAcct);

    /**
     * 更新用户
     * @param userAcct 用户对象（必须包含 custId）
     * @return 更新后的用户对象
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException 用户不存在时抛出
     */
    UserAcct update(UserAcct userAcct);

    /**
     * 删除用户
     * @param custId 客户ID
     * @throws com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException 用户不存在时抛出
     */
    void deleteByCustId(Long custId);

    /**
     * 批量删除用户
     * @param custIds 客户ID列表
     * @return 实际删除的数量
     */
    int batchDelete(@Param("custIds") List<Long> custIds);


}


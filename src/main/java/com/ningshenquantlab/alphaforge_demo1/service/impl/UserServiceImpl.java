package com.ningshenquantlab.alphaforge_demo1.service.impl;

import com.ningshenquantlab.alphaforge_demo1.dao.UserDao;
import com.ningshenquantlab.alphaforge_demo1.entity.User;
import com.ningshenquantlab.alphaforge_demo1.exception.BusinessException;
import com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException;
import com.ningshenquantlab.alphaforge_demo1.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户业务逻辑层实现类
 * 
 * 职责：
 * 1. 实现业务逻辑（参数校验、业务规则、数据转换等）
 * 2. 调用 DAO 层完成数据操作
 * 3. 处理异常，转换为业务异常
 * 4. 管理事务（使用 @Transactional）
 * 
 * 注解说明：
 * - @Service：标记为业务逻辑层组件，由 Spring 管理
 * - @Slf4j：Lombok 注解，自动生成日志对象 log
 * - @Transactional：声明式事务，方法执行失败时自动回滚
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    
    /**
     * 依赖注入：Service 需要用到 DAO
     * 通过构造器注入（推荐方式，便于测试和保证不可变性）
     */
    private final UserDao userDao;

    /**
     * 构造器注入
     * Spring 4.3+ 单一构造器的 @Autowired 可以省略
     */
    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * 根据客户ID查询用户
     * 
     * 业务逻辑：
     * 1. 参数校验（Controller 已通过 @Validated 校验，这里可选）
     * 2. 调用 DAO 查询
     * 3. 如果不存在，抛出 ResourceNotFoundException
     * 
     * @param custId 客户ID
     * @return 用户对象
     * @throws ResourceNotFoundException 用户不存在时抛出
     */
    @Override
    public User findById(String custId) {
        log.debug("Service: 查询用户, custId={}", custId);
        
        // 调用 DAO 查询
        User user = userDao.findById(custId);
        
        // 业务规则：用户不存在时抛出异常
        if (user == null) {
            log.warn("用户不存在: custId={}", custId);
            throw new ResourceNotFoundException("用户不存在: custId=" + custId);
        }
        
        return user;
    }

    /**
     * 查询所有用户（分页）
     * 
     * 业务逻辑：
     * 1. 将页码转换为数据库的 offset
     * 2. 调用 DAO 查询
     * 
     * 分页计算：
     * - page = 1, size = 10 → offset = 0, limit = 10（第1-10条）
     * - page = 2, size = 10 → offset = 10, limit = 10（第11-20条）
     * - 公式：offset = (page - 1) * size
     */
    @Override
    public List<User> findAll(Integer page, Integer size) {
        log.debug("Service: 查询用户列表, page={}, size={}", page, size);
        
        // 计算 offset
        int offset = (page - 1) * size;
        
        return userDao.findAll(offset, size);
    }

    /**
     * 搜索用户（模糊查询）
     */
    @Override
    public List<User> search(String keyword, Integer page, Integer size) {
        log.debug("Service: 搜索用户, keyword={}, page={}, size={}", keyword, page, size);
        
        int offset = (page - 1) * size;
        
        return userDao.search(keyword, offset, size);
    }

    /**
     * 统计用户总数
     */
    @Override
    public Long count() {
        return userDao.count();
    }

    /**
     * 统计搜索结果总数
     */
    @Override
    public Long countBySearch(String keyword) {
        return userDao.countBySearch(keyword);
    }

    /**
     * 创建用户
     * 
     * 业务逻辑：
     * 1. 检查客户ID是否已存在（业务规则：ID不能重复）
     * 2. 调用 DAO 插入数据
     * 3. 返回创建后的用户
     * 
     * @Transactional：声明式事务
     * - 方法执行成功：自动提交事务
     * - 方法抛出异常：自动回滚事务
     * - rollbackFor：指定哪些异常需要回滚（默认：RuntimeException 和 Error）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public User save(User user) {
        log.info("Service: 创建用户, user={}", user);
        
        // 业务规则1：检查客户ID是否已存在
        if (userDao.existsById(user.getCustId())) {
            log.warn("客户ID已存在: custId={}", user.getCustId());
            throw new BusinessException("客户ID已存在: " + user.getCustId());
        }
        
        // 调用 DAO 插入数据
        int rows = userDao.insert(user);
        
        // 检查插入结果
        if (rows == 0) {
            log.error("创建用户失败: user={}", user);
            throw new BusinessException("创建用户失败");
        }
        
        log.info("创建用户成功: custId={}", user.getCustId());
        
        // 返回创建后的用户（重新查询，获取完整数据）
        return userDao.findById(user.getCustId());
    }

    /**
     * 更新用户
     * 
     * 业务逻辑：
     * 1. 检查用户是否存在
     * 2. 调用 DAO 更新数据
     * 3. 返回更新后的用户
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public User update(User user) {
        log.info("Service: 更新用户, user={}", user);
        
        // 业务规则1：检查用户是否存在
        if (!userDao.existsById(user.getCustId())) {
            log.warn("用户不存在: custId={}", user.getCustId());
            throw new ResourceNotFoundException("用户不存在: custId=" + user.getCustId());
        }
        
        // 调用 DAO 更新数据
        int rows = userDao.update(user);
        
        // 检查更新结果
        if (rows == 0) {
            log.error("更新用户失败: user={}", user);
            throw new BusinessException("更新用户失败");
        }
        
        log.info("更新用户成功: custId={}", user.getCustId());
        
        // 返回更新后的用户
        return userDao.findById(user.getCustId());
    }

    /**
     * 删除用户
     * 
     * 业务逻辑：
     * 1. 检查用户是否存在
     * 2. 调用 DAO 删除数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteById(String custId) {
        log.info("Service: 删除用户, custId={}", custId);
        
        // 业务规则1：检查用户是否存在
        if (!userDao.existsById(custId)) {
            log.warn("用户不存在: custId={}", custId);
            throw new ResourceNotFoundException("用户不存在: custId=" + custId);
        }
        
        // 调用 DAO 删除数据
        int rows = userDao.deleteById(custId);
        
        if (rows == 0) {
            log.error("删除用户失败: custId={}", custId);
            throw new BusinessException("删除用户失败");
        }
        
        log.info("删除用户成功: custId={}", custId);
    }

    /**
     * 批量删除用户
     * 
     * 业务逻辑：
     * 1. 过滤不存在的用户ID
     * 2. 调用 DAO 批量删除
     * 3. 返回实际删除的数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int batchDelete(List<String> custIds) {
        log.info("Service: 批量删除用户, custIds={}", custIds);
        
        if (custIds == null || custIds.isEmpty()) {
            log.warn("批量删除: 用户ID列表为空");
            return 0;
        }
        
        // 调用 DAO 批量删除
        int deletedCount = userDao.batchDelete(custIds);
        
        log.info("批量删除成功: 删除了 {} 个用户", deletedCount);
        
        return deletedCount;
    }
}

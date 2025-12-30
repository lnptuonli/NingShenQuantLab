package com.ningshenquantlab.alphaforge_demo1.customer.service.impl;

import com.ningshenquantlab.alphaforge_demo1.customer.entity.UserAcct;
import com.ningshenquantlab.alphaforge_demo1.customer.mapper.UserAcctMapper;
import com.ningshenquantlab.alphaforge_demo1.customer.service.UserAcctService;
import com.ningshenquantlab.alphaforge_demo1.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class UserAcctServiceImpl implements UserAcctService {
    @Autowired
    private UserAcctMapper userAcctMapper;

    /**
     * 根据 ID 查询用户
     */
    @Override
    public UserAcct findByCustId(Long custId) {
        UserAcct userAcct = userAcctMapper.selectByCustId(custId);
        if (userAcct == null) {
            throw new ResourceNotFoundException("用户不存在：ID = " + custId);
        }
        return userAcct;
    }
    /**
     * 根据用户名查询用户
     */
    public UserAcct findByCustName(String custName) {
        return userAcctMapper.selectByCustName(custName);
    }

    /**
     * 查询所有用户
     */
    @Override
    public List<UserAcct> findAll() {
        return userAcctMapper.selectAll();
    }

    @Override
    public List<UserAcct> search(String keyword, Integer page, Integer size) {
        log.debug("Service: 搜索用户, keyword={}, page={}, size={}", keyword, page, size);

        int offset = (page - 1) * size;
        return userAcctMapper.search(keyword, offset, size);
    }

    @Override
    public Long countBySearch(String keyword) {
        return userAcctMapper.countBySearch(keyword);
    }

    /**
     * 分页查询用户
     */
    @Override
    public List<UserAcct> findPage(Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        return userAcctMapper.selectPage(offset, size);
    }

    /**
     * 查询用户总数
     */
    @Override
    public Long count() {
        return userAcctMapper.countAll();
    }

    /**
     * 保存用户
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAcct save(UserAcct userAcct) {
        // 检查用户名是否重复
        UserAcct existingUser = userAcctMapper.selectByCustName(userAcct.getCustName());
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 插入用户
        userAcctMapper.insert(userAcct);
        log.info("用户创建成功：CUST_ID = {}", userAcct.getCustId());

        return userAcct;
    }

    /**
     * 更新用户
     */
    @Transactional(rollbackFor = Exception.class)
    public UserAcct update(UserAcct userAcct) {
        // 检查用户是否存在
        findByCustId(userAcct.getCustId());

        // 更新用户
        int rows = userAcctMapper.update(userAcct);
        if (rows == 0) {
            throw new RuntimeException("更新失败");
        }

        log.info("用户更新成功：ID = {}", userAcct.getId());
        return findByCustId(userAcct.getId());
    }

    /**
     * 删除用户
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCustId(Long custId) {
        // 检查用户是否存在
        findByCustId(custId);

        // 删除用户
        int rows = userAcctMapper.deleteByCustId(custId);
        if (rows == 0) {
            throw new RuntimeException("删除失败");
        }

        log.info("用户删除成功：ID = {}", custId);
    }

    @Override
    public int batchDelete(List<Long> custIds) {
        int rows = userAcctMapper.batchDelete(custIds);
        if (rows == 0) {
            throw new RuntimeException("删除失败");
        }

        log.info("用户删除成功：共 = {}个", rows);
        return rows;
    }
}


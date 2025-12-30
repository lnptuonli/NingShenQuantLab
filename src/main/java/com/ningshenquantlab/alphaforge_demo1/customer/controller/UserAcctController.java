package com.ningshenquantlab.alphaforge_demo1.customer.controller;

import com.ningshenquantlab.alphaforge_demo1.common.PageResult;
import com.ningshenquantlab.alphaforge_demo1.common.Result;
import com.ningshenquantlab.alphaforge_demo1.customer.entity.UserAcct;
import com.ningshenquantlab.alphaforge_demo1.customer.service.UserAcctService;
import com.ningshenquantlab.alphaforge_demo1.customer.validation.CreateGroup;
import com.ningshenquantlab.alphaforge_demo1.customer.validation.UpdateGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/v1/userAccts")
public class UserAcctController {
    @Autowired
    private UserAcctService userAcctService;
    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    public Result<PageResult<UserAcct>> getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<UserAcct> userAccts = userAcctService.findPage(page, size);
        Long total = userAcctService.count();

        PageResult<UserAcct> pageResult = new PageResult<>(userAccts, total, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取单个用户
     */
    @GetMapping("/{cust_id}")
    public Result<UserAcct> getUser(@PathVariable Long cust_id) {
        UserAcct userAcct = userAcctService.findByCustId(cust_id);
        return Result.success(userAcct);
    }

    /**
     * 创建用户
     */
    @PostMapping
    public Result<UserAcct> createUser(@Validated(CreateGroup.class) @RequestBody UserAcct userAcct) {
        UserAcct savedUser = userAcctService.save(userAcct);
        return Result.success("创建成功", savedUser);
    }
    /**
     * 更新用户
     */
    @PutMapping("/{cust_id}")
    public Result<UserAcct> updateUser(
            @PathVariable("cust_id") Long custId,
            @Validated(UpdateGroup.class) @RequestBody UserAcct userAcct
    ) {
        userAcct.setId(custId);
        UserAcct updatedUser = userAcctService.update(userAcct);
        return Result.success("更新成功", updatedUser);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{cust_id}")
    public Result<String> deleteUser(@PathVariable("cust_id") Long custId) {
        userAcctService.deleteByCustId(custId);
        return Result.success("删除成功");
    }

    /**
     * 获取所有用户（不分页）
     */
    @GetMapping("/all")
    public Result<List<UserAcct>> getAllUsers() {
        List<UserAcct> userAccts = userAcctService.findAll();
        return Result.success(userAccts);
    }

    /**
     * 搜索用户（分页）
     */
    @GetMapping("/search")
    public Result<PageResult<UserAcct>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        List<UserAcct> userAccts = userAcctService.search(keyword, page, size);
        Long total = userAcctService.countBySearch(keyword);
        PageResult<UserAcct> pageResult = new PageResult<>(userAccts, total, page, size);
        return Result.success(pageResult);
    }

    /**
     * 批量删除用户
     */    @DeleteMapping("/batch")
    public Result<String> batchDelete(@RequestBody List<Long> custIds) {
        int deletedCount = userAcctService.batchDelete(custIds);
        return Result.success("批量删除成功，共删除 " + deletedCount + " 条记录");
    }
}


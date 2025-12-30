package com.ningshenquantlab.alphaforge_demo1.customer.controller;

import com.ningshenquantlab.alphaforge_demo1.common.PageResult;
import com.ningshenquantlab.alphaforge_demo1.common.Result;
import com.ningshenquantlab.alphaforge_demo1.customer.entity.User;
import com.ningshenquantlab.alphaforge_demo1.customer.service.UserService;
import com.ningshenquantlab.alphaforge_demo1.customer.validation.CreateGroup;
import com.ningshenquantlab.alphaforge_demo1.customer.validation.UpdateGroup;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 * 负责处理用户相关的 HTTP 请求
 * 
 * 职责：
 * 1. 接收 HTTP 请求，提取参数
 * 2. 参数校验（通过注解）
 * 3. 调用 Service 层处理业务逻辑
 * 4. 封装返回结果（统一使用 Result 类）
 * 5. 异常由 GlobalExceptionHandler 统一处理
 * 
 * RESTful API 设计规范：
 * - GET /users           : 查询列表
 * - GET /users/{id}      : 查询单个
 * - POST /users          : 创建
 * - PUT /users/{id}      : 更新
 * - DELETE /users/{id}   : 删除
 * 
 * 注解说明：
 * - @RestController: 标记为控制器，返回 JSON 数据
 * - @RequestMapping: 指定基础路径
 * - @Validated: 开启方法参数校验
 * - @Slf4j: Lombok 注解，自动生成日志对象
 */
@Slf4j  // Lombok：自动生成日志对象
@Validated  // 开启方法参数校验（用于 @PathVariable、@RequestParam 等）
@RestController  // 标记为控制器，返回 JSON 数据
@RequestMapping("/api/v1/users")  // 基础路径：/api/v1/users
public class UserController {
    
    /**
     * 依赖注入：Controller 需要调用 Service 处理业务逻辑
     * 通过构造器注入（推荐方式）
     */
    private final UserService userService;

    /**
     * 构造器注入
     * Spring 4.3+ 单一构造器的 @Autowired 可以省略
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户列表（分页）
     * 
     * 请求示例：
     * - GET /api/v1/users?page=1&size=10
     * - GET /api/v1/users?page=2&size=20&search=张三
     * 
     * 参数说明：
     * - page: 页码（从 1 开始），默认 1
     * - size: 每页数量，默认 10
     * - search: 搜索关键词（可选），模糊匹配昵称
     * 
     * 返回格式：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "list": [...],      // 用户列表
     *     "total": 100,       // 总记录数
     *     "page": 1,          // 当前页
     *     "size": 10,         // 每页大小
     *     "totalPages": 10    // 总页数
     *   }
     * }
     */
    @GetMapping
    public Result<PageResult<User>> getUsers(
            //RequestParam的defaultValue属性自己是有默认值的，是一大串无意义的制表符和特殊字符，他认为用户不可能写得出这种字符串
            //参数绑定时，优先取name="page"这个属性，如果没写，则会取参数名，即page、size、search
            @RequestParam(name="page",defaultValue = "1") @Min(value = 1, message = "页码必须大于0") Integer page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "每页数量必须大于0") Integer size,
            @RequestParam(required = false) String search
    ) {
        log.info("Controller: 查询用户列表, page={}, size={}, search={}", page, size, search);

        // 根据是否有搜索关键词，调用不同的 Service 方法
        List<User> users;
        Long total;
        
        if (search != null && !search.trim().isEmpty()) {//非空判断trim一下，万一全是空格呢
            // 有搜索关键词：模糊查询
            users = userService.search(search, page, size);
            total = userService.countBySearch(search);
        } else {
            // 无搜索关键词：查询所有
            users = userService.findAll(page, size);
            total = userService.count();
        }

        // 封装分页结果
        PageResult<User> pageResult = new PageResult<>(users, total, page, size);
        
        // 返回统一格式
        return Result.success(pageResult);
    }
    /**
     * 获取单个用户
     * 
     * 请求示例：
     * - GET /api/v1/users/100372
     * 
     * 路径参数：
     * - custId: 客户ID
     * 
     * 返回格式：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "id": 1,
     *     "custId": "100372",
     *     "custName": "张三",
     *     "custKey": "***"
     *   }
     * }
     */
    @GetMapping("/{custId}")
    public Result<User> getUser(
            @PathVariable @NotNull(message = "客户ID不能为空") Long custId
    ) {
        log.info("Controller: 查询用户, custId={}", custId);
        
        // 调用 Service 查询用户
        User user = userService.findById(custId);
        
        // 返回成功结果
        return Result.success(user);
    }

    /**
     * 创建用户
     * 
     * 请求示例：
     * POST /api/v1/users
     * Content-Type: application/json
     * 
     * {
     *   "custId": "100375",
     *   "custName": "李四",
     *   "custKey": "Abc@1234"
     * }
     * 
     * 校验规则（CreateGroup）：
     * - custId: 必填，正数
     * - custName: 必填，2-50字符
     * - custKey: 必填，8-50字符，包含数字、字母、特殊符号
     * 
     * 返回格式：
     * {
     *   "code": 200,
     *   "message": "创建成功",
     *   "data": { ... }
     * }
     */
    @PostMapping
    public Result<User> createUser(
            @RequestBody @Validated(CreateGroup.class) User user
    ) {
        log.info("Controller: 创建用户, user={}", user);
        
        // 调用 Service 创建用户
        User savedUser = userService.save(user);
        
        // 返回成功结果
        return Result.success("创建成功", savedUser);
    }

    /**
     * 更新用户（完整更新）
     * 
     * 请求示例：
     * PUT /api/v1/users/100372
     * Content-Type: application/json
     * 
     * {
     *   "custName": "张三（修改）",
     *   "custKey": "NewPass@123"
     * }
     * 
     * 校验规则（UpdateGroup）：
     * - custName: 必填，2-50字符
     * - custKey: 选填，如果提供则必须8-50字符
     */
    @PutMapping("/{custId}")
    public Result<User> updateUser(
            @PathVariable @NotNull(message = "客户ID不能为空") Long custId,
            @RequestBody @Validated(UpdateGroup.class) User user
    ) {
        log.info("Controller: 更新用户, custId={}, user={}", custId, user);
        
        // 将路径参数的 custId 设置到用户对象中
        user.setCustId(custId);
        
        // 调用 Service 更新用户
        User updatedUser = userService.update(user);
        
        // 返回成功结果
        return Result.success("更新成功", updatedUser);
    }

    /**
     * 删除用户
     * 
     * 请求示例：
     * DELETE /api/v1/users/100372
     * 
     * 路径参数：
     * - custId: 客户ID
     */
    @DeleteMapping("/{custId}")
    public Result<Void> deleteUser(
            @PathVariable @NotNull(message = "客户ID不能为空") Long custId
    ) {
        log.info("Controller: 删除用户, custId={}", custId);
        
        // 调用 Service 删除用户
        userService.deleteById(custId);
        
        // 返回成功结果（无数据）
        Result<Void> result = Result.success();
        result.setMessage("删除成功");
        return result;
    }

    /**
     * 批量删除用户
     * 
     * 请求示例：
     * DELETE /api/v1/users?custIds=100372,100373,100374
     * 
     * 查询参数：
     * - custIds: 客户ID列表（逗号分隔）
     */
    @DeleteMapping
    public Result<Void> batchDelete(
            @RequestParam @NotNull(message = "客户ID列表不能为空") List<Long> custIds
    ) {
        log.info("Controller: 批量删除用户, custIds={}", custIds);
        
        // 调用 Service 批量删除
        int deletedCount = userService.batchDelete(custIds);
        
        // 返回成功结果
        Result<Void> result = Result.success();
        result.setMessage("批量删除成功，删除了 " + deletedCount + " 个用户");
        return result;
    }
}


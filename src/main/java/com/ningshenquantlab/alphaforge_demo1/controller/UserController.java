package com.ningshenquantlab.alphaforge_demo1.controller;

import com.ningshenquantlab.alphaforge_demo1.entity.User;
import com.ningshenquantlab.alphaforge_demo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController = @Controller + @ResponseBody
// 表示这个类是一个控制器，所有方法返回的都是数据（JSON），而不是页面
@RestController
// @RequestMapping：指定这个 Controller 的基础路径
// 所有方法的路径都会加上 /user 前缀
@RequestMapping("/user")
public class UserController {
    // 依赖注入：Controller 需要用到 Service
    private final UserService userService;
    //构造器注入
    @Autowired
    public UserController(UserService userService){
        this.userService=userService;
    }
    // @GetMapping：处理 GET 请求
    // /{id}：路径参数，例如 /user/123，id = 123
    // @PathVariable：把路径参数绑定到方法参数
    @GetMapping("/{cust_id}")
    public User getUser(@PathVariable Long cust_id) {
        // 调用 Service 获取用户
        return userService.getUserById(cust_id);
        // Spring Boot 会自动把 User 对象转成 JSON 返回
    }
}

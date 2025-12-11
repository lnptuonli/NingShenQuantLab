package com.ningshenquantlab.alphaforge_demo1.service.impl;

import com.ningshenquantlab.alphaforge_demo1.dao.UserDao;
import com.ningshenquantlab.alphaforge_demo1.entity.User;
import com.ningshenquantlab.alphaforge_demo1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    // 依赖注入：Service 需要用到 Dao
    private final UserDao userDao;
    // 构造器注入（推荐）
    // 当只有一个构造器时，@Autowired 可以省略
    @Autowired
    public UserServiceImpl(UserDao userDao){
        this.userDao = userDao;
    }

    @Override
    public User getUserById(Long id) {
        // 这里可以添加业务逻辑
        if(id==null){
            System.out.println("check if id is null");
            id=100372L;
        }
        User user=userDao.findById(id);
        return user;
    }

}

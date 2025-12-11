package com.ningshenquantlab.alphaforge_demo1.dao;

import com.ningshenquantlab.alphaforge_demo1.entity.User;
// Dao 接口：定义数据访问的方法（增删改查）
public interface UserDao {
    User findById(Long id);
}

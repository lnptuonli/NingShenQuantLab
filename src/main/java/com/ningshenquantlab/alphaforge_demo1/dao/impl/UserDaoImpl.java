package com.ningshenquantlab.alphaforge_demo1.dao.impl;

import com.ningshenquantlab.alphaforge_demo1.dao.UserDao;
import com.ningshenquantlab.alphaforge_demo1.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl implements UserDao{
    private final JdbcTemplate jdbcTemplate;
    //jdbcTemplate会在运行时自动创建，只是检查会飘红
    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User findById(Long cust_id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM user WHERE cust_id = ?",
                new BeanPropertyRowMapper<>(User.class),
                cust_id
        );
    }
}

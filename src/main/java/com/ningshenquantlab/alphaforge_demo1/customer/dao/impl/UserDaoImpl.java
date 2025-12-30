package com.ningshenquantlab.alphaforge_demo1.customer.dao.impl;

import com.ningshenquantlab.alphaforge_demo1.customer.dao.UserDao;
import com.ningshenquantlab.alphaforge_demo1.customer.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问层实现类
 * 使用 JdbcTemplate 执行 SQL 语句
 * 
 * 注意事项：
 * 1. @Repository 注解：标记为数据访问层组件，由 Spring 管理
 * 2. BeanPropertyRowMapper：自动将数据库字段映射到 Java 对象
 *    - 数据库：cust_id, cust_name, cust_key
 *    - Java：custId, custName, custKey
 *    - 自动完成下划线和驼峰的转换
 * 3. EmptyResultDataAccessException：查询不到数据时的异常处理
 */
@Slf4j  // Lombok：自动生成日志对象 log
@Repository  // 标记为 DAO 层组件
public class UserDaoImpl implements UserDao {
    
    /**
     * JdbcTemplate：Spring 提供的 JDBC 操作模板类
     * 简化了 JDBC 的使用，自动管理连接、异常处理等
     * 由 Spring Boot 自动配置，直接注入即可使用
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造器注入（推荐方式）
     * Spring 4.3+ 版本，单一构造器的 @Autowired 可以省略
     * 
     * @param jdbcTemplate Spring 自动创建的 JdbcTemplate Bean
     */
    @Autowired
    public UserDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 根据客户ID查询用户
     * 
     * SQL 说明：
     * - SELECT * FROM user WHERE cust_id = ? 
     * - ? 是占位符，防止 SQL 注入
     * - BeanPropertyRowMapper 自动映射查询结果到 User 对象
     * 
     * 异常处理：
     * - 如果查询不到数据，queryForObject 会抛出 EmptyResultDataAccessException
     * - 这里捕获后返回 null，由上层业务逻辑处理
     */
    //这是springJDBC的风格，在Mybatis中，sql会直接写在xml里面
    @Override
    public User findById(Long custId) {
        try {
            String sql = "SELECT * FROM user WHERE cust_id = ?";
            return jdbcTemplate.queryForObject(
                sql,
                new BeanPropertyRowMapper<>(User.class),  // 自动映射
                custId
            );
        } catch (EmptyResultDataAccessException e) {
            // 查询不到数据时返回 null，而不是抛出异常
            log.debug("用户不存在: custId={}", custId);
            return null;
        }
    }

    /**
     * 查询所有用户（分页）
     * 
     * SQL 说明：
     * - LIMIT ? OFFSET ? 用于分页查询
     * - LIMIT：每页显示的数量
     * - OFFSET：跳过的记录数
     * 
     * 示例：第2页，每页10条
     * - page = 2, size = 10
     * - offset = (2-1) * 10 = 10
     * - LIMIT 10 OFFSET 10（跳过前10条，取10条）
     */
    @Override
    public List<User> findAll(Integer offset, Integer limit) {
        String sql = "SELECT * FROM user ORDER BY id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(
            sql,
            new BeanPropertyRowMapper<>(User.class),
            limit,
            offset
        );
    }

    /**
     * 搜索用户（模糊查询）
     * 
     * SQL 说明：
     * - LIKE ? 用于模糊查询
     * - %keyword% 表示包含关键词即可
     * - CONCAT('%', ?, '%') 在 SQL 中拼接通配符
     */
    @Override
    public List<User> search(String keyword, Integer offset, Integer limit) {
        String sql = "SELECT * FROM user WHERE cust_name LIKE CONCAT('%', ?, '%') " +
                     "ORDER BY id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(
            sql,
            new BeanPropertyRowMapper<>(User.class),
            keyword,
            limit,
            offset
        );
    }

    /**
     * 统计用户总数
     * 
     * SQL 说明：
     * - COUNT(*) 统计总记录数
     * - queryForObject(..., Long.class) 将结果转为 Long 类型
     */
    @Override
    public Long count() {
        String sql = "SELECT COUNT(*) FROM user";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    /**
     * 统计搜索结果总数
     */
    @Override
    public Long countBySearch(String keyword) {
        String sql = "SELECT COUNT(*) FROM user WHERE cust_name LIKE CONCAT('%', ?, '%')";
        return jdbcTemplate.queryForObject(sql, Long.class, keyword);
    }

    /**
     * 创建用户
     * 
     * SQL 说明：
     * - INSERT INTO 插入数据
     * - (cust_id, cust_name, cust_key) 指定要插入的列
     * - VALUES (?, ?, ?) 使用占位符防止 SQL 注入
     * - update() 方法用于执行增删改操作，返回受影响的行数
     */
    @Override
    public int insert(User user) {
        String sql = "INSERT INTO user (cust_id, cust_name, cust_key) VALUES (?, ?, ?)";
        return jdbcTemplate.update(
            sql,
            user.getCustId(),
            user.getCustName(),
            user.getCustKey()
        );
    }

    /**
     * 更新用户
     * 
     * SQL 说明：
     * - UPDATE user SET ... WHERE cust_id = ?
     * - 只更新非 ID 字段
     * - 根据 cust_id 定位要更新的记录
     */
    @Override
    public int update(User user) {
        String sql = "UPDATE user SET cust_name = ?, cust_key = ? WHERE cust_id = ?";
        return jdbcTemplate.update(
            sql,
            user.getCustName(),
            user.getCustKey(),
            user.getCustId()
        );
    }

    /**
     * 删除用户
     * 
     * SQL 说明：
     * - DELETE FROM user WHERE cust_id = ?
     * - 根据 cust_id 删除记录
     */
    @Override
    public int deleteById(Long custId) {
        String sql = "DELETE FROM user WHERE cust_id = ?";
        return jdbcTemplate.update(sql, custId);
    }

    /**
     * 批量删除用户
     * 
     * SQL 说明：
     * - DELETE FROM user WHERE cust_id IN (?, ?, ?)
     * - IN 子句用于匹配多个值
     * - 需要动态生成占位符（根据 custIds 的数量）
     * 
     * 示例：custIds = [1, 2, 3]
     * SQL: DELETE FROM user WHERE cust_id IN (?, ?, ?)
     * 参数: [1, 2, 3]
     */
    @Override
    public int batchDelete(List<Long> custIds) {
        if (custIds == null || custIds.isEmpty()) {
            return 0;  // 没有要删除的数据
        }
        
        // 动态生成占位符：?, ?, ?
        String placeholders = String.join(",", 
            custIds.stream().map(id -> "?").toArray(String[]::new)
        );
        
        String sql = "DELETE FROM user WHERE cust_id IN (" + placeholders + ")";
        return jdbcTemplate.update(sql, custIds.toArray());
    }

    /**
     * 检查客户ID是否存在
     * 
     * SQL 说明：
     * - SELECT COUNT(*) FROM user WHERE cust_id = ?
     * - 如果 count > 0，说明存在
     */
    @Override
    public boolean existsById(Long custId) {
        String sql = "SELECT COUNT(*) FROM user WHERE cust_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, custId);
        return count != null && count > 0;
    }
}


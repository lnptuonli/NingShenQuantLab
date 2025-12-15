package com.ningshenquantlab.alphaforge_demo1.entity;

import com.ningshenquantlab.alphaforge_demo1.validation.CreateGroup;
import com.ningshenquantlab.alphaforge_demo1.validation.UpdateGroup;
import com.ningshenquantlab.alphaforge_demo1.validation.CustKey;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.*;

/**
 * 用户实体类
 * 对应数据库表：user
 * 
 * 字段映射说明：
 * - Java 使用驼峰命名：custId, custName, custKey
 * - 数据库使用下划线命名：cust_id, cust_name, cust_key
 * - JdbcTemplate 的 BeanPropertyRowMapper 会自动完成映射
 * 
 * 校验分组说明：
 * - CreateGroup：创建用户时的校验规则（不需要 id，需要密钥）
 * - UpdateGroup：更新用户时的校验规则（需要 id，昵称）
 * - 没有指定分组的注解，在所有情况下都会校验
 */
@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造器
@AllArgsConstructor // Lombok：生成全参构造器
public class User {
    
    /**
     * 数据库主键（自增）
     * 对应数据库字段：id
     * 
     * 说明：这是数据库的自增主键，由数据库自动生成
     * 创建时不需要传入，更新时也通常用 custId 而不是 id
     */
    private Long id;

    /**
     * 客户业务ID
     * 对应数据库字段：cust_id
     * 
     * 校验规则：
     * - 更新时必须提供（groups = UpdateGroup.class）
     * - 必须是正数（@Positive）
     * 
     * 注意：根据表结构，cust_id 是 MEDIUMTEXT 类型，但这里用 String 更合适
     * 如果确定是数字，建议修改表结构为 BIGINT 或 VARCHAR
     */
    @NotNull(message = "客户ID不能为空", groups = UpdateGroup.class)
    private Long custId;  // 根据表结构 BIGINT，使用 Long 类型，使用包装类是因为它可能为null
    //JSON 序列化时，Long 可以序列化为 null，而万一数据库里是null，用基本类型long映射过来则永远是 0

    /**
     * 客户昵称/用户名
     * 对应数据库字段：cust_name
     * 
     * 校验规则：
     * - 创建和更新时都不能为空（groups = {CreateGroup.class, UpdateGroup.class}）
     * - 长度必须在 2-50 之间（@Size）
     * - 不能只包含空格（@NotBlank 会自动检查）
     */
    @NotBlank(message = "客户昵称不能为空", groups = {CreateGroup.class, UpdateGroup.class})
    @Size(min = 2, max = 50, message = "客户昵称长度必须在 2-50 个字符之间")
    private String custName;

    /**
     * 客户密钥/密码
     * 对应数据库字段：cust_key
     * 
     * 校验规则：
     * - 创建时必须提供（groups = CreateGroup.class）
     * - 更新时可选（不在 UpdateGroup 中）
     * - 长度至少8位（@Size）
     * - 必须符合自定义格式（@CustKey）：包含数字、字母、特殊符号
     */
    @NotBlank(message = "客户密钥不能为空", groups = CreateGroup.class)
    @Size(min = 8, max = 50, message = "密钥长度必须在 8-50 个字符之间")
    @CustKey(message = "密钥格式不正确，必须包含数字、字母和特殊符号")
    private String custKey;
}

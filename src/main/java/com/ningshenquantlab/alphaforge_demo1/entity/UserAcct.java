package com.ningshenquantlab.alphaforge_demo1.entity;
/**
 * 用户实体类
 * 对应数据库表：user_acct
 **/
import com.ningshenquantlab.alphaforge_demo1.validation.ContactCheck;
import com.ningshenquantlab.alphaforge_demo1.validation.CreateGroup;
import com.ningshenquantlab.alphaforge_demo1.validation.CustKey;
import com.ningshenquantlab.alphaforge_demo1.validation.UpdateGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data  // Lombok：自动生成 getter、setter、toString、equals、hashCode
@NoArgsConstructor  // Lombok：生成无参构造器
@AllArgsConstructor // Lombok：生成全参构造器
@ContactCheck//跨字段检查的自定义注解需要标注在类上，并且在定义时也要在Target注解里写上ElementType.TYPE，表示其可以作用在类上
public class UserAcct {
    /**
     * 数据库主键（自增）
     * 对应数据库字段：id
     *
     * 说明：这是数据库的自增主键，由数据库自动生成
     * 创建时不需要传入，更新时也通常用 custId 而不是 id
     */
    private Long id;
    @NotNull(message = "客户ID不能为空", groups = UpdateGroup.class)
    private Long custId;
    @NotNull(message = "客户名称不能为空", groups = UpdateGroup.class)
    @Size(min = 2, max = 50, message = "客户昵称长度必须在 2-50 个字符之间")
    private String custName;
    @NotBlank(message = "客户密钥不能为空", groups = CreateGroup.class)
    @Size(min = 8, max = 50, message = "密钥长度必须在 8-50 个字符之间")
    @CustKey(message = "密钥格式不正确，必须包含数字、字母和特殊符号")//自定义注解
    private String custKey;
    private String custEmail;
    private String custPhone;
    private Integer status;          // 0-禁用 1-启用，默认为1-启用
    private LocalDateTime createTime;//默认为当前时间戳
    private LocalDateTime updateTime;//默认为当前时间戳，更新时随之更新
}

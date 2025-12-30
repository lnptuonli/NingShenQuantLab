package com.ningshenquantlab.alphaforge_demo1.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义校验注解：客户密钥格式校验
 * 规则：必须包含至少一个数字、一个字母和一个特殊符号，且长度大于等于8位
 * 
 * 使用示例：
 * @CustKey(message = "密钥格式不正确")
 * private String custKey;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})  // 可以用在字段和参数上
@Retention(RetentionPolicy.RUNTIME)                   // 运行时保留
@Constraint(validatedBy = CustKeyValidator.class)     // 指定校验器
@Documented                                           // 生成文档
public @interface CustKey {
    /**
     * 校验失败时的错误消息
     */
    String message() default "客户密钥格式不合法，必须包含数字、字母和特殊符号，长度至少8位";
    
    /**
     * 校验分组
     */
    Class<?>[] groups() default {};
    
    /**
     * 负载（用于传递元数据）
     */
    Class<? extends Payload>[] payload() default {};
}


package com.ningshenquantlab.alphaforge_demo1.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 自定义校验注解：客户联系方式校验
 * 规则：邮箱和电话不能同时为空
 *
 * 使用示例：
 * @ContactCheck(message = "邮箱和电话不能同时为空")
 * private String custEmail;
 * private String custPhone;
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})  // 可以用在类上
@Retention(RetentionPolicy.RUNTIME)                   // 运行时保留
@Constraint(validatedBy = ContactCheckValidator.class)     // 指定校验器
@Documented                                           // 生成文档
public @interface ContactCheck {
    /**
     * 校验失败时的错误消息
     */
    String message() default "邮箱和电话不能同时为空";

    /**
     * 校验分组，决定什么时候校验
     */
    Class<?>[] groups() default {};

    /**
     * 负载（用于传递元数据）提供一个“元数据通道”，让你可以把额外信息附加到约束上，给框架或工具扩展用
     * 与安全框架、监控工具集成时，传递额外的上下文
     * 这样，校验失败时，你可以通过ConstraintViolation.getConstraintDescriptor().getPayload()拿到这个元数据
     * 知道这是一个 Error 级别 的约束，而不是 Warning
     */
    Class<? extends Payload>[] payload() default {};
}

package com.ningshenquantlab.alphaforge_demo1.customer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 客户密钥校验器
 * 实现 ConstraintValidator 接口，定义具体的校验逻辑
 * 
 * 校验规则：
 * 1. 至少包含一个数字 (?=.*[0-9])
 * 2. 至少包含一个字母 (?=.*[A-Za-z])
 * 3. 至少包含一个特殊符号 (?=.*[^A-Za-z0-9])
 * 4. 总长度至少8位 .{8,}
 */
public class CustKeyValidator implements ConstraintValidator<CustKey, String> {
    
    /**
     * 密钥格式正则表达式
     * ^        : 字符串开始
     * (?=...)  : 正向预查（lookahead），确保包含某个模式但不消耗字符
     * .{8,}    : 至少8个任意字符
     * $        : 字符串结束
     */
    private static final Pattern CUST_KEY_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[A-Za-z])(?=.*[^A-Za-z0-9]).{8,}$"
    );

    /**
     * 初始化方法（可选）
     * 在校验器实例化时调用，可以获取注解上的参数
     */
    @Override
    public void initialize(CustKey constraintAnnotation) {
        // 可以在这里读取注解参数，如果需要的话
    }

    /**
     * 核心校验方法
     * @param value 待校验的值
     * @param context 校验上下文（可用于自定义错误消息）
     * @return true 表示校验通过，false 表示校验失败
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 值不在这里校验，应该由 @NotNull 或 @NotBlank 负责
        // 如果字段可以为空，这里返回 true；如果不能为空，需要配合 @NotNull 使用
        if (value == null || value.trim().isEmpty()) {
            return true;  // 交给 @NotBlank 处理
        }
        
        // 使用正则表达式校验格式
        return CUST_KEY_PATTERN.matcher(value).matches();
    }
}


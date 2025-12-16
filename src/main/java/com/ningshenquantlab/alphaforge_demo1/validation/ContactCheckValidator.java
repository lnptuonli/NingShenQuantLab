package com.ningshenquantlab.alphaforge_demo1.validation;

import com.ningshenquantlab.alphaforge_demo1.entity.UserAcct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
/**
 * 客户联系方式检查，邮箱与电话至少输入一个
 */
public class ContactCheckValidator implements ConstraintValidator<ContactCheck, UserAcct> {
    public void initialize(ContactCheck constraint) {
        // 可以在这里读取注解参数，如果需要的话
    }

    public boolean isValid(UserAcct user, ConstraintValidatorContext context) {
        // 允许 custKey 或 custEmail 至少一个非空
        return !(isBlank(user.getCustKey()) && isBlank(user.getCustEmail()));
    }
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}

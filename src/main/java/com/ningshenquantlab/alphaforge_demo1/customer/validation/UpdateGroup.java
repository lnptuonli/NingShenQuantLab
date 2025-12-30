package com.ningshenquantlab.alphaforge_demo1.customer.validation;

/**
 * 更新操作的校验分组
 * 用于 @Validated 注解，指定在更新用户时需要校验的字段
 * 例如：更新时需要 ID，但可能不需要密钥
 */
public interface UpdateGroup {
    // 空接口，仅用作分组标记
}


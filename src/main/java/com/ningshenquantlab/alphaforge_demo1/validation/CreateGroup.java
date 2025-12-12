package com.ningshenquantlab.alphaforge_demo1.validation;

/**
 * 创建操作的校验分组
 * 用于 @Validated 注解，指定在创建用户时需要校验的字段
 * 例如：创建时不需要 ID，但需要密钥
 */
public interface CreateGroup {
    // 空接口，仅用作分组标记
}


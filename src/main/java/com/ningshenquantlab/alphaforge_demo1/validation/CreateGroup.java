package com.ningshenquantlab.alphaforge_demo1.validation;

/**
 * 创建操作的校验分组
 * 用于 @Validated 注解，指定在创建用户时需要校验的字段
 * 例如：创建时不需要 ID，但需要密钥
 */
public interface CreateGroup {
    // 空接口，仅用作分组标记
    // 它不需要任何方法或实现，只是一个“标记”。
    // 框架在运行时只关心你传入的分组类型（接口的 Class 对象），并不会调用接口里的方法。
    // 没有实现是正常的，它的存在意义就是让 @Validated 能识别你要执行哪一组校验。

}



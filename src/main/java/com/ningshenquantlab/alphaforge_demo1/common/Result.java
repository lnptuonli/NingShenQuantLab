package com.ningshenquantlab.alphaforge_demo1.common;

import lombok.Data;

/**
 * 统一返回结果类
 * 用于封装所有 API 接口的返回数据，统一返回格式
 * 
 * 返回格式：
 * {
 *   "code": 200,           // 状态码
 *   "message": "success",  // 提示信息
 *   "data": {...},         // 业务数据
 *   "timestamp": 1234567890 // 时间戳
 * }
 * 
 * 常用状态码：
 * - 200: 成功
 * - 400: 客户端请求错误（参数校验失败、业务异常）
 * - 401: 未授权（未登录）
 * - 403: 无权限
 * - 404: 资源不存在
 * - 500: 服务器内部错误
 */
@Data  // Lombok 注解，自动生成 getter、setter、toString、equals、hashCode
public class Result<T> {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 提示信息
     */
    private String message;
    
    /**
     * 返回的数据（泛型，可以是任意类型）
     */
    private T data;
    
    /**
     * 时间戳（当前请求的时间）
     */
    private Long timestamp;

    /**
     * 私有构造器，防止外部直接 new
     * 必须通过静态工厂方法创建实例
     */
    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回（带数据）
     * 
     * 使用示例：
     * return Result.success(user);
     * 
     * @param data 要返回的数据
     * @return Result 对象
     */
    //public static <T>这样写，可以声明这个方法是一个泛型方法，在调用时，会根据上下文来推断出T的类型
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（无数据）
     * 
     * 使用示例：
     * return Result.success();
     * 
     * @return Result 对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 成功返回（自定义消息 + 数据）
     * 
     * 使用示例：
     * return Result.success("创建成功", user);
     * 
     * @param message 自定义消息
     * @param data 要返回的数据
     * @return Result 对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 失败返回（自定义状态码和消息）
     * 
     * 使用示例：
     * return Result.error(404, "用户不存在");
     * 
     * @param code 状态码
     * @param message 错误消息
     * @return Result 对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);  // 失败时通常不返回数据
        return result;
    }

    /**
     * 失败返回（默认 500 服务器错误）
     * 
     * 使用示例：
     * return Result.error("系统繁忙，请稍后再试");
     * 
     * @param message 错误消息
     * @return Result 对象
     */
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}

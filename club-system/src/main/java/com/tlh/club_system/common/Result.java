package com.tlh.club_system.common;

import lombok.Data;

/**
 * 统一API响应结果封装
 * 所有的 Controller 接口都返回这个对象
 */
@Data
public class Result<T> {

    private Integer code; // 状态码: 200成功, 500错误
    private String msg;   // 提示信息
    private T data;       // 返回的数据 (泛型)

    // 无参数构造器
    public Result() {}

    // 全参数构造器
    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    // 成功 - 无数据 (例如删除成功)
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    // 成功 - 带数据 (例如查询列表)
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    // 成功 - 自定义消息
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(200, msg, data);
    }

    // 失败 - 自定义错误信息
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }

    // 失败 - 自定义状态码和信息
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
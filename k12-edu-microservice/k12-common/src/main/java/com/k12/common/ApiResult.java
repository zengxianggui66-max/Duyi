package com.k12.common;

import lombok.Data;

/**
 * 统一API响应结果包装类
 * 与前端 src/api/index.ts 中的 ApiResult<T> 接口对应
 */
@Data
public class ApiResult<T> {
    /** 状态码：200=成功，其他=失败 */
    private int code;
    
    /** 提示信息 */
    private String message;
    
    /** 响应数据 */
    private T data;

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResult<T> success(T data) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success() {
        return success(null);
    }

    /**
     * 失败响应
     */
    public static <T> ApiResult<T> error(int code, String message) {
        ApiResult<T> result = new ApiResult<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败响应（默认状态码500）
     */
    public static <T> ApiResult<T> error(String message) {
        return error(500, message);
    }
}

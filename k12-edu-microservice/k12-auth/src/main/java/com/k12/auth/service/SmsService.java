package com.k12.auth.service;

/**
 * 短信验证码服务接口
 */
public interface SmsService {

    /**
     * 发送短信验证码
     * @param phone 手机号
     * @param type 用途 (login/register/reset)
     * @param ip 请求IP
     */
    void sendCode(String phone, String type, String ip);

    /**
     * 验证短信验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String phone, String code);

    /**
     * 验证并消耗指定用途的验证码
     */
    boolean verifyCode(String phone, String code, String type);

    /**
     * 校验验证码是否有效（不消耗）
     */
    boolean checkCode(String phone, String code);

    boolean checkCode(String phone, String code, String type);

    /**
     * 将验证码标记为已使用
     */
    void consumeCode(String phone, String code);
}

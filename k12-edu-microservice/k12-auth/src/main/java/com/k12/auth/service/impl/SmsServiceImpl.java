package com.k12.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.entity.SmsCode;
import com.k12.auth.mapper.SmsCodeMapper;
import com.k12.auth.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 短信验证码服务实现
 *
 * 开发阶段使用模拟短信（控制台打印验证码），
 * 生产环境替换为真实的短信服务商 API（如阿里云SMS、腾讯云SMS等）。
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    private final SmsCodeMapper smsCodeMapper;
    public SmsServiceImpl(SmsCodeMapper smsCodeMapper) {
        this.smsCodeMapper = smsCodeMapper;
    }


    /** 验证码有效期（分钟） */
    private static final int CODE_EXPIRE_MINUTES = 5;
    /** 发送间隔限制（秒） */
    private static final int SEND_INTERVAL_SECONDS = 60;
    /** 每日发送上限 */
    private static final int DAILY_LIMIT = 10;

    @Override
    public void sendCode(String phone, String type, String ip) {
        // 1. 检查发送频率限制（60秒内不能重复发送）
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusSeconds(SEND_INTERVAL_SECONDS);
        Long recentCount = smsCodeMapper.selectCount(
                new LambdaQueryWrapper<SmsCode>()
                        .eq(SmsCode::getPhone, phone)
                        .ge(SmsCode::getCreateTime, oneMinuteAgo)
        );
        if (recentCount > 0) {
            throw new BusinessException(429, "发送过于频繁，请" + SEND_INTERVAL_SECONDS + "秒后重试");
        }

        // 2. 检查每日发送上限
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Long dailyCount = smsCodeMapper.selectCount(
                new LambdaQueryWrapper<SmsCode>()
                        .eq(SmsCode::getPhone, phone)
                        .ge(SmsCode::getCreateTime, todayStart)
        );
        if (dailyCount >= DAILY_LIMIT) {
            throw new BusinessException(429, "今日发送次数已达上限");
        }

        // 3. 生成 6 位随机验证码
        String code = String.format("%06d", new Random().nextInt(999999));

        // 4. 保存到数据库
        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(phone);
        smsCode.setCode(code);
        smsCode.setType(type != null ? type : "login");
        smsCode.setIp(ip != null ? ip : "");
        smsCode.setStatus(0);
        smsCode.setExpireTime(LocalDateTime.now().plusMinutes(CODE_EXPIRE_MINUTES));
        smsCodeMapper.insert(smsCode);

        // 5. 发送短信
        sendSmsByProvider(phone, code, type);
    }

    @Override
    public boolean verifyCode(String phone, String code) {
        return verifyCode(phone, code, null);
    }

    @Override
    public boolean verifyCode(String phone, String code, String type) {
        if (!checkCode(phone, code, type)) {
            return false;
        }
        SmsCode smsCode = findValidCode(phone, code, type);
        if (smsCode != null) {
            smsCode.setStatus(1);
            smsCodeMapper.updateById(smsCode);
        }
        return true;
    }

    @Override
    public boolean checkCode(String phone, String code) {
        return checkCode(phone, code, null);
    }

    @Override
    public boolean checkCode(String phone, String code, String type) {
        return findValidCode(phone, code, type) != null;
    }

    @Override
    public void consumeCode(String phone, String code) {
        SmsCode smsCode = findValidCode(phone, code, null);
        if (smsCode == null) {
            return;
        }
        smsCode.setStatus(1);
        smsCodeMapper.updateById(smsCode);
    }

    private SmsCode findValidCode(String phone, String code, String type) {
        LambdaQueryWrapper<SmsCode> wrapper = new LambdaQueryWrapper<SmsCode>()
                .eq(SmsCode::getPhone, phone)
                .eq(SmsCode::getCode, code)
                .eq(SmsCode::getStatus, 0)
                .gt(SmsCode::getExpireTime, LocalDateTime.now());
        if (StringUtils.hasText(type)) {
            wrapper.eq(SmsCode::getType, type);
        }
        return smsCodeMapper.selectOne(
                wrapper.orderByDesc(SmsCode::getCreateTime).last("LIMIT 1"));
    }

    /**
     * 调用短信服务商发送短信
     * 
     * 开发阶段：直接在控制台打印验证码（方便调试）
     * 生产环境：替换为阿里云SMS / 腾讯云SMS / 短信宝 等真实接口
     */
    private void sendSmsByProvider(String phone, String code, String type) {
        // ========== 开发模式：控制台打印 ==========
        log.info("========================================");
        log.info("【K12教育平台】手机号 {} 的验证码: {}", phone, code);
        log.info("验证码有效期 {} 分钟，请勿泄露给他人", CODE_EXPIRE_MINUTES);
        log.info("========================================");

        // ========== 生产模式（取消注释并配置参数） ==========
        // 示例：阿里云短信
        // try {
        //     DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        //     IAcsClient client = new DefaultAcsClient(profile);
        //     CommonRequest request = new CommonRequest();
        //     request.setSysMethod(MethodType.POST);
        //     request.setSysDomain("dysmsapi.aliyuncs.com");
        //     request.setSysVersion("2017-05-25");
        //     request.setSysAction("SendSms");
        //     request.putQueryParameter("PhoneNumbers", phone);
        //     request.putQueryParameter("SignName", "K12教育平台");
        //     request.putQueryParameter("TemplateCode", "SMS_XXXXXXXXX");
        //     request.putQueryParameter("TemplateParam", "{\"code\":\"" + code + "\"}");
        //     CommonResponse response = client.getCommonResponse(request);
        //     log.info("短信发送结果: {}", response.getData());
        // } catch (Exception e) {
        //     log.error("短信发送失败: phone={}, error={}", phone, e.getMessage());
        //     throw new BusinessException("短信发送失败，请稍后重试");
        // }
    }
}

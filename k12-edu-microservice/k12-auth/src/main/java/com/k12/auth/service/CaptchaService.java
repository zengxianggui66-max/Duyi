package com.k12.auth.service;

import java.util.Map;

public interface CaptchaService {

    Map<String, Object> generate();

    boolean verify(String captchaKey, String captchaCode);

    void requireValid(String captchaKey, String captchaCode);
}

package com.k12.member.service;

import java.util.Map;

public interface MemberService {
    Map<String, Object> createOrder(Long userId, Integer memberLevel);
    Map<String, Object> getMemberInfo(Long userId);
}

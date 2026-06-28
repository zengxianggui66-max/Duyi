package com.k12.member.service.impl;

import com.k12.common.BusinessException;
import com.k12.common.entity.MemberOrder;
import com.k12.member.mapper.MemberOrderMapper;
import com.k12.member.mapper.UserMapper;
import com.k12.member.service.MemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberOrderMapper memberOrderMapper;
    private final UserMapper userMapper;

    public MemberServiceImpl(MemberOrderMapper memberOrderMapper, UserMapper userMapper) {
        this.memberOrderMapper = memberOrderMapper;
        this.userMapper = userMapper;
    }

    private static final Map<Integer, double[]> MEMBER_PRICES = Map.of(
            1, new double[]{99, 365},
            2, new double[]{199, 365}
    );

    @Override
    public Map<String, Object> createOrder(Long userId, Integer memberLevel) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!MEMBER_PRICES.containsKey(memberLevel)) {
            throw new BusinessException("无效的会员等级");
        }
        double[] priceInfo = MEMBER_PRICES.get(memberLevel);
        double amount = priceInfo[0];
        int days = (int) priceInfo[1];

        String orderNo = "K12" + System.currentTimeMillis() + new Random().nextInt(1000);

        MemberOrder order = new MemberOrder();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setMemberLevel(memberLevel);
        order.setAmount(java.math.BigDecimal.valueOf(amount));
        order.setDurationDays(days);
        order.setPayStatus(0);
        memberOrderMapper.insert(order);

        Map<String, Object> result = new HashMap<>();
        result.put("orderNo", orderNo);
        result.put("memberLevel", memberLevel);
        result.put("amount", amount);
        result.put("durationDays", days);
        result.put("message", "订单创建成功，请完成支付");
        return result;
    }

    @Override
    public Map<String, Object> getMemberInfo(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        com.k12.common.entity.User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        int level = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
        if (level < 0 || level > 2) {
            level = 0;
        }

        Map<String, Object> info = new HashMap<>();
        info.put("memberLevel", level);
        info.put("memberExpireTime", user.getMemberExpireTime());

        String[] levelNames = {"免费用户", "基础会员", "高级会员"};
        info.put("memberLevelName", levelNames[level]);

        boolean isExpired = user.getMemberExpireTime() != null && user.getMemberExpireTime().isBefore(LocalDateTime.now());
        info.put("isExpired", isExpired);

        String[] benefits = {"每日免费下载3个", "每日下载50个+智能备课+智能组卷", "无限下载+全功能+优先客服"};
        info.put("benefits", benefits[level]);

        return info;
    }
}

package com.k12.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.Result;
import com.k12.common.auth.TokenBlacklistService;
import com.k12.common.dto.BindPhoneDTO;
import com.k12.common.dto.LoginDTO;
import com.k12.common.dto.OAuthCallbackDTO;
import com.k12.common.dto.RegisterDTO;
import com.k12.common.dto.SmsLoginDTO;
import com.k12.common.dto.SmsRegisterDTO;
import com.k12.common.util.RoleUtils;
import com.k12.common.entity.User;
import com.k12.auth.mapper.UserMapper;
import com.k12.auth.mapper.UserOAuthBindMapper;
import com.k12.common.entity.UserOAuthBind;
import com.k12.common.dto.OAuthCompleteDTO;
import com.k12.common.dto.ChangePasswordDTO;
import com.k12.common.dto.ChangePhoneDTO;
import com.k12.common.dto.SmsSendDTO;
import com.k12.common.dto.UpdateProfileDTO;
import com.k12.auth.service.AuthService;
import com.k12.auth.service.LoginAttemptService;
import com.k12.auth.service.UserLoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import com.k12.auth.service.OAuthService;
import com.k12.auth.service.OAuthStateService;
import com.k12.auth.service.OAuthStateService.OAuthStateContext;
import com.k12.auth.service.SmsService;
import com.k12.common.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserOAuthBindMapper userOAuthBindMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final SmsService smsService;
    private final OAuthService oAuthService;
    private final OAuthStateService oAuthStateService;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserLoginLogService userLoginLogService;
    public AuthServiceImpl(UserMapper userMapper, UserOAuthBindMapper userOAuthBindMapper, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, SmsService smsService, OAuthService oAuthService, OAuthStateService oAuthStateService, LoginAttemptService loginAttemptService, TokenBlacklistService tokenBlacklistService, UserLoginLogService userLoginLogService) {
        this.userMapper = userMapper;
        this.userOAuthBindMapper = userOAuthBindMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.smsService = smsService;
        this.oAuthService = oAuthService;
        this.oAuthStateService = oAuthStateService;
        this.loginAttemptService = loginAttemptService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.userLoginLogService = userLoginLogService;
    }


    @Override
    public Result<Map<String, Object>> login(LoginDTO dto, HttpServletRequest request) {
        String attemptKey = "pwd:" + dto.getUsername();
        loginAttemptService.checkAllowed(attemptKey);

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            loginAttemptService.onFailure(attemptKey);
            userLoginLogService.recordFailure(dto.getUsername(), "password", "用户不存在", request);
            throw new BusinessException(401, "用户不存在");
        }
        String loginType = UserLoginLogServiceImpl.resolveLoginType(user.getRole(), null);
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            loginAttemptService.onFailure(attemptKey);
            userLoginLogService.recordFailure(dto.getUsername(), loginType, "密码错误", request);
            throw new BusinessException(401, "密码错误");
        }
        if (user.getStatus() == 0) {
            userLoginLogService.recordFailure(dto.getUsername(), loginType, "账号已被禁用", request);
            throw new BusinessException(403, "账号已被禁用");
        }

        loginAttemptService.onSuccess(attemptKey);
        userLoginLogService.recordSuccess(user.getId(), user.getUsername(), loginType, request);
        return Result.success("登录成功", buildLoginData(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> register(RegisterDTO dto) {
        requireAgreedToTerms(dto.getAgreedToTerms());
        String role = RoleUtils.resolveRegisterRole(dto.getRole());

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(dto.getEmail())) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
            if (emailCount > 0) {
                throw new BusinessException("邮箱已被注册");
            }
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
        user.setRole(role);
        user.setMemberLevel(0);
        user.setStatus(1);
        userMapper.insert(user);

        Map<String, Object> data = buildLoginData(user);
        data.put("isNewUser", true);
        return Result.success("注册成功", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> smsRegister(SmsRegisterDTO dto) {
        requireAgreedToTerms(dto.getAgreedToTerms());
        String role = RoleUtils.resolveRegisterRole(dto.getRole());

        String attemptKey = "sms-reg:" + dto.getPhone();
        loginAttemptService.checkAllowed(attemptKey);

        if (!smsService.checkCode(dto.getPhone(), dto.getCode())) {
            loginAttemptService.onFailure(attemptKey);
            throw new BusinessException(401, "验证码错误或已过期");
        }

        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        if (existing != null) {
            throw new BusinessException(400, "该手机号已注册，请直接登录");
        }

        User user = new User();
        user.setUsername("phone_" + dto.getPhone());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        String nickname = StringUtils.hasText(dto.getNickname())
                ? dto.getNickname().trim()
                : "用户" + dto.getPhone().substring(7);
        user.setNickname(nickname);
        user.setPhone(dto.getPhone());
        user.setOauthType(null);
        user.setOauthId(null);
        user.setRole(role);
        user.setMemberLevel(0);
        user.setStatus(1);
        userMapper.insert(user);

        smsService.consumeCode(dto.getPhone(), dto.getCode());
        loginAttemptService.onSuccess(attemptKey);

        Map<String, Object> data = buildLoginData(user);
        data.put("isNewUser", true);
        data.put("needBindPhone", false);
        return Result.success("注册成功", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> smsLogin(SmsLoginDTO dto, HttpServletRequest request) {
        String attemptKey = "sms:" + dto.getPhone();
        loginAttemptService.checkAllowed(attemptKey);

        if (!smsService.checkCode(dto.getPhone(), dto.getCode())) {
            loginAttemptService.onFailure(attemptKey);
            userLoginLogService.recordFailure(dto.getPhone(), "sms", "验证码错误或已过期", request);
            throw new BusinessException(401, "验证码错误或已过期");
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, dto.getPhone()));
        boolean isNewUser = false;
        if (user == null) {
            isNewUser = true;
            user = new User();
            user.setUsername("phone_" + dto.getPhone());
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setNickname("用户" + dto.getPhone().substring(7));
            user.setPhone(dto.getPhone());
            user.setOauthType(null);
            user.setOauthId(null);
            user.setRole(RoleUtils.resolveRegisterRole(dto.getRole()));
            user.setMemberLevel(0);
            user.setStatus(1);
            userMapper.insert(user);
        }

        if (user.getStatus() == 0) {
            userLoginLogService.recordFailure(user.getUsername(), "sms", "账号已被禁用", request);
            throw new BusinessException(403, "账号已被禁用");
        }

        smsService.consumeCode(dto.getPhone(), dto.getCode());
        loginAttemptService.onSuccess(attemptKey);
        userLoginLogService.recordSuccess(user.getId(), user.getUsername(), "sms", request);
        Map<String, Object> data = buildLoginData(user);
        data.put("isNewUser", isNewUser);
        data.put("needBindPhone", false);
        return Result.success(isNewUser ? "注册并登录成功" : "登录成功", data);
    }

    @Override
    public Result<Map<String, Object>> getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return Result.success(buildUserInfo(user));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Map<String, Object>> updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname().trim());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar().trim());
        }
        if (dto.getGender() != null) {
            if (dto.getGender() < 0 || dto.getGender() > 2) {
                throw new BusinessException(400, "性别取值无效");
            }
            user.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            String bd = dto.getBirthday().trim();
            if (bd.isEmpty()) {
                user.setBirthday(null);
            } else {
                try {
                    user.setBirthday(LocalDate.parse(bd));
                } catch (DateTimeParseException e) {
                    throw new BusinessException(400, "生日格式应为 YYYY-MM-DD");
                }
            }
        }
        if (dto.getEmail() != null) {
            String email = dto.getEmail().trim();
            if (email.isEmpty()) {
                user.setEmail("");
            } else {
                Long emailCount = userMapper.selectCount(
                        new LambdaQueryWrapper<User>()
                                .eq(User::getEmail, email)
                                .ne(User::getId, userId));
                if (emailCount > 0) {
                    throw new BusinessException("邮箱已被其他账号使用");
                }
                user.setEmail(email);
            }
        }

        userMapper.updateById(user);

        Map<String, Object> data = new HashMap<>();
        data.put("userInfo", buildUserInfo(user));
        return Result.success("保存成功", data);
    }

    @Override
    public Result<Map<String, Object>> oauthLogin(OAuthCallbackDTO dto, HttpServletRequest request) {
        OAuthStateContext context = oAuthStateService.validateAndConsume(dto.getState(), dto.getType());
        Map<String, Object> result = oAuthService.handleCallback(dto.getCode(), dto.getType(), context);

        if (Boolean.TRUE.equals(result.get("bindSuccess"))) {
            return Result.success("绑定成功", result);
        }
        if (Boolean.TRUE.equals(result.get("needsRoleSelection"))) {
            return Result.success("请选择身份", result);
        }

        recordOAuthLoginSuccess(result, request);
        enrichNeedBindPhone(result);
        return Result.success("登录成功", result);
    }

    private void recordOAuthLoginSuccess(Map<String, Object> result, HttpServletRequest request) {
        Object userInfo = result.get("userInfo");
        if (!(userInfo instanceof Map<?, ?> map)) {
            return;
        }
        Object idObj = map.get("id");
        if (idObj instanceof Number idNum) {
            String username = map.get("username") != null ? String.valueOf(map.get("username")) : null;
            userLoginLogService.recordSuccess(idNum.longValue(), username, "oauth", request);
        }
    }

    @Override
    public Result<Map<String, Object>> completeOAuthRegister(OAuthCompleteDTO dto) {
        Map<String, Object> result = oAuthService.completeOAuthRegister(
                dto.getType(), dto.getOauthId(), dto.getNickname(), dto.getAvatar(), dto.getRole());
        enrichNeedBindPhone(result);
        return Result.success("注册成功", result);
    }

    @Override
    public Result<List<Map<String, Object>>> listOAuthBinds(Long userId) {
        return Result.success(oAuthService.listBinds(userId));
    }

    @Override
    public Result<List<Map<String, Object>>> listOAuthLogs(Long userId, Integer size) {
        return Result.success(oAuthService.listLogs(userId, size));
    }

    @Override
    public Result<Void> unbindOAuth(Long userId, String type) {
        oAuthService.unbind(userId, type);
        return Result.success("解绑成功", null);
    }

    private void enrichNeedBindPhone(Map<String, Object> result) {
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) result.get("userInfo");
        if (userInfo != null) {
            Object need = userInfo.get("needBindPhone");
            result.put("needBindPhone", Boolean.TRUE.equals(need) || "true".equals(String.valueOf(need)));
        }
    }

    @Override
    public void sendSmsCode(Long userId, SmsSendDTO dto, String ip) {
        String type = StringUtils.hasText(dto.getType()) ? dto.getType().trim() : "login";
        User user = userId != null ? userMapper.selectById(userId) : null;

        switch (type) {
            case "change_phone_old" -> {
                User currentUser = requireLoggedInUser(user);
                if (!dto.getPhone().equals(currentUser.getPhone())) {
                    throw new BusinessException(400, "请向当前绑定的手机号发送验证码");
                }
            }
            case "change_phone_new" -> {
                User currentUser = requireLoggedInUser(user);
                if (!StringUtils.hasText(currentUser.getPhone())) {
                    throw new BusinessException(400, "请先绑定手机号");
                }
                if (dto.getPhone().equals(currentUser.getPhone())) {
                    throw new BusinessException(400, "新手机号不能与当前手机号相同");
                }
                assertPhoneNotUsed(dto.getPhone(), userId);
            }
            case "set_password" -> {
                User currentUser = requireLoggedInUser(user);
                if (!StringUtils.hasText(currentUser.getPhone())) {
                    throw new BusinessException(400, "请先绑定手机号");
                }
                if (!dto.getPhone().equals(currentUser.getPhone())) {
                    throw new BusinessException(400, "请向当前绑定的手机号发送验证码");
                }
            }
            case "bind" -> {
                if (user != null && StringUtils.hasText(user.getPhone())) {
                    throw new BusinessException(400, "已绑定手机号，请使用「更换手机号」");
                }
            }
            default -> {
                // login / register / reset 等公开场景
            }
        }
        smsService.sendCode(dto.getPhone(), type, ip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> changePhone(Long userId, ChangePhoneDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!StringUtils.hasText(user.getPhone())) {
            throw new BusinessException(400, "请先绑定手机号");
        }
        String newPhone = dto.getNewPhone().trim();
        if (newPhone.equals(user.getPhone())) {
            throw new BusinessException(400, "新手机号不能与当前手机号相同");
        }
        assertPhoneNotUsed(newPhone, userId);

        if (!smsService.verifyCode(user.getPhone(), dto.getOldCode(), "change_phone_old")) {
            throw new BusinessException(400, "原手机验证码错误或已过期");
        }
        if (!smsService.verifyCode(newPhone, dto.getNewCode(), "change_phone_new")) {
            throw new BusinessException(400, "新手机验证码错误或已过期");
        }

        user.setPhone(newPhone);
        userMapper.updateById(user);
        return Result.success("手机号已更换", null);
    }

    @Override
    public Result<Void> bindPhone(Long userId, BindPhoneDTO dto) {
        User current = userMapper.selectById(userId);
        if (current != null && StringUtils.hasText(current.getPhone())) {
            throw new BusinessException(400, "已绑定手机号，请使用「更换手机号」");
        }
        if (!smsService.verifyCode(dto.getPhone(), dto.getCode(), "bind")) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, dto.getPhone())
                        .ne(User::getId, userId));
        if (exists > 0) {
            throw new BusinessException("该手机号已被其他账号绑定");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        user.setPhone(dto.getPhone());
        userMapper.updateById(user);
        return Result.success("绑定成功", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        boolean viaOld = StringUtils.hasText(dto.getOldPassword());
        boolean viaSms = StringUtils.hasText(dto.getPhone()) && StringUtils.hasText(dto.getCode());

        if (viaOld && viaSms) {
            throw new BusinessException(400, "请选择原密码或短信验证其中一种方式");
        }
        if (!viaOld && !viaSms) {
            throw new BusinessException(400, "请提供原密码，或使用已绑定手机号短信验证");
        }

        if (viaOld) {
            if (!StringUtils.hasText(user.getPassword())
                    || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new BusinessException(400, "原密码错误");
            }
        } else {
            if (!StringUtils.hasText(user.getPhone())) {
                throw new BusinessException(400, "请先绑定手机号后再通过短信设置密码");
            }
            if (!user.getPhone().equals(dto.getPhone().trim())) {
                throw new BusinessException(400, "请使用当前账号已绑定的手机号");
            }
            if (!smsService.verifyCode(dto.getPhone(), dto.getCode(), "set_password")) {
                throw new BusinessException(400, "验证码错误或已过期");
            }
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
        return Result.success("密码已更新", null);
    }

    @Override
    public Result<Void> logout(String token) {
        if (!StringUtils.hasText(token)) {
            return Result.success(null);
        }
        try {
            Claims claims = jwtUtils.parseToken(token);
            String jti = claims.get("jti") != null ? String.valueOf(claims.get("jti")) : null;
            long exp = claims.getExpiration() != null
                    ? claims.getExpiration().getTime()
                    : System.currentTimeMillis() + 86400000L;
            tokenBlacklistService.add(jti, exp);
        } catch (Exception ignored) {
            // 无效 token 也视为登出成功
        }
        return Result.success("已退出登录", null);
    }

    private Map<String, Object> buildLoginData(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole());
        claims.put("memberLevel", user.getMemberLevel());
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), claims);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("accessToken", token);
        data.put("expiresIn", 86400);
        data.put("userInfo", buildUserInfo(user));
        return data;
    }

    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("email", user.getEmail());
        info.put("gender", user.getGender() != null ? user.getGender() : 0);
        info.put("birthday", user.getBirthday() != null ? user.getBirthday().toString() : null);
        info.put("phone", user.getPhone());
        info.put("role", user.getRole());
        info.put("roleName", RoleUtils.getRoleName(user.getRole()));
        info.put("memberLevel", user.getMemberLevel());
        info.put("memberExpireTime", user.getMemberExpireTime());
        info.put("oauthType", user.getOauthType());
        info.put("needBindPhone", !StringUtils.hasText(user.getPhone()));
        List<UserOAuthBind> binds = userOAuthBindMapper.selectList(
                new LambdaQueryWrapper<UserOAuthBind>().eq(UserOAuthBind::getUserId, user.getId()));
        info.put("oauthBinds", binds.stream().map(UserOAuthBind::getOauthType).toList());
        return info;
    }

    private void requireAgreedToTerms(String agreed) {
        if (!"true".equalsIgnoreCase(agreed) && !"1".equals(agreed) && !"yes".equalsIgnoreCase(agreed)) {
            throw new BusinessException(400, "请阅读并同意用户协议与隐私政策");
        }
    }

    private static User requireLoggedInUser(User user) {
        if (user == null) {
            throw new BusinessException(401, "请先登录");
        }
        return user;
    }

    private void assertPhoneNotUsed(String phone, Long excludeUserId) {
        Long exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, phone)
                        .ne(User::getId, excludeUserId));
        if (exists > 0) {
            throw new BusinessException("该手机号已被其他账号绑定");
        }
    }

}

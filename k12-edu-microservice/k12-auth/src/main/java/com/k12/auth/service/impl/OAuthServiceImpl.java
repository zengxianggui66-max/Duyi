package com.k12.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.entity.User;
import com.k12.common.entity.UserOAuthBind;
import com.k12.common.util.RoleUtils;
import com.k12.auth.mapper.UserMapper;
import com.k12.auth.mapper.UserOAuthBindMapper;
import com.k12.auth.service.OAuthService;
import com.k12.auth.service.OAuthStateService.OAuthStateContext;
import com.k12.common.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OAuthServiceImpl implements OAuthService {

    private static final Logger log = LoggerFactory.getLogger(OAuthServiceImpl.class);

    private static final Set<String> OAUTH_TYPES = Set.of("wechat", "qq", "wework");

    private final UserMapper userMapper;
    private final UserOAuthBindMapper userOAuthBindMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    public OAuthServiceImpl(UserMapper userMapper, UserOAuthBindMapper userOAuthBindMapper, JwtUtils jwtUtils, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userOAuthBindMapper = userOAuthBindMapper;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
    }


    @Value("${oauth.mock.enabled:true}")
    private boolean mockEnabled;

    @Value("${oauth.weixin.app-id:}")
    private String weixinAppId;
    @Value("${oauth.weixin.app-secret:}")
    private String weixinAppSecret;

    @Value("${oauth.qq.app-id:}")
    private String qqAppId;
    @Value("${oauth.qq.app-secret:}")
    private String qqAppSecret;
    @Value("${oauth.qq.redirect-uri:}")
    private String qqRedirectUri;

    @Value("${oauth.wework.corp-id:}")
    private String weworkCorpId;
    @Value("${oauth.wework.agent-id:}")
    private String weworkAgentId;
    @Value("${oauth.wework.corp-secret:}")
    private String weworkCorpSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final int MAX_LOG_HISTORY = 200;
    private final Map<Long, Deque<Map<String, Object>>> oauthLogs = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public Map<String, Object> getAuthorizationUrl(String type, String redirectUri, String state) {
        validateOAuthType(type);
        if (mockEnabled) {
            String sep = redirectUri.contains("?") ? "&" : "?";
            String mockUrl = redirectUri + sep + "code=mock_" + type + "_" + System.currentTimeMillis()
                    + "&state=" + state;
            return Map.of("type", type, "state", state, "authorizationUrl", mockUrl);
        }
        String url = switch (type) {
            case "wechat" -> getWeixinAuthUrl(redirectUri, state);
            case "qq" -> getQQAuthUrl(redirectUri, state);
            case "wework" -> getWeworkAuthUrl(redirectUri, state);
            default -> throw new BusinessException("不支持的登录类型: " + type);
        };
        return Map.of("type", type, "state", state, "authorizationUrl", url);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> handleCallback(String code, String type, OAuthStateContext context) {
        validateOAuthType(type);
        try {
            OAuthProfile profile = resolveOAuthProfile(code, type);
            if (context.isBindMode()) {
                bindOAuthToUser(context.userId(), type, profile);
                appendOAuthLog(context.userId(), type, "bind", "success", null, "绑定成功");
                return Map.of("bindSuccess", true, "oauthType", type);
            }
            return loginOrRegister(type, profile, context.role());
        } catch (BusinessException e) {
            if (context.isBindMode() && context.userId() != null) {
                appendOAuthLog(context.userId(), type, "bind", "fail", String.valueOf(e.getCode()), e.getMessage());
            }
            throw e;
        } catch (RuntimeException e) {
            if (context.isBindMode() && context.userId() != null) {
                appendOAuthLog(context.userId(), type, "bind", "fail", "500", e.getMessage());
            }
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> listBinds(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        List<UserOAuthBind> binds = userOAuthBindMapper.selectList(
                new LambdaQueryWrapper<UserOAuthBind>().eq(UserOAuthBind::getUserId, userId));
        long bindCount = binds.size();
        boolean hasPassword = StringUtils.hasText(user.getPassword())
                && !user.getPassword().isBlank();
        boolean hasPhone = StringUtils.hasText(user.getPhone());
        boolean canUnbindByPolicy = !(bindCount <= 1 && !hasPassword && !hasPhone);
        String unbindReason = canUnbindByPolicy
                ? ""
                : "至少保留一种登录方式，请先设置密码或绑定手机号后再解绑";

        List<Map<String, Object>> result = new ArrayList<>();
        for (String t : OAUTH_TYPES) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", t);
            item.put("typeName", getTypeName(t));
            UserOAuthBind bind = binds.stream().filter(b -> t.equals(b.getOauthType())).findFirst().orElse(null);
            item.put("bound", bind != null);
            if (bind != null) {
                item.put("nickname", bind.getNickname());
                item.put("avatar", bind.getAvatar());
                item.put("bindTime", bind.getCreateTime());
                // 目前无独立 third-party last_login_at 字段，先使用更新时刻作为最近登录近似值
                item.put("lastLoginAt", bind.getUpdateTime());
                item.put("canUnbind", canUnbindByPolicy);
                item.put("unbindReason", canUnbindByPolicy ? "" : unbindReason);
            } else {
                item.put("lastLoginAt", null);
                item.put("canUnbind", false);
                item.put("unbindReason", "");
            }
            result.add(item);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long userId, String type) {
        validateOAuthType(type);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        UserOAuthBind bind = userOAuthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getUserId, userId)
                        .eq(UserOAuthBind::getOauthType, type));
        if (bind == null) {
            throw new BusinessException(400, "未绑定" + getTypeName(type));
        }
        long bindCount = userOAuthBindMapper.selectCount(
                new LambdaQueryWrapper<UserOAuthBind>().eq(UserOAuthBind::getUserId, userId));
        boolean hasPassword = StringUtils.hasText(user.getPassword())
                && !user.getPassword().isBlank();
        boolean hasPhone = StringUtils.hasText(user.getPhone());
        if (bindCount <= 1 && !hasPassword && !hasPhone) {
            throw new BusinessException(400, "至少保留一种登录方式，请先设置密码或绑定手机号后再解绑");
        }
        userOAuthBindMapper.deleteById(bind.getId());
        syncUserPrimaryOAuth(userId);
        appendOAuthLog(userId, type, "unbind", "success", null, "解绑成功");
        log.info("用户解绑第三方: userId={}, type={}", userId, type);
    }

    @Override
    public List<Map<String, Object>> listLogs(Long userId, Integer size) {
        int limit = size == null ? 5 : Math.max(1, Math.min(size, 20));
        Deque<Map<String, Object>> queue = oauthLogs.getOrDefault(userId, new ArrayDeque<>());
        List<Map<String, Object>> logs = queue.stream().limit(limit).toList();
        if (!logs.isEmpty()) {
            return logs;
        }
        // 历史无内存日志时，回退到最近绑定记录，保证至少有可展示数据
        List<UserOAuthBind> binds = userOAuthBindMapper.selectList(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getUserId, userId)
                        .orderByDesc(UserOAuthBind::getUpdateTime)
                        .last("LIMIT " + limit)
        );
        return binds.stream().map(bind -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", bind.getId());
            item.put("action", "bind");
            item.put("provider", bind.getOauthType());
            item.put("providerName", getTypeName(bind.getOauthType()));
            item.put("result", "success");
            item.put("reasonCode", null);
            item.put("reasonMessage", "绑定成功");
            item.put("ip", null);
            item.put("createdAt", bind.getUpdateTime() != null ? bind.getUpdateTime() : bind.getCreateTime());
            return item;
        }).toList();
    }

    private Map<String, Object> loginOrRegister(String type, OAuthProfile profile, String role) {
        UserOAuthBind existingBind = userOAuthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getOauthType, type)
                        .eq(UserOAuthBind::getOauthId, profile.oauthId()));

        User user;
        boolean isNewUser = false;

        if (existingBind != null) {
            user = userMapper.selectById(existingBind.getUserId());
            if (user == null) {
                throw new BusinessException(500, "绑定数据异常，请联系客服");
            }
        } else {
            isNewUser = true;
            String resolvedRole = RoleUtils.resolveRegisterRole(role);
            user = createOAuthUser(type, profile, resolvedRole);
            insertBind(user.getId(), type, profile);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        Map<String, Object> data = buildLoginData(user);
        data.put("isNewUser", isNewUser);
        data.put("needBindPhone", !StringUtils.hasText(user.getPhone()));
        return data;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> completeOAuthRegister(String type, String oauthId, String nickname,
                                                     String avatar, String role) {
        validateOAuthType(type);
        String resolvedRole = RoleUtils.resolveRegisterRole(role);

        UserOAuthBind exists = userOAuthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getOauthType, type)
                        .eq(UserOAuthBind::getOauthId, oauthId));
        if (exists != null) {
            User user = userMapper.selectById(exists.getUserId());
            return buildLoginData(user);
        }

        OAuthProfile profile = new OAuthProfile(oauthId, nickname, avatar);
        User user = createOAuthUser(type, profile, resolvedRole);
        insertBind(user.getId(), type, profile);

        Map<String, Object> data = buildLoginData(user);
        data.put("isNewUser", true);
        data.put("needBindPhone", true);
        return data;
    }

    private void bindOAuthToUser(Long userId, String type, OAuthProfile profile) {
        UserOAuthBind otherUserBind = userOAuthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getOauthType, type)
                        .eq(UserOAuthBind::getOauthId, profile.oauthId()));
        if (otherUserBind != null && !otherUserBind.getUserId().equals(userId)) {
            throw new BusinessException(400, "该" + getTypeName(type) + "账号已被其他用户绑定");
        }

        UserOAuthBind selfBind = userOAuthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getUserId, userId)
                        .eq(UserOAuthBind::getOauthType, type));
        if (selfBind != null) {
            throw new BusinessException(400, "您已绑定" + getTypeName(type) + "，请先解绑后再重新绑定");
        }

        insertBind(userId, type, profile);
        syncUserPrimaryOAuth(userId);
        log.info("用户绑定第三方: userId={}, type={}, oauthId={}", userId, type, profile.oauthId());
    }

    private User createOAuthUser(String type, OAuthProfile profile, String role) {
        User user = new User();
        user.setUsername(type + "_" + UUID.randomUUID().toString().substring(0, 8));
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setNickname(profile.nickname());
        user.setAvatar(profile.avatar());
        user.setRole(role);
        user.setOauthType(type);
        user.setOauthId(profile.oauthId());
        user.setMemberLevel(0);
        user.setStatus(1);
        userMapper.insert(user);
        log.info("OAuth注册新用户: id={}, type={}, role={}", user.getId(), type, role);
        return user;
    }

    private void insertBind(Long userId, String type, OAuthProfile profile) {
        UserOAuthBind bind = new UserOAuthBind();
        bind.setUserId(userId);
        bind.setOauthType(type);
        bind.setOauthId(profile.oauthId());
        bind.setNickname(profile.nickname());
        bind.setAvatar(profile.avatar());
        userOAuthBindMapper.insert(bind);
        syncUserPrimaryOAuth(userId);
    }

    private void syncUserPrimaryOAuth(Long userId) {
        UserOAuthBind first = userOAuthBindMapper.selectOne(
                new LambdaQueryWrapper<UserOAuthBind>()
                        .eq(UserOAuthBind::getUserId, userId)
                        .orderByAsc(UserOAuthBind::getId)
                        .last("LIMIT 1"));
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        if (first != null) {
            user.setOauthType(first.getOauthType());
            user.setOauthId(first.getOauthId());
        } else {
            user.setOauthType(null);
            user.setOauthId(null);
        }
        userMapper.updateById(user);
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
        List<String> boundTypes = binds.stream().map(UserOAuthBind::getOauthType).toList();
        info.put("oauthBinds", boundTypes);
        return info;
    }

    private OAuthProfile resolveOAuthProfile(String code, String type) {
        if (mockEnabled) {
            log.info("【模拟OAuth】type={}, code={}", type, code);
            String oauthId = "mock_" + type + "_" + UUID.randomUUID().toString().substring(0, 8);
            String nickname = switch (type) {
                case "wechat" -> "微信用户";
                case "qq" -> "QQ用户";
                case "wework" -> "企业微信用户";
                default -> "第三方用户";
            };
            return new OAuthProfile(oauthId, nickname, "");
        }
        return switch (type) {
            case "wechat" -> {
                Map<String, String> u = getWeixinUserInfo(code);
                yield new OAuthProfile(u.get("openid"), u.getOrDefault("nickname", "微信用户"),
                        u.getOrDefault("headimgurl", ""));
            }
            case "qq" -> {
                Map<String, String> u = getQQUserInfo(code);
                yield new OAuthProfile(u.get("openid"), u.getOrDefault("nickname", "QQ用户"),
                        u.getOrDefault("figureurl_qq_2", ""));
            }
            case "wework" -> {
                Map<String, String> u = getWeworkUserInfo(code);
                yield new OAuthProfile(u.get("userid"), u.getOrDefault("name", "企业微信用户"),
                        u.getOrDefault("avatar", ""));
            }
            default -> throw new BusinessException("不支持的登录类型: " + type);
        };
    }

    private void validateOAuthType(String type) {
        if (!OAUTH_TYPES.contains(type)) {
            throw new BusinessException(400, "不支持的第三方类型");
        }
    }

    private String getTypeName(String type) {
        return switch (type) {
            case "wechat" -> "微信";
            case "qq" -> "QQ";
            case "wework" -> "企业微信";
            default -> type;
        };
    }

    private void appendOAuthLog(Long userId, String provider, String action, String result,
                                String reasonCode, String reasonMessage) {
        if (userId == null) return;
        Deque<Map<String, Object>> queue = oauthLogs.computeIfAbsent(userId, k -> new ArrayDeque<>());
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", UUID.randomUUID().toString().replace("-", ""));
        item.put("action", action);
        item.put("provider", provider);
        item.put("providerName", getTypeName(provider));
        item.put("result", result);
        item.put("reasonCode", reasonCode);
        item.put("reasonMessage", reasonMessage);
        item.put("ip", null);
        item.put("createdAt", LocalDateTime.now());
        queue.addFirst(item);
        while (queue.size() > MAX_LOG_HISTORY) {
            queue.removeLast();
        }
    }

    private record OAuthProfile(String oauthId, String nickname, String avatar) {
    }

    // ==================== 微信 / QQ / 企微 URL 与 API（保持原实现） ====================

    private String getWeixinAuthUrl(String redirectUri, String state) {
        return "https://open.weixin.qq.com/connect/qrconnect?"
                + "appid=" + (weixinAppId.isEmpty() ? "wx_mock_appid" : weixinAppId)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code&scope=snsapi_login&state="
                + URLEncoder.encode(state, StandardCharsets.UTF_8) + "#wechat_redirect";
    }

    private Map<String, String> getWeixinUserInfo(String code) {
        String tokenUrl = String.format(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                weixinAppId, weixinAppSecret, code);
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenRes = restTemplate.getForObject(tokenUrl, Map.class);
        if (tokenRes == null) {
            throw new BusinessException("微信授权失败");
        }
        String accessToken = (String) tokenRes.get("access_token");
        String openid = (String) tokenRes.get("openid");
        if (accessToken == null || openid == null) {
            throw new BusinessException("微信授权失败");
        }
        String userInfoUrl = String.format(
                "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s", accessToken, openid);
        @SuppressWarnings("unchecked")
        Map<String, String> userRes = restTemplate.getForObject(userInfoUrl, Map.class);
        if (userRes == null) {
            throw new BusinessException("微信获取用户信息失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("openid", openid);
        result.put("nickname", userRes.getOrDefault("nickname", "微信用户"));
        result.put("headimgurl", userRes.getOrDefault("headimgurl", ""));
        return result;
    }

    private String getQQAuthUrl(String redirectUri, String state) {
        return "https://graph.qq.com/oauth2.0/authorize?"
                + "client_id=" + (qqAppId.isEmpty() ? "qq_mock_appid" : qqAppId)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8)
                + "&scope=get_user_info";
    }

    private String getWeworkAuthUrl(String redirectUri, String state) {
        String corp = weworkCorpId.isEmpty() ? "ww_mock_corp" : weworkCorpId;
        String agent = weworkAgentId.isEmpty() ? "1000001" : weworkAgentId;
        return "https://open.weixin.qq.com/connect/oauth2/authorize?"
                + "appid=" + corp
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code&scope=snsapi_privateinfo&agentid=" + agent
                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) + "#wechat_redirect";
    }

    private Map<String, String> getWeworkUserInfo(String code) {
        if (weworkCorpId.isEmpty()) {
            throw new BusinessException("企业微信未配置");
        }
        String secret = weworkCorpSecret.isEmpty() ? weworkAgentId : weworkCorpSecret;
        String tokenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="
                + weworkCorpId + "&corpsecret=" + secret;
        @SuppressWarnings("unchecked")
        Map<String, Object> tokenRes = restTemplate.getForObject(tokenUrl, Map.class);
        String accessToken = tokenRes != null ? (String) tokenRes.get("access_token") : null;
        if (accessToken == null) {
            throw new BusinessException("企业微信获取 access_token 失败");
        }
        String userUrl = "https://qyapi.weixin.qq.com/cgi-bin/auth/getuserinfo?access_token="
                + accessToken + "&code=" + code;
        @SuppressWarnings("unchecked")
        Map<String, Object> userRes = restTemplate.getForObject(userUrl, Map.class);
        if (userRes == null || userRes.get("userid") == null) {
            throw new BusinessException("企业微信授权失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("userid", String.valueOf(userRes.get("userid")));
        result.put("name", "企业微信用户");
        result.put("avatar", "");
        return result;
    }

    private Map<String, String> getQQUserInfo(String code) {
        String tokenUrl = String.format(
                "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s",
                qqAppId, qqAppSecret, code, URLEncoder.encode(qqRedirectUri, StandardCharsets.UTF_8));
        String tokenStr = restTemplate.getForObject(tokenUrl, String.class);
        String accessToken = extractParam(tokenStr, "access_token");
        if (accessToken == null) {
            throw new BusinessException("QQ授权失败");
        }
        String openidUrl = String.format("https://graph.qq.com/oauth2.0/me?access_token=%s", accessToken);
        String openidStr = restTemplate.getForObject(openidUrl, String.class);
        String openid = extractJsonField(openidStr, "openid");
        if (openid == null) {
            throw new BusinessException("QQ获取openid失败");
        }
        String userInfoUrl = String.format(
                "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s",
                accessToken, qqAppId, openid);
        @SuppressWarnings("unchecked")
        Map<String, String> userRes = restTemplate.getForObject(userInfoUrl, Map.class);
        if (userRes == null) {
            throw new BusinessException("QQ获取用户信息失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("openid", openid);
        result.put("nickname", userRes.getOrDefault("nickname", "QQ用户"));
        result.put("figureurl_qq_2", userRes.getOrDefault("figureurl_qq_2", ""));
        return result;
    }

    private String extractParam(String str, String key) {
        if (str == null) {
            return null;
        }
        for (String pair : str.split("&")) {
            String[] kv = pair.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                return kv[1];
            }
        }
        return null;
    }

    private String extractJsonField(String str, String key) {
        if (str == null) {
            return null;
        }
        int start = str.indexOf("\"" + key + "\"");
        if (start == -1) {
            return null;
        }
        start = str.indexOf(":", start) + 1;
        int end = str.indexOf("\"", start + 1);
        return str.substring(start + 1, end);
    }
}

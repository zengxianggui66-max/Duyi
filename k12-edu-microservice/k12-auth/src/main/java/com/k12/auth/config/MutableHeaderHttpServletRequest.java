package com.k12.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

/**
 * 允许在 Filter 中补充请求头（用于直连 auth 时从 JWT 注入 X-User-Id）
 */
public class MutableHeaderHttpServletRequest extends HttpServletRequestWrapper {

    private final Map<String, String> extraHeaders = new HashMap<>();

    public MutableHeaderHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public void setHeader(String name, String value) {
        extraHeaders.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String extra = extraHeaders.get(name);
        if (extra != null) {
            return extra;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (extraHeaders.containsKey(name)) {
            return Collections.enumeration(List.of(extraHeaders.get(name)));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new LinkedHashSet<>();
        Enumeration<String> parent = super.getHeaderNames();
        while (parent.hasMoreElements()) {
            names.add(parent.nextElement());
        }
        names.addAll(extraHeaders.keySet());
        return Collections.enumeration(names);
    }
}

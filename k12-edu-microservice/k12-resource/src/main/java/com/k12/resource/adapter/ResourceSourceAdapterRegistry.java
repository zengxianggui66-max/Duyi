package com.k12.resource.adapter;

import com.k12.common.BusinessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ResourceSourceAdapterRegistry {

    private final Map<String, ResourceSourceAdapter> byType;

    public ResourceSourceAdapterRegistry(List<ResourceSourceAdapter> adapters) {
        this.byType = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ResourceSourceAdapter::sourceType,
                        Function.identity(),
                        (left, right) -> {
                            throw new IllegalStateException("重复的 sourceType 适配器: " + left.sourceType());
                        }));
    }

    public ResourceSourceAdapter require(String sourceType) {
        ResourceSourceAdapter adapter = byType.get(sourceType);
        if (adapter == null) {
            throw new BusinessException(400, "不支持的资源来源: " + sourceType);
        }
        return adapter;
    }

    public ResourceSourceAdapter get(String sourceType) {
        return byType.get(sourceType);
    }
}

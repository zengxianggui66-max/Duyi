package com.k12.resource.service;

import com.k12.common.dto.ResourceBrandVO;
import com.k12.resource.mapper.EduResourceBrandMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    private final EduResourceBrandMapper eduResourceBrandMapper;
    public BrandService(EduResourceBrandMapper eduResourceBrandMapper) {
        this.eduResourceBrandMapper = eduResourceBrandMapper;
    }


    public List<ResourceBrandVO> listActive(String stage, String subject) {
        // M1：stage/subject 预留，当前返回全部启用品牌
        return eduResourceBrandMapper.listActive().stream()
                .map(ResourceBrandVO::fromEntity)
                .collect(Collectors.toList());
    }
}

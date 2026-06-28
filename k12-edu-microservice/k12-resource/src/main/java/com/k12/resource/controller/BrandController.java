package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.ResourceBrandVO;
import com.k12.resource.service.BrandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;
    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }


    @GetMapping
    public Result<List<ResourceBrandVO>> list(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject) {
        return Result.success(brandService.listActive(stage, subject));
    }
}

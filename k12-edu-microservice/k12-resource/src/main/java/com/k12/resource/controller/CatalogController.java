package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.CatalogBreadcrumbItemVO;
import com.k12.common.dto.CatalogNodeVO;
import com.k12.common.dto.CatalogSchemeVO;
import com.k12.resource.service.CatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;
    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }


    @GetMapping("/schemes")
    public Result<List<CatalogSchemeVO>> listSchemes(
            @RequestParam(required = false) String brandCode,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject) {
        return Result.success(catalogService.listSchemes(brandCode, stage, subject));
    }

    @GetMapping("/tree")
    public Result<List<CatalogNodeVO>> getTree(
            @RequestParam(required = false) Integer schemeId,
            @RequestParam(required = false) String schemeCode,
            @RequestParam(required = false) String volumeKey,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String edition,
            @RequestParam(required = false) String subject) {
        return Result.success(catalogService.getTree(
                schemeId, schemeCode, volumeKey, gradeName, edition, subject));
    }

    @GetMapping("/node/{id}/breadcrumb")
    public Result<List<CatalogBreadcrumbItemVO>> getBreadcrumb(@PathVariable Long id) {
        return Result.success(catalogService.getBreadcrumb(id));
    }
}

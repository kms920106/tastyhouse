package com.tastyhouse.webapi.banner;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.webapi.banner.response.BannerListItemResponse;

@RestController
@RequestMapping("/api/banners")
@Tag(name = "Banner", description = "배너 관리 API")
public class BannerApiController {

    private final BannerQueryService bannerQueryService;

    public BannerApiController(BannerQueryService bannerQueryService) {
        this.bannerQueryService = bannerQueryService;
    }

    @Operation(summary = "홈 배너 목록 조회")
    @GetMapping("/v1/home")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getHomeBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<BannerListItemResponse> pageResponse = bannerQueryService.getHomeBanners(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "사이드바 배너 목록 조회")
    @GetMapping("/v1/sidebar")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getSidebarBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<BannerListItemResponse> pageResponse = bannerQueryService.getSidebarBanners(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }
}

package com.tastyhouse.webapi.banner.adapter.in.web;

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
import com.tastyhouse.application.banner.port.in.BannerQueryUseCase;
import com.tastyhouse.webapi.banner.adapter.in.web.response.BannerListItemResponse;

@RestController
@RequestMapping("/api/banners")
@Tag(name = "Banner", description = "배너 관리 API")
public class BannerApiController {

    private final BannerQueryUseCase bannerQueryService;

    public BannerApiController(BannerQueryUseCase bannerQueryService) {
        this.bannerQueryService = bannerQueryService;
    }

    @Operation(summary = "홈 배너 목록 조회")
    @GetMapping("/v1/home")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getHomeBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<BannerListItemResponse> pageResponse = PaginationResponse.from(
            bannerQueryService.getHomeBanners(pageRequest.page(), pageRequest.size())
                .map(BannerListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "사이드바 배너 목록 조회")
    @GetMapping("/v1/sidebar")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getSidebarBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PaginationResponse<BannerListItemResponse> pageResponse = PaginationResponse.from(
            bannerQueryService.getSidebarBanners(pageRequest.page(), pageRequest.size())
                .map(BannerListItemResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }
}

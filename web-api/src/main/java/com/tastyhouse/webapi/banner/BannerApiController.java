package com.tastyhouse.webapi.banner;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.banner.response.BannerListItemResponse;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Tag(name = "Banner", description = "배너 관리 API")
public class BannerApiController {

    private final BannerService bannerService;

    @Operation(summary = "홈 배너 목록 조회")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/home")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getHomeBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResponse<BannerListItemResponse> pageResult = bannerService.findHomeBanners(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }

    @Operation(summary = "사이드바 배너 목록 조회")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/sidebar")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getSidebarBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResponse<BannerListItemResponse> pageResult = bannerService.findSidebarBanners(pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }
}

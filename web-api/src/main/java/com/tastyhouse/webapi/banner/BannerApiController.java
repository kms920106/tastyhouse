package com.tastyhouse.webapi.banner;

import com.tastyhouse.core.common.CommonResponse;
import com.tastyhouse.webapi.banner.response.BannerListItem;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.core.common.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/home")
    public ResponseEntity<CommonResponse<List<BannerListItem>>> getHomeBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<BannerListItem> pageResult = bannerService.findHomeBanners(pageRequest);
        return ResponseEntity.ok(CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }

    @Operation(summary = "사이드바 배너 목록 조회")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class)))})
    @GetMapping("/v1/sidebar")
    public ResponseEntity<CommonResponse<List<BannerListItem>>> getSidebarBanners(@Valid @ModelAttribute PageRequest pageRequest) {
        PageResult<BannerListItem> pageResult = bannerService.findSidebarBanners(pageRequest);
        return ResponseEntity.ok(CommonResponse.success(pageResult.getContent(), pageRequest.page(), pageRequest.size(), pageResult.getTotalElements()));
    }
}

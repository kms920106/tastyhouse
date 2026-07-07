package com.tastyhouse.adminapi.banner;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.adminapi.banner.request.BannerCreateRequest;
import com.tastyhouse.adminapi.banner.request.BannerUpdateRequest;
import com.tastyhouse.adminapi.banner.response.BannerDetailResponse;
import com.tastyhouse.adminapi.banner.response.BannerListItemResponse;
import com.tastyhouse.adminapi.banner.response.BannerPageResponse;
import com.tastyhouse.adminapi.common.ApiResponse;
import com.tastyhouse.adminapi.common.PageRequest;

@Tag(name = "Banner Admin", description = "배너 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/banners")
public class BannerApiController {

    private final BannerService bannerService;

    @Operation(summary = "배너 목록 조회", description = "배너 목록을 페이징 조회합니다. (비노출·노출기간 만료 배너 포함) type 미지정 시 전체 유형 조회")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getBanners(
        @RequestParam(required = false) BannerType type,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        BannerPageResponse pageResponse = bannerService.getBanners(type, pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "배너 등록", description = "새로운 배너를 등록합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createBanner(@Valid @RequestBody BannerCreateRequest request) {
        Long id = bannerService.createBanner(
            request.type(),
            request.title(),
            request.imageFileId(),
            request.linkUrl(),
            request.startDate(),
            request.endDate(),
            request.sort(),
            request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "배너 상세 조회", description = "배너 상세를 조회합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<BannerDetailResponse>> getBanner(@PathVariable Long id) {
        BannerDetailResponse response = bannerService.getBanner(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배너 수정", description = "기존 배너를 수정합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBanner(
        @PathVariable Long id,
        @Valid @RequestBody BannerUpdateRequest request
    ) {
        bannerService.updateBanner(
            id,
            request.type(),
            request.title(),
            request.imageFileId(),
            request.linkUrl(),
            request.startDate(),
            request.endDate(),
            request.sort(),
            request.visible()
        );
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배너 삭제", description = "기존 배너를 삭제합니다.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

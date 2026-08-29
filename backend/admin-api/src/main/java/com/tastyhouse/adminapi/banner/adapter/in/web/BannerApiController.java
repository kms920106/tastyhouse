package com.tastyhouse.adminapi.banner.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.banner.adapter.in.web.request.BannerCreateRequest;
import com.tastyhouse.adminapi.banner.adapter.in.web.request.BannerSearchRequest;
import com.tastyhouse.adminapi.banner.adapter.in.web.request.BannerUpdateRequest;
import com.tastyhouse.adminapi.banner.adapter.in.web.response.BannerDetailResponse;
import com.tastyhouse.adminapi.banner.adapter.in.web.response.BannerListItemResponse;
import com.tastyhouse.adminapi.banner.application.port.in.BannerCommandUseCase;
import com.tastyhouse.adminapi.banner.application.port.in.BannerCreateCommand;
import com.tastyhouse.adminapi.banner.application.port.in.BannerDeleteCommand;
import com.tastyhouse.adminapi.banner.application.port.in.BannerUpdateCommand;
import com.tastyhouse.adminapi.banner.application.port.in.BannerQueryUseCase;

@Tag(name = "Banner Admin", description = "배너 관리자 API")
@RestController
@RequestMapping("/api/banners")
public class BannerApiController {

    private final BannerCommandUseCase bannerCommandUseCase;
    private final BannerQueryUseCase bannerQueryUseCase;

    public BannerApiController(BannerCommandUseCase bannerCommandUseCase, BannerQueryUseCase bannerQueryUseCase) {
        this.bannerCommandUseCase = bannerCommandUseCase;
        this.bannerQueryUseCase = bannerQueryUseCase;
    }

    @Operation(summary = "배너 목록 조회", description = "배너 목록을 페이징 조회합니다. (비노출·노출기간 만료 배너 포함) type 미지정 시 전체 유형 조회, title은 부분 일치 검색, visible은 null=전체/true=노출/false=비노출")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<BannerListItemResponse>>> getBanners(
        @Valid @ModelAttribute BannerSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<BannerListItemResponse> pageResponse = bannerQueryUseCase.getBanners(search.type(), search.title(), search.visible(), pageRequest.page(), pageRequest.size());
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "배너 등록", description = "새로운 배너를 등록합니다.")
    @PostMapping("/v1")
    public ResponseEntity<ApiResponse<Long>> createBanner(@Valid @RequestBody BannerCreateRequest request) {
        BannerCreateCommand command = request.toCommand();
        Long id = bannerCommandUseCase.createBanner(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "배너 상세 조회", description = "배너 상세를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<BannerDetailResponse>> getBanner(@PathVariable Long id) {
        BannerDetailResponse response = bannerQueryUseCase.getBanner(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "배너 수정", description = "기존 배너를 수정합니다.")
    @PutMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> updateBanner(
        @PathVariable Long id,
        @Valid @RequestBody BannerUpdateRequest request
    ) {
        BannerUpdateCommand command = request.toCommand(id);
        bannerCommandUseCase.updateBanner(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "배너 삭제", description = "기존 배너를 삭제합니다.")
    @DeleteMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        BannerDeleteCommand command = BannerDeleteCommand.of(id);
        bannerCommandUseCase.deleteBanner(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

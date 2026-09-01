package com.tastyhouse.ceoapi.region.adapter.in.web;

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
import com.tastyhouse.application.region.port.out.AdminDongItemResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.ceoapplication.region.port.in.AdminDongQueryUseCase;
import com.tastyhouse.ceoapi.region.adapter.in.web.request.AdminDongBoundarySearchRequest;
import com.tastyhouse.ceoapi.region.adapter.in.web.request.AdminDongSearchRequest;
import com.tastyhouse.ceoapi.region.adapter.in.web.request.AdminDongTreeRequest;
import com.tastyhouse.ceoapi.region.adapter.in.web.response.AdminDongBoundaryResponse;
import com.tastyhouse.ceoapi.region.adapter.in.web.response.AdminDongItemResponse;
import com.tastyhouse.ceoapi.region.adapter.in.web.response.AdminDongTreeResponse;

@Tag(name = "Ceo Admin Dong", description = "점주 행정동 검색 API")
@RestController
@RequestMapping("/api/admin-dongs")
public class AdminDongApiController {

    private final AdminDongQueryUseCase adminDongQueryService;

    public AdminDongApiController(AdminDongQueryUseCase adminDongQueryService) {
        this.adminDongQueryService = adminDongQueryService;
    }

    @Operation(summary = "행정동 검색", description = "배달가능지역으로 등록할 행정동을 검색합니다. 사용 중인 행정동만 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<AdminDongItemResponse>>> getAdminDongs(
        @Valid @ModelAttribute AdminDongSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<AdminDongItemResult> pageResult = adminDongQueryService.getAdminDongs(
            search.keyword(), pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<AdminDongItemResponse> pageResponse =
            PaginationResponse.from(pageResult.map(AdminDongItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }

    @Operation(
        summary = "행정동 계층 조회",
        description = "시/도 → 시/군/구 → 행정동을 한 단계씩 조회합니다. 파라미터를 비우면 시/도 목록, sidoName만 지정하면 시/군/구 목록, 둘 다 지정하면 행정동 목록을 반환합니다."
    )
    @GetMapping("/v1/tree")
    public ResponseEntity<ApiResponse<AdminDongTreeResponse>> getAdminDongTree(
        @Valid @ModelAttribute AdminDongTreeRequest search
    ) {
        AdminDongTreeResponse response = AdminDongTreeResponse.from(
            adminDongQueryService.getAdminDongTree(search.sidoName(), search.sigunguName())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "행정동 경계 조회",
        description = "지도 영역(bbox) 또는 행정동 ID 목록으로 경계 폴리곤을 조회합니다. 두 조건은 배타적이며, 영역이 너무 넓으면 오류 대신 빈 목록과 truncated=true를 반환합니다."
    )
    @GetMapping("/v1/boundaries")
    public ResponseEntity<ApiResponse<AdminDongBoundaryResponse>> getAdminDongBoundaries(
        @Valid @ModelAttribute AdminDongBoundarySearchRequest search
    ) {
        AdminDongBoundaryResponse response = AdminDongBoundaryResponse.from(
            adminDongQueryService.getAdminDongBoundaries(
                search.swLat(),
                search.swLng(),
                search.neLat(),
                search.neLng(),
                search.level(),
                search.adminDongIds()
            )
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

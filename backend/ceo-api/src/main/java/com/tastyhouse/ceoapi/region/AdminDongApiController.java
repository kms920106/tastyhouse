package com.tastyhouse.ceoapi.region;

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
import com.tastyhouse.ceoapi.region.request.AdminDongSearchRequest;
import com.tastyhouse.ceoapi.region.response.AdminDongItemResponse;

@Tag(name = "Ceo Admin Dong", description = "점주 행정동 검색 API")
@RestController
@RequestMapping("/api/admin-dongs")
public class AdminDongApiController {

    private final AdminDongQueryService adminDongQueryService;

    public AdminDongApiController(AdminDongQueryService adminDongQueryService) {
        this.adminDongQueryService = adminDongQueryService;
    }

    @Operation(summary = "행정동 검색", description = "배달가능지역으로 등록할 행정동을 검색합니다. 사용 중인 행정동만 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<AdminDongItemResponse>>> getAdminDongs(
        @Valid @ModelAttribute AdminDongSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PaginationResponse<AdminDongItemResponse> pageResponse = adminDongQueryService.getAdminDongs(
            search.keyword(), pageRequest.page(), pageRequest.size()
        );
        return ResponseEntity.ok(ApiResponse.success(
            pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()
        ));
    }
}

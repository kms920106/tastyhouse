package com.tastyhouse.adminapi.ceo.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.ceo.adapter.in.web.response.CeoListItemResponse;
import com.tastyhouse.adminapplication.ceo.port.in.CeoManagementQueryUseCase;

@Tag(name = "Ceo Admin", description = "점주 관리자 API")
@RestController
@RequestMapping("/api/ceos")
public class CeoApiController {

    private final CeoManagementQueryUseCase ceoQueryUseCase;

    public CeoApiController(CeoManagementQueryUseCase ceoQueryUseCase) {
        this.ceoQueryUseCase = ceoQueryUseCase;
    }

    @Operation(summary = "점주 목록 조회", description = "가게 배정용 점주 Select 드롭다운을 위한 전체 점주 목록을 조회합니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<CeoListItemResponse>>> getCeos() {
        List<CeoListItemResponse> ceos = ceoQueryUseCase.getCeos().stream()
            .map(CeoListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(ceos));
    }
}

package com.tastyhouse.webapi.grade.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.grade.port.in.GradeQueryUseCase;
import com.tastyhouse.webapi.grade.adapter.in.web.response.GradeInfoListItemResponse;

@Tag(name = "Grade", description = "등급 정책 API")
@RestController
@RequestMapping("/api/grades")
public class GradeApiController {

    private final GradeQueryUseCase gradeQueryService;

    public GradeApiController(GradeQueryUseCase gradeQueryService) {
        this.gradeQueryService = gradeQueryService;
    }

    @Operation(summary = "등급 세부 조건 목록 조회", description = "전체 등급의 이름과 달성 조건(최소/최대 리뷰 개수)을 조회합니다. 인증 불필요.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<GradeInfoListItemResponse>>> getGradeInfoList() {
        List<GradeInfoListItemResponse> gradeInfoList = gradeQueryService.getGradeInfoList().stream()
            .map(GradeInfoListItemResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(gradeInfoList));
    }
}

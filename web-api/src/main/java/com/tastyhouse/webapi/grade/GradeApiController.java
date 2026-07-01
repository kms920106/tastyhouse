package com.tastyhouse.webapi.grade;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.grade.response.GradeInfoListItemResponse;

@Tag(name = "Grade", description = "등급 정책 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/grades")
public class GradeApiController {

    private final GradeService gradeService;

    @Operation(summary = "등급 세부 조건 목록 조회", description = "전체 등급의 이름과 달성 조건(최소/최대 리뷰 개수)을 조회합니다. 인증 불필요.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = ApiResponse.class)))})
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<GradeInfoListItemResponse>>> getGradeInfoList() {
        List<GradeInfoListItemResponse> gradeInfoList = gradeService.getGradeInfoList();
        return ResponseEntity.ok(ApiResponse.success(gradeInfoList));
    }
}

package com.tastyhouse.adminapi.member.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.apicommon.common.PageRequest;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.member.adapter.in.web.request.MemberSearchRequest;
import com.tastyhouse.adminapi.member.adapter.in.web.request.MemberWithdrawRequest;
import com.tastyhouse.adminapi.member.adapter.in.web.response.MemberDetailResponse;
import com.tastyhouse.adminapi.member.adapter.in.web.response.MemberListItemResponse;
import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.adminapplication.member.port.in.MemberActivateCommand;
import com.tastyhouse.adminapplication.member.port.in.MemberManagementCommandUseCase;
import com.tastyhouse.adminapplication.member.port.in.MemberSuspendCommand;
import com.tastyhouse.adminapplication.member.port.in.MemberManagementWithdrawCommand;
import com.tastyhouse.adminapplication.member.port.in.MemberManagementQueryUseCase;

@Tag(name = "Member Admin", description = "회원 관리자 API")
@RestController
@RequestMapping("/api/members")
public class MemberApiController {

    private final MemberManagementQueryUseCase memberQueryUseCase;
    private final MemberManagementCommandUseCase memberCommandUseCase;

    public MemberApiController(MemberManagementQueryUseCase memberQueryUseCase, MemberManagementCommandUseCase memberCommandUseCase) {
        this.memberQueryUseCase = memberQueryUseCase;
        this.memberCommandUseCase = memberCommandUseCase;
    }

    @Operation(summary = "회원 목록 조회", description = "회원 목록을 페이징 조회합니다. nickname/username/phone은 부분 일치 검색, status/grade는 필터(미지정 시 전체)입니다.")
    @GetMapping("/v1")
    public ResponseEntity<ApiResponse<List<MemberListItemResponse>>> getMembers(
        @Valid @ModelAttribute MemberSearchRequest search,
        @Valid @ModelAttribute PageRequest pageRequest
    ) {
        PageResult<MemberListItemResult> pageResult = memberQueryUseCase.getMembers(
            search.nickname(), search.username(), search.phone(), search.status(), search.grade(),
            pageRequest.page(), pageRequest.size()
        );
        PaginationResponse<MemberListItemResponse> pageResponse = PaginationResponse.from(pageResult.map(MemberListItemResponse::from));
        return ResponseEntity.ok(ApiResponse.success(pageResponse.content(), pageResponse.page(), pageResponse.size(), pageResponse.totalElements()));
    }

    @Operation(summary = "회원 상세 조회", description = "회원 상세 정보를 조회합니다.")
    @GetMapping("/v1/{id}")
    public ResponseEntity<ApiResponse<MemberDetailResponse>> getMember(@PathVariable Long id) {
        MemberDetailResponse response = MemberDetailResponse.from(memberQueryUseCase.getMember(id));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "회원 정지", description = "회원 상태를 ACTIVE에서 SUSPENDED로 변경합니다.")
    @PatchMapping("/v1/{id}/suspend")
    public ResponseEntity<ApiResponse<Void>> suspend(@PathVariable Long id) {
        MemberSuspendCommand command = MemberSuspendCommand.of(id);
        memberCommandUseCase.suspend(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 정지 해제", description = "회원 상태를 SUSPENDED에서 ACTIVE로 변경합니다.")
    @PatchMapping("/v1/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable Long id) {
        MemberActivateCommand command = MemberActivateCommand.of(id);
        memberCommandUseCase.activate(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 강제 탈퇴", description = "관리자가 회원을 강제로 탈퇴 처리합니다. 상태가 DELETED로 변경되고 탈퇴 사유가 기록됩니다.")
    @PostMapping("/v1/{id}/withdrawal")
    public ResponseEntity<ApiResponse<Void>> withdraw(
        @PathVariable Long id,
        @Valid @RequestBody MemberWithdrawRequest request
    ) {
        MemberManagementWithdrawCommand command = request.toCommand(id);
        memberCommandUseCase.withdraw(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

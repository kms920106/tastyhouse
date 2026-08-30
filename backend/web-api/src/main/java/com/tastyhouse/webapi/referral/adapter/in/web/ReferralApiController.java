package com.tastyhouse.webapi.referral.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.webapplication.auth.security.CustomUserDetails;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapplication.referral.port.in.ReferralQueryUseCase;
import com.tastyhouse.webapplication.referral.response.ReferralMemberListItemResponse;

@RestController
@RequestMapping("/api/referrals")
@Tag(name = "Referral", description = "추천인 API")
public class ReferralApiController {

    private final ReferralQueryUseCase referralQueryService;

    public ReferralApiController(ReferralQueryUseCase referralQueryService) {
        this.referralQueryService = referralQueryService;
    }

    @Operation(summary = "내 추천 이력 조회", description = "내가 추천한 회원 목록과 보상 상태를 조회합니다.")
    @GetMapping("/v1/my")
    public ResponseEntity<ApiResponse<List<ReferralMemberListItemResponse>>> getMyReferrals(
        @CurrentUser CustomUserDetails userDetails
    ) {
        List<ReferralMemberListItemResponse> referrals = referralQueryService.getMyReferrals(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(referrals));
    }
}

package com.tastyhouse.webapi.referral;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.webapi.common.ApiResponse;
import com.tastyhouse.webapi.referral.response.MemberReferralListItemResponse;
import com.tastyhouse.webapi.security.CurrentUser;
import com.tastyhouse.webapi.service.CustomUserDetails;

@RestController
@RequestMapping("/api/referrals")
@RequiredArgsConstructor
@Tag(name = "Referral", description = "추천인 API")
public class ReferralApiController {

    private final ReferralService referralService;

    @Operation(summary = "내 추천 이력 조회", description = "내가 추천한 회원 목록과 보상 상태를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    @GetMapping("/v1/my")
    public ResponseEntity<ApiResponse<List<MemberReferralListItemResponse>>> getMyReferrals(
        @CurrentUser CustomUserDetails userDetails
    ) {
        List<MemberReferralListItemResponse> referrals = referralService.getMyReferrals(userDetails.getMemberId());
        return ResponseEntity.ok(ApiResponse.success(referrals));
    }
}

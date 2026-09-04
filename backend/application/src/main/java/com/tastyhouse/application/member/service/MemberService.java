package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import org.springframework.stereotype.Component;


import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.application.review.port.out.MyReviewListItemResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;
import com.tastyhouse.application.coupon.service.CouponQueryService;
import com.tastyhouse.application.member.port.out.MemberStatsResult;
import com.tastyhouse.application.member.port.out.MyGradeResult;
import com.tastyhouse.application.member.port.in.MemberCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberScreenUseCase;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;

/**
 * 내 정보 화면 컨트롤러 파사드.
 *
 * <p>회원 자체의 조회·변경은 CQRS 분리에 따라 {@link MemberQueryService}/{@link MemberCommandService}가
 * 담당하고, 이 클래스는 "토큰 검증 후 변경"처럼 여러 협력자를 순서대로 엮는 화면 단위 흐름과, 내 정보
 * 화면이 함께 보여주는 다른 컨텍스트(쿠폰·리뷰·북마크·등급·회원 통계) 위임만 얇게 유지한다.
 *
 * <p>반환 타입은 읽기 계약({@code *Result}·{@code PageResult})이며 응답 record를 조립하지 않는다 —
 * Response 조립은 web-api의 컨트롤러가 담당한다(챕터 10). 감쌀 값이 하나뿐인 연산(사용 가능 여부·리뷰
 * 개수·인증 토큰)은 경계 타입({@code boolean}/{@code long}/{@code String})을 그대로 내보내고 컨트롤러가 감싼다.
 *
 * <p><b>트랜잭션 원자성 판정</b> — 이 파사드는 의도적으로 {@code @Transactional}을 갖지 않는다. 파사드가
 * 트랜잭션을 열면 아래 판정에서 "DB 원자성이 필요 없다"고 판단된 단계(JWT 서명 검증·Redis 접근)까지 DB
 * 커넥션을 점유하게 되므로, 원자성이 실제로 필요한 구간만 하위 CommandService가 단일 트랜잭션으로 갖는다.
 * 단계별 판정 결과는 각 메서드 Javadoc에 남긴다.
 *
 * <p>판정 기준은 하나다 — <b>"이 단계가 DB에서 읽은 값에 근거해 DB를 쓰는가(read-then-write)?"</b>. 그렇다면
 * 검증과 쓰기가 같은 트랜잭션·같은 로드 안에 있어야 하고(그렇지 않으면 검증 후 쓰기 사이에 상태가 바뀌어
 * 검사를 우회할 수 있다), 아니라면 굳이 묶지 않는다.
 */
@Component
@WebApp
public class MemberService implements MemberScreenUseCase {

    private final MemberQueryService memberQueryService;
    private final MemberCommandUseCase memberCommandUseCase;
    private final MemberAuthService memberAuthService;
    private final MemberStatsQueryService memberStatsQueryService;
    private final MemberShopService memberShopService;
    private final MemberReviewService memberReviewService;
    private final CouponQueryService couponQueryService;
    private final MemberGradeService memberGradeService;

    public MemberService(
        MemberQueryService memberQueryService,
        MemberCommandUseCase memberCommandUseCase,
        MemberAuthService memberAuthService,
        MemberStatsQueryService memberStatsQueryService,
        MemberShopService memberShopService,
        MemberReviewService memberReviewService,
        CouponQueryService couponQueryService,
        MemberGradeService memberGradeService
    ) {
        this.memberQueryService = memberQueryService;
        this.memberCommandUseCase = memberCommandUseCase;
        this.memberAuthService = memberAuthService;
        this.memberStatsQueryService = memberStatsQueryService;
        this.memberShopService = memberShopService;
        this.memberReviewService = memberReviewService;
        this.couponQueryService = couponQueryService;
        this.memberGradeService = memberGradeService;
    }

    @Override
    public void updateMyProfile(MemberProfileUpdateCommand command) {
        memberCommandUseCase.updateProfile(command);
    }

    @Override
    public String verifyPasswordAndIssueToken(Long memberId, String password) {
        memberAuthService.verifyPassword(memberId, password);
        return memberAuthService.createPersonalInfoVerifyToken(memberId);
    }

    @Override
    public MemberPersonalInfoResult getPersonalInfo(Long memberId) {
        return memberQueryService.getPersonalInfo(memberId);
    }

    /**
     * 개인정보 변경 — 본인인증 토큰(+번호 변경 시 휴대폰 인증 토큰)을 검증한 뒤 변경한다.
     *
     * <p><b>원자성 판정: 묶지 않는다(비원자 허용).</b> 두 검증
     * ({@link MemberAuthService#verifyPersonalInfoToken}·{@link MemberAuthService#verifyPhoneToken})은
     * <em>JWT 서명·클레임 검증만</em> 수행하며 DB를 읽지 않는다(토큰 자체가 발급 시점의 인증 사실을 서명으로
     * 담고 있다). 따라서 "검증 시점과 변경 시점 사이에 DB 상태가 바뀌어 검사를 우회한다"는 read-then-write
     * 경합이 성립하지 않으므로 검증과 변경을 한 트랜잭션으로 묶을 이득이 없다. 실제 DB write는
     * {@link MemberCommandService#updatePersonalInfo} 한 번뿐이고 그 메서드가 자체 트랜잭션을 가지므로,
     * 이 유스케이스의 DB 변경은 이미 단일 트랜잭션이다.
     */
    @Override
    public void updatePersonalInfo(MemberPersonalInfoUpdateCommand command, String verifyToken, String smsVerifyToken) {
        Long memberId = command.memberId();
        String phoneNumber = command.phoneNumber();
        memberAuthService.verifyPersonalInfoToken(memberId, verifyToken);
        if (phoneNumber != null) {
            memberAuthService.verifyPhoneToken(memberId, smsVerifyToken, phoneNumber);
        }
        memberCommandUseCase.updatePersonalInfo(command);
    }

    /**
     * 비밀번호 변경 — 본인인증 토큰을 검증한 뒤 변경한다.
     *
     * <p><b>원자성 판정: 묶었다(단일 트랜잭션으로 하강).</b> 본인인증 토큰 검증은 위와 같이 JWT 서명뿐이라
     * DB와 무관하지만, "새 비밀번호가 기존과 같은지" 검사는 <em>DB에서 읽은 현재 비밀번호</em>에 근거해
     * DB를 쓰는 read-then-write다. 과거에는 이 검사가 {@code MemberAuthService.verifyNotSamePassword}의
     * 별도 readOnly 트랜잭션에서 수행돼 검사와 변경이 두 트랜잭션·두 번의 회원 로드로 쪼개져 있었다
     * (검사 후 변경 사이에 비밀번호가 바뀌면 검사를 우회 가능). 그 검사를
     * {@link MemberCommandService#updatePassword} 안으로 내려 단일 트랜잭션·단일 로드로 원자화했고,
     * 예외 코드·검사 순서는 그대로 보존했다.
     */
    @Override
    public void updatePassword(MemberPasswordUpdateCommand command, String verifyToken) {
        memberAuthService.verifyPersonalInfoToken(command.memberId(), verifyToken);
        memberCommandUseCase.updatePassword(command);
    }

    /**
     * 회원 탈퇴 — 탈퇴 처리 후 액세스 토큰을 무효화한다.
     *
     * <p><b>원자성 판정: 묶지 않는다(묶으면 오히려 틀린다).</b> 탈퇴({@link MemberCommandService#withdraw})는
     * 자체 트랜잭션 안의 DB 변경이고, 토큰 무효화({@link MemberAuthService#invalidateAccessToken})는
     * <em>Redis 블랙리스트 등록</em>이라 DB 트랜잭션과 무관하다. 오히려 <b>순서가 중요</b>하다 — 탈퇴가
     * 커밋된 뒤 토큰을 무효화해야 하며, 한 트랜잭션에 넣으면 Redis 등록이 커밋 전에 일어나 탈퇴가 롤백된
     * 경우에도 토큰만 죽는 불일치가 남는다. 현재 호출 순서가 그 요구를 만족한다.
     */
    @Override
    public void withdrawMember(MemberWithdrawCommand command, String bearerToken) {
        memberCommandUseCase.withdraw(command);
        memberAuthService.invalidateAccessToken(bearerToken);
    }

    @Override
    public boolean checkNicknameAvailability(String nickname) {
        return memberQueryService.checkNicknameAvailability(nickname);
    }

    @Override
    public boolean checkPhoneAvailability(String phoneNumber) {
        return memberQueryService.checkPhoneAvailability(phoneNumber);
    }

    @Override
    public MyGradeResult getMyGrade(Long memberId) {
        return memberGradeService.getMyGrade(memberId);
    }

    @Override
    public List<MyCouponListItemResult> getMyCoupons(Long memberId) {
        return couponQueryService.getMyCoupons(memberId);
    }

    @Override
    public List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId) {
        return couponQueryService.getMyAvailableCoupons(memberId);
    }

    @Override
    public PageResult<MyReviewListItemResult> getMyReviews(Long memberId, int page, int size) {
        return memberReviewService.getMyReviews(memberId, page, size);
    }

    @Override
    public long getMyReviewCount(Long memberId) {
        return memberReviewService.getMyReviewCount(memberId);
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size) {
        return memberShopService.getMyBookmarkedShops(memberId, page, size);
    }

    @Override
    public MemberWithProfileImageResult getMemberBasicProfile(Long targetMemberId) {
        return memberQueryService.getMemberProfile(targetMemberId);
    }

    @Override
    public MemberWithProfileImageResult getMyProfile(Long memberId) {
        return memberQueryService.getMyProfile(memberId);
    }

    @Override
    public MemberStatsResult getMemberStats(Long memberId) {
        return memberStatsQueryService.getMemberStats(memberId);
    }
}

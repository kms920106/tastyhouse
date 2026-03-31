package com.tastyhouse.webapi.member;

import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.entity.place.dto.MyBookmarkedPlaceItemDto;
import com.tastyhouse.core.entity.referral.MemberReferral;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.MemberWithdrawal;
import com.tastyhouse.core.entity.user.WithdrawalReason;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.*;
import com.tastyhouse.file.FileService;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.coupon.CouponService;
import com.tastyhouse.webapi.coupon.response.MemberCouponListItemResponse;
import com.tastyhouse.webapi.exception.UnauthorizedException;
import com.tastyhouse.webapi.member.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final FileService fileService;
    private final CouponService couponService;
    private final MemberCoreService memberCoreService;
    private final PointCoreService pointCoreService;
    private final ReviewCoreService reviewCoreService;
    private final PlaceCoreService placeCoreService;
    private final FollowCoreService followCoreService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Transactional
    public void signUp(String username, String password,
                       String nickname, String fullName,
                       com.tastyhouse.core.entity.user.Gender gender,
                       Integer birthDate, String phoneNumber,
                       Boolean pushNotificationEnabled,
                       Boolean marketingInfoEnabled, Boolean eventInfoEnabled,
                       String phoneVerifyToken, String emailVerifyToken,
                       String referrerNickname) {

        if (memberCoreService.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberCoreService.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (!org.springframework.util.StringUtils.hasText(phoneVerifyToken) || !jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_SIGNUP_PHONE_REQUIRED);
        }

        String verifiedPhone = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        if (!verifiedPhone.equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
        }

        if (!StringUtils.hasText(emailVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_SIGNUP_EMAIL_REQUIRED);
        }

        if (!jwtTokenProvider.validateEmailVerifyToken(emailVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_AUTH_EXPIRED);
        }

        String verifiedEmail = jwtTokenProvider.getEmailFromEmailVerifyToken(emailVerifyToken);
        if (!verifiedEmail.equals(username)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_MISMATCH);
        }

        if (memberCoreService.existsByPhoneNumberValueAndMemberStatusNot(phoneNumber, com.tastyhouse.core.entity.user.MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = new Member(username, passwordEncoder.encode(password), nickname, fullName, gender, birthDate, phoneNumber, pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
        memberCoreService.save(member);

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }

            Member referrer = memberCoreService.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));

            memberCoreService.saveReferral(
                MemberReferral.builder()
                    .referrerId(referrer.getId())
                    .refereeId(member.getId())
                    .build()
            );
        }
    }

    // 개인정보 수정용 본인인증 토큰의 유효성과 회원 일치 여부를 검증
    public void verifyPersonalInfoToken(Long memberId, String verifyToken) {
        if (!jwtTokenProvider.validateVerifyToken(verifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_INFO_AUTH_EXPIRED);
        }

        Long verifiedMemberId = jwtTokenProvider.getMemberIdFromVerifyToken(verifyToken);
        if (!verifiedMemberId.equals(memberId)) {
            throw new UnauthorizedException("인증 정보가 일치하지 않습니다.");
        }
    }

    // 휴대폰 인증 토큰의 유효성과 회원·번호 일치 여부를 검증
    public void verifyPhoneToken(Long memberId, String phoneVerifyToken, String phoneNumber) {
        if (!StringUtils.hasText(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_SMS_REQUIRED);
        }

        if (!jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_AUTH_EXPIRED);
        }

        Long phoneVerifiedMemberId = jwtTokenProvider.getMemberIdFromPhoneVerifyToken(phoneVerifyToken);
        if (!phoneVerifiedMemberId.equals(memberId)) {
            throw new UnauthorizedException("휴대폰 인증 정보가 일치하지 않습니다.");
        }

        String verifiedPhoneNumber = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
        if (!verifiedPhoneNumber.equals(phoneNumber)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
        }
    }

    // 닉네임 중복 여부를 확인하여 사용 가능 여부를 반환
    @Transactional(readOnly = true)
    public NicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        boolean available = !memberCoreService.existsByNickname(nickname);
        return new NicknameAvailabilityResponse(available);
    }

    // 휴대폰번호로 활성 회원 존재 여부를 확인하여 가입 가능 여부를 반환
    @Transactional(readOnly = true)
    public PhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        boolean available = !memberCoreService.existsByPhoneNumberValueAndMemberStatusNot(
            phoneNumber, MemberStatus.DELETED
        );
        return new PhoneAvailabilityResponse(available);
    }

    // 입력한 비밀번호가 저장된 비밀번호와 일치하는지 검증
    @Transactional(readOnly = true)
    public void verifyPassword(Long memberId, String rawPassword) {
        Member member = memberCoreService.getById(memberId);

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
    }

    // 회원의 개인정보를 조회하여 반환
    @Transactional(readOnly = true)
    public PersonalInfoResponse getPersonalInfo(Long memberId) {
        Member member = memberCoreService.getById(memberId);
        return PersonalInfoResponse.from(member);
    }

    // 회원을 비활성화하고 탈퇴 사유를 저장
    @Transactional
    public void withdrawMember(Long memberId, WithdrawalReason reason, String reasonDetail) {
        memberCoreService.getById(memberId).deactivate();

        memberCoreService.saveWithdrawal(
            MemberWithdrawal.builder()
                .memberId(memberId)
                .reason(reason)
                .reasonDetail(reasonDetail)
                .build()
        );
    }

    // 회원의 보유 포인트 및 이번 달 소멸 예정 포인트를 조회
    @Transactional(readOnly = true)
    public PointResponse getMemberPoint(Long memberId) {
        return pointCoreService.findMemberPoint(memberId)
            .map(PointResponse::from)
            .orElseGet(() -> new PointResponse(0, 0));
    }

    // 회원의 포인트 적립·사용 내역을 최신순으로 조회
    @Transactional(readOnly = true)
    public PointHistoryResponse getPointHistory(Long memberId) {
        PointResponse pointResponse = getMemberPoint(memberId);

        List<PointHistoryItemResponse> histories = pointCoreService.findPointHistory(memberId)
            .stream()
            .map(PointHistoryItemResponse::from)
            .collect(Collectors.toList());

        return new PointHistoryResponse(pointResponse.availablePoints(), pointResponse.expiredThisMonth(), histories);
    }

    // 회원이 보유한 전체 쿠폰 목록을 조회
    @Transactional(readOnly = true)
    public List<MemberCouponListItemResponse> getMemberCoupons(Long memberId) {
        return couponService.getMemberCoupons(memberId);
    }

    // 회원이 현재 사용 가능한 쿠폰 목록만 조회
    @Transactional(readOnly = true)
    public List<MemberCouponListItemResponse> getAvailableMemberCoupons(Long memberId) {
        return couponService.getAvailableMemberCoupons(memberId);
    }

    // 회원이 즉시 사용 가능한 포인트를 조회
    @Transactional(readOnly = true)
    public UsablePointResponse getUsablePoint(Long memberId) {
        return pointCoreService.findMemberPoint(memberId)
            .map(UsablePointResponse::from)
            .orElseGet(() -> new UsablePointResponse(0));
    }

    // 회원 프로필 정보와 프로필 이미지 URL을 조회
    @Transactional(readOnly = true)
    public Optional<MemberProfileResponse> getMemberProfile(Long memberId) {
        return memberCoreService.findById(memberId)
            .map(member -> {
                String profileImageUrl = null;
                if (member.getProfileImageFileId() != null) {
                    profileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
                }

                return new MemberProfileResponse(
                    member.getId(), member.getNickname(), member.getMemberGrade(),
                    member.getStatusMessage(), profileImageUrl, member.getFullName(),
                    member.getPhoneNumber().getValue(), member.getUsername()
                );
            });
    }

    // 새 비밀번호 확인 일치 및 기존 비밀번호와의 상이 여부를 검증한 후 변경
    @Transactional
    public void updatePassword(Long memberId, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        Member member = memberCoreService.getById(memberId);

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        member.changePassword(passwordEncoder.encode(newPassword));
    }

    // 회원의 닉네임, 상태 메시지, 프로필 이미지를 수정
    @Transactional
    public void updateMemberProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        memberCoreService.getById(memberId).changeProfile(nickname, statusMessage, profileImageFileId);
    }

    // 회원의 이름, 휴대폰, 생년월일, 성별, 알림 수신 설정을 수정
    @Transactional
    public void updatePersonalInfo(Long memberId, String fullName, String phoneNumber, Integer birthDate,
                                   com.tastyhouse.core.entity.user.Gender gender,
                                   Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                                   Boolean eventInfoEnabled) {
        memberCoreService.getById(memberId).updatePersonalInfo(fullName, phoneNumber, birthDate, gender,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
    }

    // 내가 작성한 리뷰 목록을 페이지네이션하여 조회
    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, PageRequest pageRequest) {
        PageResult<MyReviewListItemDto> coreResult = reviewCoreService.findMyReviews(
            memberId, pageRequest.page(), pageRequest.size()
        );
        return coreResult.map(MyReviewListItemResponse::from);
    }

    // 내가 북마크한 장소 목록을 페이지네이션하여 조회
    @Transactional(readOnly = true)
    public PageResult<MyBookmarkedPlaceListItemResponse> getMyBookmarkedPlaces(Long memberId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<MyBookmarkedPlaceItemDto> page = placeCoreService.findMyBookmarkedPlaces(memberId, springPageRequest);

        List<MyBookmarkedPlaceListItemResponse> content = page.getContent().stream()
            .map(MyBookmarkedPlaceListItemResponse::from)
            .collect(Collectors.toList());

        return new PageResult<>(
            content,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    // 회원의 리뷰 수, 팔로잉 수, 팔로워 수를 조회
    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewCoreService.countByMemberIdAndIsHiddenFalse(memberId);
        long followingCount = followCoreService.countFollowing(memberId);
        long followerCount = followCoreService.countFollower(memberId);

        return new MemberStatsResponse(reviewCount, followingCount, followerCount);
    }

    // 다른 회원의 프로필과 현재 사용자의 팔로우 여부를 조회
    @Transactional(readOnly = true)
    public OtherMemberProfileResponse getOtherMemberProfile(Long targetMemberId, Long viewerMemberId) {
        Member member = memberCoreService.getById(targetMemberId);

        String profileImageUrl = null;
        if (member.getProfileImageFileId() != null) {
            profileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
        }

        boolean isFollowing = viewerMemberId != null
            && followCoreService.isFollowing(viewerMemberId, targetMemberId);

        return new OtherMemberProfileResponse(
            member.getId(), member.getNickname(), member.getMemberGrade(),
            member.getStatusMessage(), profileImageUrl, isFollowing
        );
    }
}

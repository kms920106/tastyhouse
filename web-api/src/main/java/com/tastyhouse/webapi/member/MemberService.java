package com.tastyhouse.webapi.member;

import com.tastyhouse.core.entity.place.dto.MyBookmarkedPlaceItemDto;
import com.tastyhouse.core.entity.review.dto.MyReviewListItemDto;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberWithdrawal;
import com.tastyhouse.core.entity.user.WithdrawalReason;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.repository.member.MemberJpaRepository;
import com.tastyhouse.core.repository.member.MemberWithdrawalJpaRepository;
import com.tastyhouse.core.repository.place.PlaceRepository;
import com.tastyhouse.core.repository.point.MemberPointHistoryJpaRepository;
import com.tastyhouse.core.repository.point.MemberPointJpaRepository;
import com.tastyhouse.core.repository.follow.FollowRepository;
import com.tastyhouse.core.repository.review.ReviewJpaRepository;
import com.tastyhouse.core.repository.review.ReviewRepository;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.config.jwt.TokenBlacklist;
import com.tastyhouse.webapi.coupon.CouponService;
import com.tastyhouse.webapi.coupon.response.MemberCouponListItemResponse;
import com.tastyhouse.webapi.exception.UnauthorizedException;
import com.tastyhouse.file.FileService;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    private final MemberJpaRepository memberJpaRepository;
    private final MemberWithdrawalJpaRepository memberWithdrawalJpaRepository;
    private final MemberPointJpaRepository memberPointJpaRepository;
    private final MemberPointHistoryJpaRepository memberPointHistoryJpaRepository;
    private final CouponService couponService;
    private final ReviewRepository reviewRepository;
    private final ReviewJpaRepository reviewJpaRepository;
    private final PlaceRepository placeRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void signUp(String username, String password, String passwordConfirm,
                       String nickname, String fullName,
                       com.tastyhouse.core.entity.user.Gender gender,
                       Integer birthDate, String phoneNumber,
                       Boolean marketingInfoEnabled, Boolean eventInfoEnabled,
                       String phoneVerifyToken, String emailVerifyToken) {

        if (!password.equals(passwordConfirm)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        if (memberJpaRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberJpaRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (phoneNumber != null) {
            if (!org.springframework.util.StringUtils.hasText(phoneVerifyToken)
                    || !jwtTokenProvider.validatePhoneVerifyToken(phoneVerifyToken)) {
                throw new BusinessException(ErrorCode.MEMBER_SIGNUP_PHONE_REQUIRED);
            }

            String verifiedPhone = jwtTokenProvider.getPhoneNumberFromPhoneVerifyToken(phoneVerifyToken);
            if (!verifiedPhone.equals(phoneNumber)) {
                throw new BusinessException(ErrorCode.MEMBER_PHONE_MISMATCH);
            }
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

        Member member = new Member(username, passwordEncoder.encode(password), nickname, fullName, gender,
                birthDate, phoneNumber, marketingInfoEnabled, eventInfoEnabled);

        memberJpaRepository.save(member);
    }

    public void verifyPersonalInfoToken(Long memberId, String verifyToken) {
        if (!jwtTokenProvider.validateVerifyToken(verifyToken)) {
            throw new BusinessException(ErrorCode.MEMBER_INFO_AUTH_EXPIRED);
        }

        Long verifiedMemberId = jwtTokenProvider.getMemberIdFromVerifyToken(verifyToken);
        if (!verifiedMemberId.equals(memberId)) {
            throw new UnauthorizedException("인증 정보가 일치하지 않습니다.");
        }
    }

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

    public void invalidateToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7).trim();
            if (jwtTokenProvider.validateToken(accessToken)) {
                long expirationMillis = jwtTokenProvider.getExpirationMillis(accessToken);
                tokenBlacklist.add(accessToken, expirationMillis);
            }
        }
    }

    @Transactional(readOnly = true)
    public NicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        boolean available = !memberJpaRepository.existsByNickname(nickname);
        return new NicknameAvailabilityResponse(available);
    }

    @Transactional(readOnly = true)
    public void verifyPassword(Long memberId, String rawPassword) {
        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_MISMATCH);
        }
    }

    @Transactional(readOnly = true)
    public PersonalInfoResponse getPersonalInfo(Long memberId) {
        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "존재하지 않는 회원입니다."));

        return PersonalInfoResponse.from(member);
    }

    @Transactional
    public void withdrawMember(Long memberId, WithdrawalReason reason, String reasonDetail) {
        memberJpaRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "존재하지 않는 회원입니다."))
            .deactivate();

        memberWithdrawalJpaRepository.save(
            MemberWithdrawal.builder()
                .memberId(memberId)
                .reason(reason)
                .reasonDetail(reasonDetail)
                .build()
        );
    }

    @Transactional(readOnly = true)
    public PointResponse getMemberPoint(Long memberId) {
        return memberPointJpaRepository.findByMemberId(memberId)
            .map(PointResponse::from)
            .orElseGet(() -> new PointResponse(0, 0));
    }

    @Transactional(readOnly = true)
    public PointHistoryResponse getPointHistory(Long memberId) {
        PointResponse pointResponse = getMemberPoint(memberId);

        List<PointHistoryItemResponse> histories = memberPointHistoryJpaRepository
            .findByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(PointHistoryItemResponse::from)
            .collect(Collectors.toList());

        return new PointHistoryResponse(pointResponse.availablePoints(), pointResponse.expiredThisMonth(), histories);
    }

    @Transactional(readOnly = true)
    public List<MemberCouponListItemResponse> getMemberCoupons(Long memberId) {
        return couponService.getMemberCoupons(memberId);
    }

    @Transactional(readOnly = true)
    public List<MemberCouponListItemResponse> getAvailableMemberCoupons(Long memberId) {
        return couponService.getAvailableMemberCoupons(memberId);
    }

    @Transactional(readOnly = true)
    public UsablePointResponse getUsablePoint(Long memberId) {
        return memberPointJpaRepository.findByMemberId(memberId)
            .map(UsablePointResponse::from)
            .orElseGet(() -> new UsablePointResponse(0));
    }

    @Transactional(readOnly = true)
    public Optional<MemberProfileResponse> getMemberProfile(Long memberId) {
        return memberJpaRepository.findById(memberId)
            .map(member -> {
                String profileImageUrl = null;
                if (member.getProfileImageFileId() != null) {
                    profileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
                }

                return new MemberProfileResponse(
                    member.getId(), member.getNickname(), member.getMemberGrade(),
                    member.getStatusMessage(), profileImageUrl, member.getFullName(),
                    member.getPhoneNumber(), member.getUsername()
                );
            });
    }

    @Transactional
    public void updatePassword(Long memberId, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        Member member = memberJpaRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "인증된 회원을 찾을 수 없습니다."));

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        member.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void updateMemberProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        Member member = memberJpaRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.changeProfile(nickname, statusMessage, profileImageFileId);
    }

    @Transactional
    public void updatePersonalInfo(Long memberId, String fullName, String phoneNumber, Integer birthDate,
                                   com.tastyhouse.core.entity.user.Gender gender,
                                   Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                                   Boolean eventInfoEnabled) {
        Member member = memberJpaRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.updatePersonalInfo(fullName, phoneNumber, birthDate, gender,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
    }

    @Transactional(readOnly = true)
    public PageResult<MyReviewListItemResponse> getMyReviews(Long memberId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<MyReviewListItemDto> page = reviewRepository.findMyReviews(memberId, springPageRequest);

        List<MyReviewListItemResponse> content = page.getContent().stream()
            .map(MyReviewListItemResponse::from)
            .collect(Collectors.toList());

        return new PageResult<>(
            content,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    @Transactional(readOnly = true)
    public PageResult<MyBookmarkedPlaceListItemResponse> getMyBookmarkedPlaces(Long memberId, PageRequest pageRequest) {
        org.springframework.data.domain.PageRequest springPageRequest =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        Page<MyBookmarkedPlaceItemDto> page = placeRepository.findMyBookmarkedPlaces(memberId, springPageRequest);

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

    @Transactional(readOnly = true)
    public MemberStatsResponse getMemberStats(Long memberId) {
        long reviewCount = reviewJpaRepository.countByMemberIdAndIsHiddenFalse(memberId);
        long followingCount = followRepository.countByFollowerId(memberId);
        long followerCount = followRepository.countByFollowingId(memberId);

        return new MemberStatsResponse(reviewCount, followingCount, followerCount);
    }

    @Transactional(readOnly = true)
    public OtherMemberProfileResponse getOtherMemberProfile(Long targetMemberId, Long viewerMemberId) {
        Member member = memberJpaRepository.findById(targetMemberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "회원을 찾을 수 없습니다."));

        String profileImageUrl = null;
        if (member.getProfileImageFileId() != null) {
            profileImageUrl = fileService.getFileUrl(member.getProfileImageFileId());
        }

        boolean isFollowing = viewerMemberId != null
            && followRepository.existsByFollowerIdAndFollowingId(viewerMemberId, targetMemberId);

        return new OtherMemberProfileResponse(
            member.getId(), member.getNickname(), member.getMemberGrade(),
            member.getStatusMessage(), profileImageUrl, isFollowing
        );
    }
}

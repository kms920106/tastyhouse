package com.tastyhouse.core.domain.member.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.vo.PhoneNumber;

/**
 * 회원 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MemberJpaEntity} + {@code MemberMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code MemberRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class Member {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final String username;
    private String password;
    private String nickname;
    private String fullName;
    private Integer birthDate;
    private MemberGender gender;
    private PhoneNumber phoneNumber;
    private final MemberGrade memberGrade;
    private Long profileImageFileId;
    private String statusMessage;
    private boolean pushNotificationEnabled;
    private boolean marketingInfoEnabled;
    private boolean eventInfoEnabled;
    private MemberStatus memberStatus;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Member(
        Long id,
        String username,
        String password,
        String nickname,
        String fullName,
        Integer birthDate,
        MemberGender gender,
        PhoneNumber phoneNumber,
        MemberGrade memberGrade,
        Long profileImageFileId,
        String statusMessage,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        MemberStatus memberStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.memberGrade = memberGrade;
        this.profileImageFileId = profileImageFileId;
        this.statusMessage = statusMessage;
        this.pushNotificationEnabled = pushNotificationEnabled;
        this.marketingInfoEnabled = marketingInfoEnabled;
        this.eventInfoEnabled = eventInfoEnabled;
        this.memberStatus = memberStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 일반(아이디/비밀번호) 신규 회원을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Member of(
        String username,
        String password,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled
    ) {
        return new Member(
            null, username, password, nickname, fullName, birthDate, gender,
            phoneNumber != null ? new PhoneNumber(phoneNumber) : null,
            MemberGrade.NEWCOMER, null, null,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            MemberStatus.ACTIVE, null, null
        );
    }

    /**
     * 소셜 로그인 신규 회원을 생성한다(비밀번호 없음). 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Member ofSocial(
        String username,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled
    ) {
        return new Member(
            null, username, null, nickname, fullName, birthDate, gender,
            phoneNumber != null ? new PhoneNumber(phoneNumber) : null,
            MemberGrade.NEWCOMER, null, null,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            MemberStatus.ACTIVE, null, null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Member reconstitute(
        Long id,
        String username,
        String password,
        String nickname,
        String fullName,
        Integer birthDate,
        MemberGender gender,
        PhoneNumber phoneNumber,
        MemberGrade memberGrade,
        Long profileImageFileId,
        String statusMessage,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        MemberStatus memberStatus,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Member(
            id, username, password, nickname, fullName, birthDate, gender, phoneNumber,
            memberGrade, profileImageFileId, statusMessage,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled,
            memberStatus, createdAt, updatedAt
        );
    }

    public MemberId getMemberId() {
        return MemberId.of(this.id);
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String nickname, String statusMessage, Long profileImageFileId) {
        if (nickname != null) this.nickname = nickname;
        if (statusMessage != null) this.statusMessage = statusMessage;
        if (profileImageFileId != null) this.profileImageFileId = profileImageFileId;
    }

    public void updatePersonalInfo(
        String fullName,
        String phoneNumber,
        Integer birthDate,
        MemberGender gender,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled
    ) {
        if (fullName != null) this.fullName = fullName;
        if (phoneNumber != null) this.phoneNumber = new PhoneNumber(phoneNumber);
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        this.pushNotificationEnabled = pushNotificationEnabled;
        this.marketingInfoEnabled = marketingInfoEnabled;
        this.eventInfoEnabled = eventInfoEnabled;
    }

    public void withdraw() {
        if (this.memberStatus == MemberStatus.DELETED) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }
        this.memberStatus = MemberStatus.DELETED;
    }

    public void suspend() {
        if (this.memberStatus == MemberStatus.DELETED) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_CHANGE_NOT_ALLOWED);
        }
        if (this.memberStatus == MemberStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_SUSPENDED);
        }
        this.memberStatus = MemberStatus.SUSPENDED;
    }

    public void activate() {
        if (this.memberStatus == MemberStatus.DELETED) {
            throw new BusinessException(ErrorCode.MEMBER_STATUS_CHANGE_NOT_ALLOWED);
        }
        if (this.memberStatus == MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MEMBER_ALREADY_ACTIVE);
        }
        this.memberStatus = MemberStatus.ACTIVE;
    }
}

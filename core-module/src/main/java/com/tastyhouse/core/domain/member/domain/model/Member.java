package com.tastyhouse.core.domain.member.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.entity.BaseEntity;
import com.tastyhouse.core.shared.vo.PhoneNumber;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "MEMBER")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "birth_date", nullable = false)
    private Integer birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10, columnDefinition = "VARCHAR(10)")
    private MemberGender gender;

    @Embedded
    private PhoneNumber phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_grade", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberGrade memberGrade = MemberGrade.NEWCOMER;

    @Column(name = "profile_image_file_id")
    private Long profileImageFileId;

    @Column(name = "status_message", length = 200)
    private String statusMessage;

    @Column(name = "push_notification_enabled", nullable = false)
    private boolean pushNotificationEnabled = true;

    @Column(name = "marketing_info_enabled", nullable = false)
    private boolean marketingInfoEnabled = false;

    @Column(name = "event_info_enabled", nullable = false)
    private boolean eventInfoEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    private Member(
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
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.fullName = fullName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber != null ? new PhoneNumber(phoneNumber) : null;
        this.pushNotificationEnabled = pushNotificationEnabled;
        this.marketingInfoEnabled = marketingInfoEnabled;
        this.eventInfoEnabled = eventInfoEnabled;
    }

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
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        );
    }

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
            username, null, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
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

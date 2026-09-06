package com.tastyhouse.domain.member.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

public class Member {
    private final Long id;
    private final String username;
    private String password;
    private String nickname;
    private String fullName;
    private Integer birthDate;
    private MemberGender gender;
    private PhoneNumber phoneNumber;
    private final MemberGrade memberGrade;
    private UploadedFileId profileImageFileId;
    private String statusMessage;
    private boolean pushNotificationEnabled;
    private boolean marketingInfoEnabled;
    private boolean eventInfoEnabled;
    private MemberStatus memberStatus;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
        UploadedFileId profileImageFileId,
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
        UploadedFileId profileImageFileId,
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

    public void updateProfile(String nickname, String statusMessage, UploadedFileId profileImageFileId) {
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

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getNickname() {
        return this.nickname;
    }

    public String getFullName() {
        return this.fullName;
    }

    public Integer getBirthDate() {
        return this.birthDate;
    }

    public MemberGender getGender() {
        return this.gender;
    }

    public PhoneNumber getPhoneNumber() {
        return this.phoneNumber;
    }

    public MemberGrade getMemberGrade() {
        return this.memberGrade;
    }

    public UploadedFileId getProfileImageFileId() {
        return this.profileImageFileId;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public boolean isPushNotificationEnabled() {
        return this.pushNotificationEnabled;
    }

    public boolean isMarketingInfoEnabled() {
        return this.marketingInfoEnabled;
    }

    public boolean isEventInfoEnabled() {
        return this.eventInfoEnabled;
    }

    public MemberStatus getMemberStatus() {
        return this.memberStatus;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}

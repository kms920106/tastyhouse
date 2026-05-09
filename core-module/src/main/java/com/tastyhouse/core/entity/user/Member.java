package com.tastyhouse.core.entity.user;

import com.tastyhouse.core.entity.BaseEntity;
import com.tastyhouse.core.entity.common.vo.PhoneNumber;
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
    private Gender gender;

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
    private Boolean pushNotificationEnabled = true;

    @Column(name = "marketing_info_enabled", nullable = false)
    private Boolean marketingInfoEnabled = false;

    @Column(name = "event_info_enabled", nullable = false)
    private Boolean eventInfoEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberStatus memberStatus = MemberStatus.ACTIVE;

    private Member(
        String username,
        String password,
        String nickname,
        String fullName,
        Gender gender,
        Integer birthDate,
        String phoneNumber,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled
    ) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.fullName = fullName;
        this.gender = gender;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber != null ? new PhoneNumber(phoneNumber) : null;
        this.memberGrade = MemberGrade.NEWCOMER;
        this.memberStatus = MemberStatus.ACTIVE;
        this.pushNotificationEnabled = pushNotificationEnabled != null ? pushNotificationEnabled : true;
        this.marketingInfoEnabled = marketingInfoEnabled != null ? marketingInfoEnabled : false;
        this.eventInfoEnabled = eventInfoEnabled != null ? eventInfoEnabled : false;
    }

    public static Member of(
        String username,
        String password,
        String nickname,
        String fullName,
        Gender gender,
        Integer birthDate,
        String phoneNumber,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled
    ) {
        return new Member(
            username,
            password,
            nickname,
            fullName,
            gender,
            birthDate,
            phoneNumber,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled
        );
    }

    public static Member ofSocial(
        String username,
        String nickname,
        String fullName,
        Gender gender,
        Integer birthDate,
        String phoneNumber,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled
    ) {
        return new Member(
            username,
            null,
            nickname,
            fullName,
            gender,
            birthDate,
            phoneNumber,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled
        );
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(
        String nickname,
        String statusMessage,
        Long profileImageFileId
    ) {
        if (nickname != null) this.nickname = nickname;
        if (statusMessage != null) this.statusMessage = statusMessage;
        if (profileImageFileId != null) this.profileImageFileId = profileImageFileId;
    }

    public void updatePersonalInfo(
        String fullName,
        String phoneNumber,
        Integer birthDate,
        Gender gender,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled
    ) {
        if (fullName != null) this.fullName = fullName;
        if (phoneNumber != null) this.phoneNumber = new PhoneNumber(phoneNumber);
        if (birthDate != null) this.birthDate = birthDate;
        if (gender != null) this.gender = gender;
        if (pushNotificationEnabled != null) this.pushNotificationEnabled = pushNotificationEnabled;
        if (marketingInfoEnabled != null) this.marketingInfoEnabled = marketingInfoEnabled;
        if (eventInfoEnabled != null) this.eventInfoEnabled = eventInfoEnabled;
    }

    public void deactivate() {
        this.memberStatus = MemberStatus.DELETED;
    }
}


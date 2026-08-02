package com.tastyhouse.infrastructure.member.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.member.domain.model.MemberGender;
import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 회원 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Member}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MemberMapper}가 수행한다.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "MEMBER")
public class MemberJpaEntity extends BaseEntity {

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
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", nullable = false, length = 11))
    private PhoneNumber phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_grade", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberGrade memberGrade;

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "profile_image_file_id")
    private UploadedFileId profileImageFileId;

    @Column(name = "status_message", length = 200)
    private String statusMessage;

    @Column(name = "push_notification_enabled", nullable = false)
    private boolean pushNotificationEnabled;

    @Column(name = "marketing_info_enabled", nullable = false)
    private boolean marketingInfoEnabled;

    @Column(name = "event_info_enabled", nullable = false)
    private boolean eventInfoEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MemberStatus memberStatus;

    private MemberJpaEntity(
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
        MemberStatus memberStatus
    ) {
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
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MemberMapper#toEntity}에서만 호출한다.
     */
    static MemberJpaEntity create(
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
        MemberStatus memberStatus
    ) {
        return new MemberJpaEntity(
            username, password, nickname, fullName, birthDate, gender, phoneNumber,
            memberGrade, profileImageFileId, statusMessage,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled, memberStatus
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        String password,
        String nickname,
        String fullName,
        Integer birthDate,
        MemberGender gender,
        PhoneNumber phoneNumber,
        UploadedFileId profileImageFileId,
        String statusMessage,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        MemberStatus memberStatus
    ) {
        this.password = password;
        this.nickname = nickname;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.profileImageFileId = profileImageFileId;
        this.statusMessage = statusMessage;
        this.pushNotificationEnabled = pushNotificationEnabled;
        this.marketingInfoEnabled = marketingInfoEnabled;
        this.eventInfoEnabled = eventInfoEnabled;
        this.memberStatus = memberStatus;
    }
}

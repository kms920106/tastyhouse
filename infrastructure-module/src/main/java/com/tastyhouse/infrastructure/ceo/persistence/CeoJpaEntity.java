package com.tastyhouse.infrastructure.ceo.persistence;

import jakarta.persistence.AttributeOverride;
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

import com.tastyhouse.core.domain.ceo.domain.model.CeoStatus;
import com.tastyhouse.core.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 점주 계정 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Ceo}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code CeoMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "CEO")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CeoJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "business_registration_number", length = 20)
    private String businessRegistrationNumber;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", length = 11))
    private PhoneNumber phoneNumber;

    @Column(name = "email", length = 200)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private CeoStatus status;

    private CeoJpaEntity(
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumber phoneNumber,
        String email,
        CeoStatus status
    ) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code CeoMapper#toEntity}에서만 호출한다.
     */
    static CeoJpaEntity create(
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumber phoneNumber,
        String email,
        CeoStatus status
    ) {
        return new CeoJpaEntity(username, password, name, businessRegistrationNumber, phoneNumber, email, status);
    }
}

package com.tastyhouse.infrastructure.region.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * 행정동 마스터 JPA 영속 모델. 순수 도메인 모델 {@code AdminDong}과 분리된 영속 전용 엔티티다.
 *
 * <p>행정표준코드 시드 SQL로만 관리하는 read-only 마스터라 생성·변경 팩토리를 두지 않는다. 감사 컬럼이
 * 없어 {@code BaseEntity}를 상속하지 않는다({@code PublicHolidayJpaEntity} 선례).
 */
@Entity
@Table(
    name = "ADMIN_DONG",
    uniqueConstraints = @UniqueConstraint(name = "uk_admin_dong_code", columnNames = "code"),
    indexes = @Index(name = "idx_admin_dong_name", columnList = "sido_name, sigungu_name, dong_name")
)
public class AdminDongJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "code", nullable = false, length = 10)
    private String code; // 행정동 코드(10자리)

    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName; // 시/도

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName; // 시/군/구

    @Column(name = "dong_name", nullable = false, length = 50)
    private String dongName; // 행정동

    @Column(name = "is_active", nullable = false)
    private boolean active; // 사용 여부

    protected AdminDongJpaEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public String getSidoName() {
        return this.sidoName;
    }

    public String getSigunguName() {
        return this.sigunguName;
    }

    public String getDongName() {
        return this.dongName;
    }

    public boolean isActive() {
        return this.active;
    }
}

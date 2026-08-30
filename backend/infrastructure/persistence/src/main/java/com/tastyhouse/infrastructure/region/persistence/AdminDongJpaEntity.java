package com.tastyhouse.infrastructure.region.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.domain.shared.geo.GeoBoundingBox;

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
    indexes = {
        @Index(name = "idx_admin_dong_name", columnList = "sido_name, sigungu_name, dong_name"),
        @Index(name = "idx_admin_dong_center", columnList = "center_latitude, center_longitude")
    }
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

    // 아래 좌표·경계 컬럼은 전부 nullable이다 — 시드가 단계적으로 투입되므로(코드·좌표 먼저, 경계는 나중)
    // 미보유 행이 정상 상태이며, 기존 행에 무해하게 추가하기 위한 조건이기도 하다.

    @Column(name = "center_latitude", precision = 9, scale = 6)
    private BigDecimal centerLatitude; // 행정동 대표점 위도(경계 내부 보장점, centroid 아님)

    @Column(name = "center_longitude", precision = 9, scale = 6)
    private BigDecimal centerLongitude; // 행정동 대표점 경도

    // 바운딩박스 네 컬럼은 현재 쓰기 전용이다 — Hibernate가 INSERT/UPDATE 시 리플렉션으로 읽어
    // 가지만, 되읽는 자바 경로는 없다. 조회 측 프리필터(AdminDongQueryDao)는 이 박스가 아니라
    // 대표점(center_latitude·center_longitude, idx_admin_dong_center)으로 후보를 좁히고, 경계
    // 정밀 판정은 boundary 문자열을 디코딩해 수행한다. 박스는 경계에서 파생되는 값이라
    // getter를 두면 호출자 없는 죽은 코드가 되므로, 필요해지는 시점에 질의와 함께 추가한다.
    @SuppressWarnings("unused")
    @Column(name = "boundary_min_latitude", precision = 9, scale = 6)
    private BigDecimal boundaryMinLatitude; // 경계 바운딩박스 최소 위도

    @SuppressWarnings("unused")
    @Column(name = "boundary_max_latitude", precision = 9, scale = 6)
    private BigDecimal boundaryMaxLatitude; // 경계 바운딩박스 최대 위도

    @SuppressWarnings("unused")
    @Column(name = "boundary_min_longitude", precision = 9, scale = 6)
    private BigDecimal boundaryMinLongitude; // 경계 바운딩박스 최소 경도

    @SuppressWarnings("unused")
    @Column(name = "boundary_max_longitude", precision = 9, scale = 6)
    private BigDecimal boundaryMaxLongitude; // 경계 바운딩박스 최대 경도

    @Column(name = "boundary", columnDefinition = "LONGTEXT")
    private String boundary; // 경계 폴리곤(링 ";" 구분, 점 "," 구분, "경도 위도")

    protected AdminDongJpaEntity() {
    }

    /**
     * 동기화 배치가 원천에서 읽은 행정동 하나를 영속 엔티티로 만든다.
     *
     * <p>바운딩박스는 경계에서 파생되는 값이라 호출자가 따로 넘기지 않고 여기서 계산한다 — 경계와
     * 박스가 어긋나면 프리필터가 실제 경계와 다른 후보를 내놓는데, 두 값을 각각 받으면 그 어긋남이
     * 조용히 저장될 수 있다.
     */
    static AdminDongJpaEntity create(
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        GeoBoundingBox boundingBox,
        String boundary
    ) {
        AdminDongJpaEntity entity = new AdminDongJpaEntity();
        entity.code = code;
        entity.sidoName = sidoName;
        entity.sigunguName = sigunguName;
        entity.dongName = dongName;
        entity.active = active;
        entity.centerLatitude = centerLatitude;
        entity.centerLongitude = centerLongitude;
        entity.applyBoundary(boundingBox, boundary);
        return entity;
    }

    /**
     * managed 엔티티에 원천 값을 복사한다(load-copy-save). {@code id}·{@code code}는 동일성의 기준이라
     * 건드리지 않는다.
     *
     * <p>동기화가 전량 삭제·재삽입이 아니라 제자리 갱신인 이유는 다른 테이블이 {@code id}를 참조하기
     * 때문이다 — 자세한 배경은 {@code AdminDongRepository#synchronize} Javadoc 참고.
     */
    void applyChanges(
        String sidoName,
        String sigunguName,
        String dongName,
        boolean active,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        GeoBoundingBox boundingBox,
        String boundary
    ) {
        this.sidoName = sidoName;
        this.sigunguName = sigunguName;
        this.dongName = dongName;
        this.active = active;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        applyBoundary(boundingBox, boundary);
    }

    /** 원천에서 사라진 행정동을 폐지 처리한다. 참조 무결성 때문에 삭제하지 않는다. */
    void deactivate() {
        this.active = false;
    }

    private void applyBoundary(GeoBoundingBox boundingBox, String boundary) {
        this.boundary = boundary;
        // 경계가 사라진 경우 낡은 박스가 남지 않도록 null까지 그대로 반영한다.
        this.boundaryMinLatitude = boundingBox == null ? null : boundingBox.minLatitude();
        this.boundaryMaxLatitude = boundingBox == null ? null : boundingBox.maxLatitude();
        this.boundaryMinLongitude = boundingBox == null ? null : boundingBox.minLongitude();
        this.boundaryMaxLongitude = boundingBox == null ? null : boundingBox.maxLongitude();
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

    /** 대표점 위도. 좌표 미보유 시 {@code null}. */
    public BigDecimal getCenterLatitude() {
        return this.centerLatitude;
    }

    /** 대표점 경도. 좌표 미보유 시 {@code null}. */
    public BigDecimal getCenterLongitude() {
        return this.centerLongitude;
    }

    /** 경계 폴리곤 인코딩 문자열. 경계 미보유 시 {@code null}. */
    public String getBoundary() {
        return this.boundary;
    }
}

package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 가게 배달지역 도형 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopDeliveryAreaPolygon}과 분리된 영속 전용 엔티티다. 도형 좌표는 MySQL
 * {@code GEOMETRY}가 아니라 {@code LONGTEXT} 인코딩으로 담는다 — 근거는
 * {@code GeoPolygonTextCodec} Javadoc 참고.
 *
 * <p>{@code center_*}는 저장 시점 가게 좌표의 <b>스냅샷</b>이며 현재 가게 좌표와 다를 수 있다. 가게가
 * 이전하면 이 값과 현재 좌표의 차이로 "배달지역 재설정 필요"를 감지한다.
 */
@Entity
@Table(
    name = "SHOP_DELIVERY_AREA_POLYGON",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_shop_delivery_area_polygon_shop_id",
        columnNames = "shop_id"
    )
)
public class ShopDeliveryAreaPolygonJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "rings", nullable = false, columnDefinition = "LONGTEXT")
    private String rings; // 도형(링 ";" 구분, 점 "," 구분, "경도 위도")

    @Column(name = "center_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centerLatitude; // 저장 시점 가게 위도 스냅샷(7km 상한 기준점)

    @Column(name = "center_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centerLongitude;

    @Column(name = "max_radius_meters", nullable = false)
    private Integer maxRadiusMeters; // 기준점~최원거리 정점 거리(m)

    // 아래 두 컬럼은 쓰기 전용이다 — Hibernate가 INSERT/UPDATE 시 리플렉션으로 읽어 가고, 자바
    // 코드가 되읽는 경로는 없다(도메인 모델 ShopDeliveryAreaPolygon이 rings에서 직접 세므로
    // getter를 두면 호출자 없는 죽은 코드가 된다). 값은 SQL 집계·점검 질의가 소비한다.
    @SuppressWarnings("unused")
    @Column(name = "ring_count", nullable = false)
    private Integer ringCount; // 링 개수(표시·검증용 비정규화)

    @SuppressWarnings("unused")
    @Column(name = "vertex_count", nullable = false)
    private Integer vertexCount; // 총 정점 수(표시·검증용 비정규화)

    protected ShopDeliveryAreaPolygonJpaEntity() {
    }

    private ShopDeliveryAreaPolygonJpaEntity(
        Long shopId,
        String rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Integer maxRadiusMeters,
        Integer ringCount,
        Integer vertexCount
    ) {
        this.shopId = shopId;
        this.rings = rings;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.maxRadiusMeters = maxRadiusMeters;
        this.ringCount = ringCount;
        this.vertexCount = vertexCount;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopDeliveryAreaPolygonMapper#toEntity}에서만 호출한다.
     */
    static ShopDeliveryAreaPolygonJpaEntity create(
        Long shopId,
        String rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Integer maxRadiusMeters,
        Integer ringCount,
        Integer vertexCount
    ) {
        return new ShopDeliveryAreaPolygonJpaEntity(
            shopId,
            rings,
            centerLatitude,
            centerLongitude,
            maxRadiusMeters,
            ringCount,
            vertexCount
        );
    }

    /**
     * managed 엔티티에 변경분을 복사한다(load-copy-save). {@code shopId}는 교체 대상이 아니므로 건드리지 않는다.
     */
    void applyChanges(
        String rings,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        Integer maxRadiusMeters,
        Integer ringCount,
        Integer vertexCount
    ) {
        this.rings = rings;
        this.centerLatitude = centerLatitude;
        this.centerLongitude = centerLongitude;
        this.maxRadiusMeters = maxRadiusMeters;
        this.ringCount = ringCount;
        this.vertexCount = vertexCount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getRings() {
        return this.rings;
    }

    public BigDecimal getCenterLatitude() {
        return this.centerLatitude;
    }

    public BigDecimal getCenterLongitude() {
        return this.centerLongitude;
    }

    public Integer getMaxRadiusMeters() {
        return this.maxRadiusMeters;
    }
}

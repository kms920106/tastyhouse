package com.tastyhouse.domain.shop.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 가게 편의정보(주차/발렛/찾아오는길/노출위치) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopConvenienceInfoJpaEntity} + {@code ShopConvenienceInfoMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopConvenienceInfoRepository#save}를 호출해야 한다. shopId당 1개만 존재한다(upsert).
 */
public class ShopConvenienceInfo {

    private static final int DIRECTIONS_GUIDE_MAX_LENGTH = 200;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private boolean parkingAvailable; // 주차 가능 여부 (상태전이로 재대입됨)
    private boolean parkingPaid; // 주차 유료 여부 (상태전이로 재대입됨)
    private boolean valetAvailable; // 발렛 가능 여부 (상태전이로 재대입됨)
    private boolean valetPaid; // 발렛 유료 여부 (상태전이로 재대입됨)
    private String directionsGuide; // 찾아오는 길 안내 (상태전이로 재대입됨, nullable, 최대 200자)
    private BigDecimal displayLatitude; // 노출 위치 위도 (상태전이로 재대입됨, nullable)
    private BigDecimal displayLongitude; // 노출 위치 경도 (상태전이로 재대입됨, nullable)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopConvenienceInfo(
        Long id,
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 편의정보를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopConvenienceInfo of(
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        validateDirectionsGuide(directionsGuide);

        return new ShopConvenienceInfo(null, shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopConvenienceInfo reconstitute(
        Long id,
        ShopId shopId,
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopConvenienceInfo(id, shopId, parkingAvailable, parkingPaid, valetAvailable, valetPaid,
            directionsGuide, displayLatitude, displayLongitude, createdAt, updatedAt);
    }

    /**
     * 편의정보를 수정한다.
     */
    public void update(
        boolean parkingAvailable,
        boolean parkingPaid,
        boolean valetAvailable,
        boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude
    ) {
        validateDirectionsGuide(directionsGuide);

        this.parkingAvailable = parkingAvailable;
        this.parkingPaid = parkingPaid;
        this.valetAvailable = valetAvailable;
        this.valetPaid = valetPaid;
        this.directionsGuide = directionsGuide;
        this.displayLatitude = displayLatitude;
        this.displayLongitude = displayLongitude;
    }

    private static void validateDirectionsGuide(String directionsGuide) {
        if (directionsGuide != null && directionsGuide.length() > DIRECTIONS_GUIDE_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_DIRECTIONS_GUIDE_TOO_LONG);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public boolean isParkingAvailable() {
        return this.parkingAvailable;
    }

    public boolean isParkingPaid() {
        return this.parkingPaid;
    }

    public boolean isValetAvailable() {
        return this.valetAvailable;
    }

    public boolean isValetPaid() {
        return this.valetPaid;
    }

    public String getDirectionsGuide() {
        return this.directionsGuide;
    }

    public BigDecimal getDisplayLatitude() {
        return this.displayLatitude;
    }

    public BigDecimal getDisplayLongitude() {
        return this.displayLongitude;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}

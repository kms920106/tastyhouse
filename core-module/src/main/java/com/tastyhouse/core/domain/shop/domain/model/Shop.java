package com.tastyhouse.core.domain.shop.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.domain.shop.domain.vo.ShopId;

/**
 * 상점 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopJpaEntity} + {@code ShopMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ShopRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class Shop {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private Long ceoId; // 소유 점주 ID (CEO.id 참조, null이면 점주 미배정)
    private Long stationId; // 지하철역 ID (STATION.id 참조)
    private String name; // 상호명
    private BigDecimal latitude; // 위도
    private BigDecimal longitude; // 경도
    private final Double rating; // 평균 평점
    private String roadAddress; // 도로명 주소
    private String lotAddress; // 지번 주소
    private String phoneNumber; // 대표 전화번호
    private Long thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)
    private Long trademarkImageFileId; // 상표 이미지 파일 ID (승인 완료 시 반영, FILE.id 참조)
    private boolean permanentlyClosed; // 폐업 여부 (true: 폐업)
    private boolean hidden; // 노출정지 여부 (true: 배민앱 완전 비노출, 폐업과 별개)
    private boolean closedOnPublicHolidays; // 공휴일 휴무 여부 (true: 공휴일 휴무)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Shop(
        Long id,
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        Long trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.ceoId = ceoId;
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.trademarkImageFileId = trademarkImageFileId;
        this.permanentlyClosed = permanentlyClosed;
        this.hidden = hidden;
        this.closedOnPublicHolidays = closedOnPublicHolidays;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 상점을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Shop of(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        return new Shop(
            null,
            null,
            stationId,
            name,
            latitude,
            longitude,
            null,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            null,
            false,
            false,
            false,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Shop reconstitute(
        Long id,
        Long ceoId,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        Long trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Shop(
            id,
            ceoId,
            stationId,
            name,
            latitude,
            longitude,
            rating,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            trademarkImageFileId,
            permanentlyClosed,
            hidden,
            closedOnPublicHolidays,
            createdAt,
            updatedAt
        );
    }

    public ShopId getShopId() {
        return ShopId.of(this.id);
    }

    public void update(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId
    ) {
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
    }

    /**
     * 소유 점주를 배정한다(관리자가 가게-점주 연결 시 사용). null이면 점주 미배정 상태로 되돌린다.
     */
    public void assignCeo(Long ceoId) {
        this.ceoId = ceoId;
    }

    /**
     * 대표 전화번호를 갱신한다. 전화번호 다건 관리에서 대표번호 변경 시 {@code Shop.phoneNumber}를 동기화한다.
     */
    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * 승인 완료된 상표 이미지를 반영한다.
     */
    public void changeTrademarkImage(Long trademarkImageFileId) {
        this.trademarkImageFileId = trademarkImageFileId;
    }

    /**
     * 승인 완료된 대표(썸네일) 이미지를 반영한다.
     */
    public void changeThumbnailImage(Long thumbnailImageFileId) {
        this.thumbnailImageFileId = thumbnailImageFileId;
    }

    /**
     * 공휴일 휴무 여부를 설정한다.
     */
    public void updateHolidayClosure(boolean closedOnPublicHolidays) {
        this.closedOnPublicHolidays = closedOnPublicHolidays;
    }

    /**
     * 배민앱에서 가게를 완전히 숨긴다(노출정지).
     */
    public void hide() {
        this.hidden = true;
    }

    /**
     * 노출정지를 해제해 다시 노출한다.
     */
    public void show() {
        this.hidden = false;
    }

    public void close() {
        this.permanentlyClosed = true;
    }
}

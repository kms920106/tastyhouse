package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.vo.StationId;
import com.tastyhouse.infrastructure.ceo.persistence.CeoIdConverter;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Shop}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopMapper}가 수행한다.
 */
@Entity
@Table(name = "SHOP")
public class ShopJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = CeoIdConverter.class)
    @Column(name = "ceo_id")
    private CeoId ceoId; // 소유 점주 ID (CEO.id 참조, null이면 점주 미배정)

    @Convert(converter = StationIdConverter.class)
    @Column(name = "station_id", nullable = false)
    private StationId stationId; // 지하철역 ID (STATION.id 참조)

    @Column(name = "name", nullable = false, unique = true)
    private String name; // 상호명

    @Column(name = "latitude", nullable = false)
    private BigDecimal latitude; // 위도

    @Column(name = "longitude", nullable = false)
    private BigDecimal longitude; // 경도

    @Column(name = "rating")
    private Double rating; // 평균 평점

    @Column(name = "road_address")
    private String roadAddress; // 도로명 주소

    @Column(name = "lot_address")
    private String lotAddress; // 지번 주소

    @Column(name = "phone_number")
    private String phoneNumber; // 전화번호

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "thumbnail_image_file_id")
    private UploadedFileId thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "trademark_image_file_id")
    private UploadedFileId trademarkImageFileId; // 상표 이미지 파일 ID (FILE.id 참조)

    @Column(name = "is_permanently_closed", nullable = false)
    private boolean permanentlyClosed; // 폐업 여부 (true: 폐업)

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden; // 노출정지 여부 (true: 배민앱 완전 비노출)

    @Column(name = "is_closed_on_public_holidays", nullable = false)
    private boolean closedOnPublicHolidays; // 공휴일 휴무 여부

    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount; // 최소주문금액 (0: 미설정, 설정 시 5000~30000, 배달 주문에만 적용)

    protected ShopJpaEntity() {
    }

    private ShopJpaEntity(
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount
    ) {
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
        this.minOrderAmount = minOrderAmount;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopMapper#toEntity}에서만 호출한다.
     */
    static ShopJpaEntity create(
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount
    ) {
        return new ShopJpaEntity(
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
            minOrderAmount
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        CeoId ceoId,
        StationId stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays,
        int minOrderAmount
    ) {
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
        this.minOrderAmount = minOrderAmount;
    }

    public Long getId() {
        return this.id;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public StationId getStationId() {
        return this.stationId;
    }

    public String getName() {
        return this.name;
    }

    public BigDecimal getLatitude() {
        return this.latitude;
    }

    public BigDecimal getLongitude() {
        return this.longitude;
    }

    public Double getRating() {
        return this.rating;
    }

    public String getRoadAddress() {
        return this.roadAddress;
    }

    public String getLotAddress() {
        return this.lotAddress;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public UploadedFileId getThumbnailImageFileId() {
        return this.thumbnailImageFileId;
    }

    public UploadedFileId getTrademarkImageFileId() {
        return this.trademarkImageFileId;
    }

    public boolean isPermanentlyClosed() {
        return this.permanentlyClosed;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isClosedOnPublicHolidays() {
        return this.closedOnPublicHolidays;
    }

    public int getMinOrderAmount() {
        return this.minOrderAmount;
    }
}

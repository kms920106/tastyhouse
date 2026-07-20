package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Shop}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "SHOP")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "station_id", nullable = false)
    private Long stationId; // 지하철역 ID (STATION.id 참조)

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

    @Column(name = "thumbnail_image_file_id")
    private Long thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)

    @Column(name = "is_permanently_closed", nullable = false)
    private boolean permanentlyClosed; // 폐업 여부 (true: 폐업)

    private ShopJpaEntity(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        boolean permanentlyClosed
    ) {
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.permanentlyClosed = permanentlyClosed;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopMapper#toEntity}에서만 호출한다.
     */
    static ShopJpaEntity create(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        boolean permanentlyClosed
    ) {
        return new ShopJpaEntity(
            stationId,
            name,
            latitude,
            longitude,
            rating,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            permanentlyClosed
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        boolean permanentlyClosed
    ) {
        this.stationId = stationId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.roadAddress = roadAddress;
        this.lotAddress = lotAddress;
        this.phoneNumber = phoneNumber;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.permanentlyClosed = permanentlyClosed;
    }
}

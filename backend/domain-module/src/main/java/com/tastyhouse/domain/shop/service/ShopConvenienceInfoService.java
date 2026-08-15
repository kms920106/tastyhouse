package com.tastyhouse.domain.shop.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopConvenienceInfo;
import com.tastyhouse.domain.shop.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 편의정보(주차·발렛·찾아오는길·표시위치) 불변식(도메인 서비스).
 *
 * <p>찾아오는길 텍스트는 금칙어 검수를 통과해야 하고, 지도 표시 위치는 <b>가게 실제 좌표에서
 * {@value #MAX_DISPLAY_LOCATION_DISTANCE_METERS}m 이내</b>여야 한다(가게 애그리거트의 좌표를 함께 읽어
 * 판정하는 크로스 애그리거트 규칙 — 분류 C). 편의정보는 가게당 1건으로 없으면 생성, 있으면 갱신하는
 * upsert 시맨틱을 가진다.
 *
 * <p><b>변경이력 기록도 이 서비스가 소유한다</b>(가게정보 분류 {@code CONVENIENCE_INFO}·{@code AMENITY}).
 * 편의정보는 upsert라 변경 전 값을 보려면 저장 전에 기존 행을 읽어야 하는데, 이 서비스는 upsert를
 * 수행하려고 이미 그 행을 읽는 유일한 지점이다. ceo-api의 {@code CommandService}는 CQRS 교차 주입
 * 금지로 QueryDao를 주입할 수 없어 변경 전 값을 구조적으로 볼 수 없다.
 *
 * <p><b>편의시설 배정·해제도 여기로 내렸다.</b> 이력에 담을 요약이 카테고리 ID가 아니라 <b>편의시설
 * 이름</b>이어야 하고(ID만 남으면 사람이 읽을 수 없다), 그 이름은 카테고리 존재 검증으로 이미 로드하는
 * {@link ShopAmenityCategory}에서 나온다. 배정은 <b>행 단위 CREATE</b>, 해제는 <b>행 단위 DELETE</b>로
 * 한 행씩 남긴다 — 편의정보 upsert와 달리 화면에서 한 번에 하나씩 켜고 끄는 조작이다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다. 변경 주체({@link ShopChangeActor})는 도메인이 인증을 모르므로
 * 마지막 파라미터로 명시 전달받는다.
 */
public class ShopConvenienceInfoService {

    private static final double MAX_DISPLAY_LOCATION_DISTANCE_METERS = 1000;

    private final ShopConvenienceInfoRepository shopConvenienceInfoRepository;
    private final ShopRepository shopRepository;
    private final ShopDetailRepository shopDetailRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopConvenienceInfoService(
        ShopConvenienceInfoRepository shopConvenienceInfoRepository,
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopConvenienceInfoRepository = shopConvenienceInfoRepository;
        this.shopRepository = shopRepository;
        this.shopDetailRepository = shopDetailRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    /**
     * 편의정보를 upsert 한다. 찾아오는길은 금칙어 검수를, 표시 위치는 가게 좌표 반경 검증을 통과해야 한다.
     *
     * <p>편의정보는 한 화면에서 주차·발렛·찾아오는길·표시위치를 통째로 저장하는 replace-all 성격이므로,
     * 필드별로 이력을 쪼개지 않고 <b>저장 1회당 1행</b>만 남긴다({@code UPDATE}). 필드마다 남기면 이력
     * 목록이 "점주가 저장한 횟수"가 아니라 "바뀐 필드 수"로 페이징되어 읽을 수 없게 된다.
     */
    public void upsertConvenienceInfo(
        Long shopId,
        Boolean parkingAvailable,
        Boolean parkingPaid,
        Boolean valetAvailable,
        Boolean valetPaid,
        String directionsGuide,
        BigDecimal displayLatitude,
        BigDecimal displayLongitude,
        ShopChangeActor actor
    ) {
        if (directionsGuide != null) {
            prohibitedWordValidator.validate(directionsGuide);
        }

        if (displayLatitude != null && displayLongitude != null) {
            validateDisplayLocation(shopId, displayLatitude, displayLongitude);
        }

        // 변경 전 요약을 update 호출 전에 확정한다 — 같은 인스턴스를 제자리에서 갱신하므로
        // 나중에 읽으면 이미 변경 후 값이다.
        ShopConvenienceInfo existing = shopConvenienceInfoRepository.findByShopId(shopId).orElse(null);
        String previousValue = describeConvenienceInfo(existing);

        ShopConvenienceInfo shopConvenienceInfo;
        if (existing == null) {
            shopConvenienceInfo = ShopConvenienceInfo.of(
                ShopId.of(shopId),
                parkingAvailable,
                parkingPaid,
                valetAvailable,
                valetPaid,
                directionsGuide,
                displayLatitude,
                displayLongitude
            );
        } else {
            existing.update(
                parkingAvailable,
                parkingPaid,
                valetAvailable,
                valetPaid,
                directionsGuide,
                displayLatitude,
                displayLongitude
            );
            shopConvenienceInfo = existing;
        }

        shopConvenienceInfoRepository.save(shopConvenienceInfo);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.CONVENIENCE_INFO,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeConvenienceInfo(shopConvenienceInfo)
        );
    }

    /**
     * 편의시설을 배정하고 이력을 남긴다. 존재하지 않는 카테고리는 거부한다.
     *
     * <p>이력 요약에 편의시설 이름을 담기 위해 카테고리를 로드하는데, 그 로드는 존재 검증과 같은
     * 조회이므로 추가 왕복이 생기지 않는다.
     *
     * @return 생성된 배정 식별자
     */
    public Long assignAmenity(Long shopId, Long amenityCategoryId, ShopChangeActor actor) {
        ShopAmenityCategory amenityCategory = shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));

        ShopAmenity amenity = shopDetailRepository.saveAmenity(
            ShopAmenity.of(ShopId.of(shopId), ShopAmenityCategoryId.of(amenityCategoryId))
        );

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.AMENITY,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeAmenity(amenityCategory)
        );
        return amenity.getId();
    }

    /**
     * 편의시설 배정을 해제하고 이력을 남긴다.
     *
     * <p>삭제된 행의 요약(편의시설 이름)이 필요하므로 삭제 전에 카테고리를 읽는다. 카테고리가 없으면
     * 애초에 배정될 수 없었으므로 존재하지 않는 카테고리는 거부한다 — 그래야 이력에 이름 없는
     * 삭제 행이 남지 않는다.
     */
    public void unassignAmenity(Long shopId, Long amenityCategoryId, ShopChangeActor actor) {
        ShopAmenityCategory amenityCategory = shopDetailRepository.findAmenityCategoryById(amenityCategoryId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_AMENITY_CATEGORY_NOT_FOUND));

        shopDetailRepository.deleteAmenityByShopIdAndCategoryId(shopId, amenityCategoryId);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.AMENITY,
            ShopChangeActionType.DELETE,
            actor,
            describeAmenity(amenityCategory),
            null
        );
    }

    /**
     * 편의정보 전체를 라벨 붙인 줄들로 요약한다(예: {@code "주차: 가능(유료)"} / {@code "찾아오는길: ..."}).
     *
     * <p>찾아오는길 문구는 자르지 않고 원문 그대로 담는다 — 무엇이 어떻게 달라졌는지 보려면 원문이
     * 필요하고, {@code previous_value}/{@code new_value}는 TEXT 컬럼이다. 아직 등록된 적이 없으면
     * "없음"으로 적어 "전부 미설정으로 저장함"과 구분한다.
     */
    private String describeConvenienceInfo(ShopConvenienceInfo convenienceInfo) {
        if (convenienceInfo == null) {
            return ShopChangeValueFormatter.snapshot(List.of());
        }

        List<String> lines = new ArrayList<>(4);
        lines.add("주차: " + describeFacility(convenienceInfo.isParkingAvailable(), convenienceInfo.isParkingPaid()));
        lines.add("발렛: " + describeFacility(convenienceInfo.isValetAvailable(), convenienceInfo.isValetPaid()));
        lines.add("찾아오는길: " + (convenienceInfo.getDirectionsGuide() == null || convenienceInfo.getDirectionsGuide().isBlank()
            ? ShopChangeValueFormatter.unset()
            : convenienceInfo.getDirectionsGuide()));
        lines.add("표시위치: " + describeDisplayLocation(
            convenienceInfo.getDisplayLatitude(), convenienceInfo.getDisplayLongitude()
        ));
        return ShopChangeValueFormatter.snapshot(lines);
    }

    /**
     * 주차·발렛 설정을 한 조각으로 요약한다(예: {@code "가능(유료)"} / {@code "불가"}).
     *
     * <p>불가일 때 유료 여부는 의미가 없으므로 적지 않는다 — "불가(무료)"는 읽는 사람을 혼란시킨다.
     */
    private String describeFacility(boolean available, boolean paid) {
        return available ? "가능(" + (paid ? "유료" : "무료") + ")" : "불가";
    }

    /**
     * 지도 표시 위치를 좌표 한 줄로 요약한다. 둘 중 하나라도 없으면 "미설정"이다.
     */
    private String describeDisplayLocation(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return ShopChangeValueFormatter.unset();
        }
        return latitude.toPlainString() + ", " + longitude.toPlainString();
    }

    /**
     * 편의시설 1행을 한 줄로 요약한다 — 관리자가 지정한 노출명을 쓰고, 없으면 enum 기본 표기로 폴백한다.
     */
    private String describeAmenity(ShopAmenityCategory amenityCategory) {
        String displayName = amenityCategory.getDisplayName();
        return displayName == null || displayName.isBlank()
            ? amenityCategory.getAmenity().getDisplayName()
            : displayName;
    }

    private void validateDisplayLocation(Long shopId, BigDecimal displayLatitude, BigDecimal displayLongitude) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        double distanceMeters = GeoDistance.distanceMeters(
            displayLatitude, displayLongitude, shop.getLatitude(), shop.getLongitude()
        );
        if (distanceMeters > MAX_DISPLAY_LOCATION_DISTANCE_METERS) {
            throw new BusinessException(ErrorCode.SHOP_DISPLAY_LOCATION_OUT_OF_RANGE);
        }
    }
}

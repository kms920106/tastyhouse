package com.tastyhouse.domain.shop.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopAmenity;
import com.tastyhouse.domain.shop.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.model.ShopBannerImage;
import com.tastyhouse.domain.shop.model.ShopBreakTime;
import com.tastyhouse.domain.shop.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.model.ShopClosedDay;
import com.tastyhouse.domain.shop.model.ShopFoodType;
import com.tastyhouse.domain.shop.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.model.ShopPhotoCategoryImage;

/**
 * 가게 자식 애그리거트(영업시간·휴게시간·정기휴무·카테고리·배정·배너·사진·사장님 한마디) write 포트.
 *
 * <p>command 경로·도메인 서비스가 트랜잭션 안에서 소비하는 것만 둔다 — 단건 로드({@code findXxxById}),
 * 저장·삭제, 그리고 <b>불변식 검증에 필요한 목록 조회</b>(휴게시간이 영업시간 범위 내인지 검증하는
 * {@link #findBusinessHoursByShopId(Long)}, 정기휴무 개수 제한을 검증하는
 * {@link #findClosedDaysByShopId(Long)}, 영업 상태 판정에 쓰이는 {@link #findBreakTimesByShopId(Long)}).
 *
 * <p>표현 목적 read(카테고리 목록·배정 목록·배너·사진 목록, 주문방식 배정 목록·사진 카테고리 목록·
 * 최신 사장님 한마디)는 infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로
 * 이관했다(공통 지침 패턴 4). 위 세 조회는 도메인 소비자가 없어 화면 전용이었다.
 *
 * <p>영업시간·정기휴무 목록은 도메인 소비자가 있어 이 포트에 남지만, 화면 조립용 같은 데이터는
 * {@code ShopQueryDao}의 투영 조회를 쓴다 — 목적(불변식 vs 표현)과 반환 타입이 달라 중복이 아니다.
 */
public interface ShopDetailRepository {

    Optional<ShopAmenityCategory> findAmenityCategoryById(Long id);

    ShopAmenityCategory saveAmenityCategory(ShopAmenityCategory amenityCategory);

    Optional<ShopFoodTypeCategory> findFoodTypeCategoryById(Long id);

    ShopFoodTypeCategory saveFoodTypeCategory(ShopFoodTypeCategory foodTypeCategory);

    ShopAmenity saveAmenity(ShopAmenity amenity);

    void deleteAmenityByShopIdAndCategoryId(Long shopId, Long shopAmenityCategoryId);

    ShopFoodType saveFoodType(ShopFoodType foodType);

    void deleteFoodTypeByShopIdAndCategoryId(Long shopId, Long shopFoodTypeCategoryId);

    /**
     * 가게의 영업시간 전체. 휴게시간 범위 검증(도메인 서비스)과 영업 상태 판정에 쓰인다.
     */
    List<ShopBusinessHour> findBusinessHoursByShopId(Long shopId);

    Optional<ShopBusinessHour> findBusinessHourById(Long id);

    ShopBusinessHour saveBusinessHour(ShopBusinessHour businessHour);

    void deleteBusinessHourById(Long id);

    /**
     * 가게의 휴게시간 전체. 영업 상태 판정(도메인 서비스)에 쓰인다.
     */
    List<ShopBreakTime> findBreakTimesByShopId(Long shopId);

    Optional<ShopBreakTime> findBreakTimeById(Long id);

    ShopBreakTime saveBreakTime(ShopBreakTime breakTime);

    void deleteBreakTimeById(Long id);

    /**
     * 가게의 정기휴무 전체. 등록 개수 제한 검증과 영업 상태 판정(도메인 서비스)에 쓰인다.
     */
    List<ShopClosedDay> findClosedDaysByShopId(Long shopId);

    /**
     * 정기휴무 단건. 삭제 시 변경이력에 남길 "무엇을 삭제했는지"(가게·휴무 종류)를 얻기 위해 필요하다 —
     * {@code deleteClosedDayById}는 식별자만 받아 삭제 후에는 그 정보를 복원할 수 없다.
     */
    Optional<ShopClosedDay> findClosedDayById(Long id);

    ShopClosedDay saveClosedDay(ShopClosedDay closedDay);

    void deleteClosedDayById(Long id);

    /**
     * 가게에 배정된 주문유형 전체. 주문 접수·예약 생성의 "지원하는 주문유형인가" 불변식 검증과
     * 예약주문 슬롯 판정에 쓰인다.
     *
     * <p>표현 목적 조회인 {@code ShopQueryDao#findOrderMethods}와 목적(불변식 vs 표현)·반환 타입이 달라
     * 중복이 아니다 — {@link #findBusinessHoursByShopId(Long)}가 같은 근거로 이 포트에 남아 있는 것과 동일하다.
     */
    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);

    ShopBannerImage saveBannerImage(ShopBannerImage bannerImage);

    void deleteBannerImageById(Long id);

    Optional<ShopPhotoCategory> findPhotoCategoryById(Long id);

    ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory);

    void deletePhotoCategoryById(Long id);

    Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id);

    ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);

    void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory);

    /**
     * 가게의 가장 최근 사장님 한마디. 변경이력({@code INTRODUCTION})의 <b>변경 전 값</b>을 얻기 위해
     * 도메인 소비자가 생겨 write 포트에 둔다(화면 조립용 투영 조회는 {@code ShopQueryDao} 쪽에 별도로 있다).
     *
     * <p>사장님 한마디는 갱신이 아니라 append-only 이력이므로 "현재 노출 중인 문구"는 곧 최신 행이다.
     * 아직 등록한 적이 없으면 빈 Optional을 반환한다.
     */
    Optional<ShopOwnerMessageHistory> findLatestOwnerMessage(Long shopId);
}

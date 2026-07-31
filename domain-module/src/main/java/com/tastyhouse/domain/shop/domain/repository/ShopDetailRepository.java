package com.tastyhouse.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shop.domain.model.OrderMethod;
import com.tastyhouse.domain.shop.domain.model.ShopAmenity;
import com.tastyhouse.domain.shop.domain.model.ShopAmenityCategory;
import com.tastyhouse.domain.shop.domain.model.ShopBannerImage;
import com.tastyhouse.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.domain.shop.domain.model.ShopFoodType;
import com.tastyhouse.domain.shop.domain.model.ShopFoodTypeCategory;
import com.tastyhouse.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.domain.shop.domain.model.ShopPhotoCategoryImage;

/**
 * 가게 자식 애그리거트(영업시간·휴게시간·정기휴무·카테고리·배정·배너·사진·사장님 한마디) write 포트.
 *
 * <p>command 경로·도메인 서비스가 트랜잭션 안에서 소비하는 것만 둔다 — 단건 로드({@code findXxxById}),
 * 저장·삭제, 그리고 <b>불변식 검증에 필요한 목록 조회</b>(휴게시간이 영업시간 범위 내인지 검증하는
 * {@link #findBusinessHoursByShopId(Long)}, 정기휴무 개수 제한을 검증하는
 * {@link #findClosedDaysByShopId(Long)}, 영업 상태 판정에 쓰이는 {@link #findBreakTimesByShopId(Long)}).
 *
 * <p>Result DTO를 반환하던 표현 목적 read(카테고리 목록·배정 목록·배너·사진 목록 등)는
 * infrastructure-module의 {@code infrastructure/shop/query/ShopQueryDao}로 이관했다(공통 지침 패턴 4).
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

    ShopClosedDay saveClosedDay(ShopClosedDay closedDay);

    void deleteClosedDayById(Long id);

    ShopOrderMethod saveOrderMethod(ShopOrderMethod orderMethod);

    void deleteOrderMethodByShopIdAndOrderMethod(Long shopId, OrderMethod orderMethod);

    /**
     * 가게의 주문방식 배정 전체. 회원 상세의 주문방식 노출에 쓰인다.
     */
    List<ShopOrderMethod> findOrderMethodsByShopId(Long shopId);

    ShopBannerImage saveBannerImage(ShopBannerImage bannerImage);

    void deleteBannerImageById(Long id);

    /**
     * 가게의 사진 카테고리 전체. 카테고리별 사진 묶음 조립에 쓰인다.
     */
    List<ShopPhotoCategory> findPhotoCategoriesByShopId(Long shopId);

    Optional<ShopPhotoCategory> findPhotoCategoryById(Long id);

    ShopPhotoCategory savePhotoCategory(ShopPhotoCategory photoCategory);

    void deletePhotoCategoryById(Long id);

    Optional<ShopPhotoCategoryImage> findPhotoCategoryImageById(Long id);

    ShopPhotoCategoryImage savePhotoCategoryImage(ShopPhotoCategoryImage photoCategoryImage);

    void deletePhotoCategoryImageById(Long id);

    /**
     * 가게의 최신 사장님 한마디. 가게소개 조회·수정 화면이 소비한다.
     */
    Optional<ShopOwnerMessageHistory> findLatestOwnerMessageByShopId(Long shopId);

    void saveOwnerMessage(ShopOwnerMessageHistory ownerMessageHistory);
}

package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.model.ShopImageType;

/**
 * shop 읽기 포트(CQRS query 측 아웃바운드 포트).
 *
 * <p>완전 매핑 전환으로 <b>응용 계층이 읽기 계약을 소유</b>하고 infrastructure-module의
 * {@code ShopQueryDao}가 이를 구현한다. 소비 모듈은 이 인터페이스와 같은 패키지의 반환 DTO
 * ({@code *Result})·검색 조건({@code *SearchCondition})만 알며, QueryDSL도 어댑터의 존재도 알지 않는다.
 *
 * <p>메서드명·시그니처는 DAO의 기존 공개 표면을 그대로 전사한 것이다(챕터 04는 순수 소유권 이동이라
 * 조회 동작·wire 계약을 바꾸지 않는다).
 */
public interface ShopQueryPort {

    Optional<String> findShopName(Long shopId);

    List<ShopPhoneNumberResult> findPhoneNumbers(Long shopId);

    Optional<ShopImageUrlsResult> findShopImageUrls(Long shopId);

    Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId);

    Optional<ShopOriginInfoResult> findOriginInfo(Long shopId);

    List<ShopContentBoardResult> findContentBoards(Long shopId);

    PageResult<ShopContentBoardResult> findContentBoardPage(Long shopId, Boolean hidden, ShopContentType contentType, PageQuery pageQuery);

    List<ShopHygieneBadgeResult> findHygieneBadges(Long shopId);

    List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, ShopImageType imageType);

    PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(ApprovalStatus status, ShopImageType imageType, PageQuery pageQuery);

    List<ShopSuspensionResult> findSuspensions(Long shopId);

    List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId);

    List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories();

    List<ShopAmenityCategoryResult> findVisibleAmenityCategories();

    List<ShopAmenityCategoryResult> findAllAmenityCategories();

    List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories();

    List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId);

    List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId);

    List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId);

    List<String> findFoodTypeCategoryNames(Long shopId);

    List<ShopBannerImageResult> findBannerImages(Long shopId);

    List<ShopMenuCollectionImageResult> findMenuCollectionImages(Long shopId);

    List<ShopMenuCollectionImageExposureResult> findExposedMenuCollectionImages(Long shopId);

    PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(ApprovalStatus status, PageQuery pageQuery);

    List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages();

    List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId);

    List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId);

    List<ShopOrderMethodResult> findOrderMethods(Long shopId);

    Optional<ShopOwnerMessageResult> findLatestOwnerMessage(Long shopId);

    List<ShopBusinessHourResult> findBusinessHours(Long shopId);

    List<ShopBreakTimeResult> findBreakTimes(Long shopId);

    List<ShopClosedDayResult> findClosedDays(Long shopId);

    /**
     * 회원 노출용 가게 단건. 폐업·노출정지 가게는 조회되지 않아 딥링크 진입이 차단된다.
     */
    Optional<ShopVisibleDetailResult> findVisibleDetailById(Long shopId);

    /**
     * 회원의 가게 북마크 여부. 표현용 단건 판정이라 write 포트가 아니라 이 포트가 답한다.
     */
    boolean existsBookmark(Long shopId, Long memberId);

    /**
     * 가게 관리 상세 조회 — 폐업 가게도 조회된다.
     */
    Optional<ShopManagementDetailResult> findManagementDetailById(Long shopId);

}

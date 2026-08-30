package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.model.ShopImageType;

/**
 * 가게 관리 화면 조회 포트(CQRS query 측 아웃바운드 포트) — 관리자용.
 *
 * <p>가게 관리 상세와, 점주가 올린 변경 요청(콘텐츠보드·이미지·메뉴판 이미지)의 검수 목록,
 * 그리고 편의시설·음식종류 분류의 전체 목록을 조회한다. 회원 화면 조회는 {@link ShopQueryPort},
 * 점주 관리 화면 조회는 {@link ShopOwnerQueryPort}, 여러 앱이 함께 보는 기본 정보는
 * {@link ShopBasicInfoQueryPort}가 소유한다.
 *
 * <p>{@code find*Page}가 승인 상태({@link ApprovalStatus})를 받는 데서 보이듯, 관리자는 개별 가게를
 * 편집하는 것이 아니라 요청을 <b>검수</b>한다. 반면 {@code findAll*Categories}는 노출 여부와 무관한
 * 전수 조회라 회원 화면의 {@code findVisible*Categories}와 짝을 이룬다.
 */
public interface ShopManagementQueryPort {

    PageResult<ShopContentBoardResult> findContentBoardPage(Long shopId, Boolean hidden, ShopContentType contentType, PageQuery pageQuery);

    PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(ApprovalStatus status, ShopImageType imageType, PageQuery pageQuery);

    PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(ApprovalStatus status, PageQuery pageQuery);

    List<ShopAmenityCategoryResult> findAllAmenityCategories();

    List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories();

    List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId);

    List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId);

    Optional<ShopManagementDetailResult> findManagementDetailById(Long shopId);
}

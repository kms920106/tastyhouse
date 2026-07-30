package com.tastyhouse.infrastructure.shop.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.shared.model.ApprovalStatus;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBannerImageJpaEntity.shopBannerImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopContentBoardJpaEntity.shopContentBoardJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopConvenienceInfoJpaEntity.shopConvenienceInfoJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopHygieneBadgeJpaEntity.shopHygieneBadgeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhoneNumberJpaEntity.shopPhoneNumberJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryImageJpaEntity.shopPhotoCategoryImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopSuspensionJpaEntity.shopSuspensionJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopTemporaryClosureJpaEntity.shopTemporaryClosureJpaEntity;

/**
 * 가게 관리·설정 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code ShopRepository}/{@code ShopDetailRepository} 등)와 역할이 겹치지 않는다. 소비 모듈
 * (web/admin/ceo-api)의 {@code Shop*QueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은
 * QueryDSL을 알지 않는다.
 *
 * <p>shop은 대형 도메인이므로 공통 지침의 용도별 분리 허용에 따라 DAO를 둘로 나눈다 — 이 클래스는
 * <b>가게별 설정·관리 화면 조회</b>(전화번호·편의정보·콘텐츠보드·위생뱃지·이미지 변경요청·편의시설/음식유형
 * 배정·배너·사진)를 담당하고, 목록·검색·베스트 등 대형 조인은 {@link ShopSearchQueryDao}가 담당한다.
 *
 * <p>소비자별 메서드는 CLAUDE.md 규칙대로 admin 마커 없이 순수 동작명을 쓰고, 비-admin 형제와 충돌할
 * 때만 시그니처·{@code ById} 한정어로 구별한다.
 */
@Repository
@RequiredArgsConstructor
public class ShopQueryDao {

    /**
     * 카테고리의 활성/비활성 아이콘을 한 쿼리에서 함께 투영하기 위한 파일 테이블 별칭.
     */
    private static final QUploadedFileJpaEntity activeFile = new QUploadedFileJpaEntity("activeFile");
    private static final QUploadedFileJpaEntity inactiveFile = new QUploadedFileJpaEntity("inactiveFile");

    private final JPAQueryFactory queryFactory;

    // ---------------------------------------------------------------- 전화번호

    /**
     * 가게 전화번호 목록 — 대표번호 우선, 그다음 등록 순.
     */
    public List<ShopPhoneNumberResult> findPhoneNumbers(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopPhoneNumberResult.class,
                shopPhoneNumberJpaEntity.id,
                shopPhoneNumberJpaEntity.shopId,
                shopPhoneNumberJpaEntity.phoneNumber,
                shopPhoneNumberJpaEntity.primary,
                shopPhoneNumberJpaEntity.virtual
            ))
            .from(shopPhoneNumberJpaEntity)
            .where(shopPhoneNumberJpaEntity.shopId.eq(shopId))
            .orderBy(shopPhoneNumberJpaEntity.primary.desc(), shopPhoneNumberJpaEntity.id.asc())
            .fetch();
    }

    // ---------------------------------------------------------------- 편의정보

    /**
     * 가게 편의정보 단건(가게당 1건). 없으면 비어 있다.
     */
    public Optional<ShopConvenienceInfoResult> findConvenienceInfo(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopConvenienceInfoResult.class,
                    shopConvenienceInfoJpaEntity.id,
                    shopConvenienceInfoJpaEntity.shopId,
                    shopConvenienceInfoJpaEntity.parkingAvailable,
                    shopConvenienceInfoJpaEntity.parkingPaid,
                    shopConvenienceInfoJpaEntity.valetAvailable,
                    shopConvenienceInfoJpaEntity.valetPaid,
                    shopConvenienceInfoJpaEntity.directionsGuide,
                    shopConvenienceInfoJpaEntity.displayLatitude,
                    shopConvenienceInfoJpaEntity.displayLongitude
                ))
                .from(shopConvenienceInfoJpaEntity)
                .where(shopConvenienceInfoJpaEntity.shopId.eq(shopId))
                .fetchFirst()
        );
    }

    // ------------------------------------------------------------ 콘텐츠보드

    /**
     * 가게 콘텐츠보드 목록(점주 화면) — 등록 순.
     */
    public List<ShopContentBoardResult> findContentBoards(Long shopId) {
        return contentBoardProjection()
            .where(shopContentBoardJpaEntity.shopId.eq(shopId))
            .orderBy(shopContentBoardJpaEntity.id.asc())
            .fetch();
    }

    /**
     * 콘텐츠보드 목록 페이징(관리 화면) — 가게·숨김여부·콘텐츠 유형으로 필터하며, 최근 등록 순.
     */
    public PageResult<ShopContentBoardResult> findContentBoardPage(
        Long shopId,
        Boolean hidden,
        ShopContentType contentType,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopContentBoardJpaEntity.count())
            .from(shopContentBoardJpaEntity)
            .where(
                contentBoardShopIdEq(shopId),
                contentBoardHiddenEq(hidden),
                contentBoardContentTypeEq(contentType)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopContentBoardResult> content = contentBoardProjection()
            .where(
                contentBoardShopIdEq(shopId),
                contentBoardHiddenEq(hidden),
                contentBoardContentTypeEq(contentType)
            )
            .orderBy(shopContentBoardJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopContentBoardResult> contentBoardProjection() {
        return queryFactory
            .select(Projections.constructor(ShopContentBoardResult.class,
                shopContentBoardJpaEntity.id,
                shopContentBoardJpaEntity.shopId,
                shopContentBoardJpaEntity.contentType,
                shopContentBoardJpaEntity.topic,
                shopContentBoardJpaEntity.imageFileId,
                shopContentBoardJpaEntity.youtubeUrl,
                shopContentBoardJpaEntity.description,
                shopContentBoardJpaEntity.hidden,
                shopContentBoardJpaEntity.createdAt
            ))
            .from(shopContentBoardJpaEntity);
    }

    private BooleanExpression contentBoardShopIdEq(Long shopId) {
        return shopId != null ? shopContentBoardJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression contentBoardHiddenEq(Boolean hidden) {
        return hidden != null ? shopContentBoardJpaEntity.hidden.eq(hidden) : null;
    }

    private BooleanExpression contentBoardContentTypeEq(ShopContentType contentType) {
        return contentType != null ? shopContentBoardJpaEntity.contentType.eq(contentType) : null;
    }

    // -------------------------------------------------------------- 위생 뱃지

    /**
     * 가게 위생 인증 뱃지 목록 — 인증일 최신 순.
     */
    public List<ShopHygieneBadgeResult> findHygieneBadges(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopHygieneBadgeResult.class,
                shopHygieneBadgeJpaEntity.id,
                shopHygieneBadgeJpaEntity.shopId,
                shopHygieneBadgeJpaEntity.badgeType,
                shopHygieneBadgeJpaEntity.certifiedDate,
                shopHygieneBadgeJpaEntity.lastInspectionMonth
            ))
            .from(shopHygieneBadgeJpaEntity)
            .where(shopHygieneBadgeJpaEntity.shopId.eq(shopId))
            .orderBy(shopHygieneBadgeJpaEntity.certifiedDate.desc())
            .fetch();
    }

    // ------------------------------------------------------- 이미지 변경요청

    /**
     * 가게의 이미지 변경요청 목록(점주 화면) — 최근 요청 순.
     */
    public List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId) {
        return imageChangeRequestProjection()
            .where(shopImageChangeRequestJpaEntity.shopId.eq(shopId))
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .fetch();
    }

    /**
     * 이미지 변경요청 목록 페이징(검수 화면) — 승인 상태·이미지 유형으로 필터하며, 최근 요청 순.
     */
    public PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(
        ApprovalStatus status,
        ShopImageType imageType,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopImageChangeRequestJpaEntity.count())
            .from(shopImageChangeRequestJpaEntity)
            .where(imageChangeStatusEq(status), imageChangeImageTypeEq(imageType))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopImageChangeRequestResult> content = imageChangeRequestProjection()
            .where(imageChangeStatusEq(status), imageChangeImageTypeEq(imageType))
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ShopImageChangeRequestResult.class,
                shopImageChangeRequestJpaEntity.id,
                shopImageChangeRequestJpaEntity.shopId,
                shopImageChangeRequestJpaEntity.imageType,
                shopImageChangeRequestJpaEntity.imageFileId,
                shopImageChangeRequestJpaEntity.status,
                shopImageChangeRequestJpaEntity.rejectReason
            ))
            .from(shopImageChangeRequestJpaEntity);
    }

    private BooleanExpression imageChangeStatusEq(ApprovalStatus status) {
        return status != null ? shopImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression imageChangeImageTypeEq(ShopImageType imageType) {
        return imageType != null ? shopImageChangeRequestJpaEntity.imageType.eq(imageType) : null;
    }

    // ------------------------------------------------------------ 임시중지·휴무

    /**
     * 가게 영업 임시중지 목록 — 최근 시작 순.
     */
    public List<ShopSuspensionResult> findSuspensions(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopSuspensionResult.class,
                shopSuspensionJpaEntity.id,
                shopSuspensionJpaEntity.shopId,
                shopSuspensionJpaEntity.reason,
                shopSuspensionJpaEntity.orderMethod,
                shopSuspensionJpaEntity.startAt,
                shopSuspensionJpaEntity.endAt,
                shopSuspensionJpaEntity.releasedAt
            ))
            .from(shopSuspensionJpaEntity)
            .where(shopSuspensionJpaEntity.shopId.eq(shopId))
            .orderBy(shopSuspensionJpaEntity.startAt.desc())
            .fetch();
    }

    /**
     * 가게 임시 휴무 목록 — 시작일 순.
     */
    public List<ShopTemporaryClosureResult> findTemporaryClosures(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopTemporaryClosureResult.class,
                shopTemporaryClosureJpaEntity.id,
                shopTemporaryClosureJpaEntity.shopId,
                shopTemporaryClosureJpaEntity.startDate,
                shopTemporaryClosureJpaEntity.endDate
            ))
            .from(shopTemporaryClosureJpaEntity)
            .where(shopTemporaryClosureJpaEntity.shopId.eq(shopId))
            .orderBy(shopTemporaryClosureJpaEntity.startDate.asc())
            .fetch();
    }

    // ------------------------------------------------- 편의시설·음식유형 카테고리

    /**
     * 노출 중인 음식 유형 카테고리 목록(회원 화면) — 정렬 순.
     */
    public List<ShopFoodTypeCategoryResult> findVisibleFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .where(shopFoodTypeCategoryJpaEntity.visible.eq(true))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 노출 중인 편의시설 카테고리 목록(회원 화면) — 정렬 순.
     */
    public List<ShopAmenityCategoryResult> findVisibleAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .join(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .where(shopAmenityCategoryJpaEntity.visible.eq(true))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 전체 편의시설 카테고리 목록(관리 화면 — 미노출분 포함) — 정렬 순.
     */
    public List<ShopAmenityCategoryResult> findAllAmenityCategories() {
        return queryFactory
            .select(Projections.constructor(ShopAmenityCategoryResult.class,
                shopAmenityCategoryJpaEntity.id,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopAmenityCategoryJpaEntity.sort,
                shopAmenityCategoryJpaEntity.visible
            ))
            .from(shopAmenityCategoryJpaEntity)
            .leftJoin(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .leftJoin(inactiveFile).on(inactiveFile.id.eq(shopAmenityCategoryJpaEntity.inactiveImageFileId))
            .orderBy(shopAmenityCategoryJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 전체 음식 유형 카테고리 목록(관리 화면 — 미노출분 포함) — 정렬 순.
     */
    public List<ShopFoodTypeCategoryResult> findAllFoodTypeCategories() {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeCategoryResult.class,
                shopFoodTypeCategoryJpaEntity.id,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath,
                inactiveFile.filePath,
                shopFoodTypeCategoryJpaEntity.sort,
                shopFoodTypeCategoryJpaEntity.visible
            ))
            .from(shopFoodTypeCategoryJpaEntity)
            .leftJoin(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .leftJoin(inactiveFile).on(inactiveFile.id.eq(shopFoodTypeCategoryJpaEntity.inactiveImageFileId))
            .orderBy(shopFoodTypeCategoryJpaEntity.sort.asc())
            .fetch();
    }

    // ------------------------------------------------------ 가게별 배정 목록

    /**
     * 가게에 배정된 편의시설 목록(카테고리 정보 포함, 관리·설정 화면).
     */
    public List<ShopAmenityAssignmentResult> findAmenityAssignments(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityAssignmentResult.class,
                shopAmenityJpaEntity.id,
                shopAmenityJpaEntity.shopAmenityCategoryId,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    /**
     * 가게에 배정된 편의시설 목록(회원 상세 화면 — 배정 식별자 없이 표시용 필드만).
     */
    public List<ShopAmenityWithCategoryResult> findAmenitiesWithCategory(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopAmenityWithCategoryResult.class,
                shopAmenityCategoryJpaEntity.amenity,
                shopAmenityCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopAmenityJpaEntity)
            .join(shopAmenityCategoryJpaEntity).on(shopAmenityCategoryJpaEntity.id.eq(shopAmenityJpaEntity.shopAmenityCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopAmenityCategoryJpaEntity.activeImageFileId))
            .where(shopAmenityJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    /**
     * 가게에 배정된 음식 유형 목록(카테고리 정보 포함, 관리 화면).
     */
    public List<ShopFoodTypeAssignmentResult> findFoodTypeAssignments(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopFoodTypeAssignmentResult.class,
                shopFoodTypeJpaEntity.id,
                shopFoodTypeJpaEntity.shopFoodTypeCategoryId,
                shopFoodTypeCategoryJpaEntity.foodType,
                shopFoodTypeCategoryJpaEntity.displayName,
                activeFile.filePath
            ))
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeCategoryJpaEntity.id.eq(shopFoodTypeJpaEntity.shopFoodTypeCategoryId))
            .join(activeFile).on(activeFile.id.eq(shopFoodTypeCategoryJpaEntity.activeImageFileId))
            .where(shopFoodTypeJpaEntity.shopId.eq(shopId))
            .fetch();
    }

    // ------------------------------------------------------------ 배너·사진

    /**
     * 가게 배너 이미지 목록(파일 경로 포함) — 정렬 순.
     */
    public List<ShopBannerImageResult> findBannerImages(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBannerImageResult.class,
                shopBannerImageJpaEntity.id,
                uploadedFileJpaEntity.filePath,
                shopBannerImageJpaEntity.sort
            ))
            .from(shopBannerImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopBannerImageJpaEntity.imageFileId))
            .where(shopBannerImageJpaEntity.shopId.eq(shopId))
            .orderBy(shopBannerImageJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 전체 사진 카테고리 이미지 목록(파일 경로 포함) — 정렬 순. 소비 측이 카테고리별로 묶어 쓴다.
     */
    public List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages() {
        return photoCategoryImageProjection()
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch();
    }

    /**
     * 특정 사진 카테고리의 이미지 목록(관리 화면 — 노출 여부 포함) — 정렬 순.
     *
     * <p>관리 화면은 미노출 이미지도 함께 보여주고 그 상태를 표시해야 하므로 {@code visible}을 담은
     * {@link ShopPhotoCategoryImageManagementResult}를 돌려준다.
     */
    public List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId) {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageManagementResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                uploadedFileJpaEntity.filePath,
                shopPhotoCategoryImageJpaEntity.sort,
                shopPhotoCategoryImageJpaEntity.visible
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId))
            .where(shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId.eq(shopPhotoCategoryId))
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch();
    }

    private JPQLQuery<ShopPhotoCategoryImageResult> photoCategoryImageProjection() {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryImageResult.class,
                shopPhotoCategoryImageJpaEntity.id,
                shopPhotoCategoryImageJpaEntity.shopPhotoCategoryId,
                uploadedFileJpaEntity.filePath,
                shopPhotoCategoryImageJpaEntity.sort
            ))
            .from(shopPhotoCategoryImageJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopPhotoCategoryImageJpaEntity.imageFileId));
    }
}

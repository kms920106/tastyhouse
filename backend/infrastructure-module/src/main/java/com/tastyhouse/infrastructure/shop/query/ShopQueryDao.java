package com.tastyhouse.infrastructure.shop.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityCategoryJpaEntity.shopAmenityCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopAmenityJpaEntity.shopAmenityJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBannerImageJpaEntity.shopBannerImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBreakTimeJpaEntity.shopBreakTimeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopBusinessHourJpaEntity.shopBusinessHourJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopClosedDayJpaEntity.shopClosedDayJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopContentBoardJpaEntity.shopContentBoardJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopConvenienceInfoJpaEntity.shopConvenienceInfoJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeCategoryJpaEntity.shopFoodTypeCategoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopFoodTypeJpaEntity.shopFoodTypeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopHygieneBadgeJpaEntity.shopHygieneBadgeJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopImageChangeRequestJpaEntity.shopImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOrderMethodJpaEntity.shopOrderMethodJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopOwnerMessageHistoryJpaEntity.shopOwnerMessageHistoryJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhoneNumberJpaEntity.shopPhoneNumberJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryImageJpaEntity.shopPhotoCategoryImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopPhotoCategoryJpaEntity.shopPhotoCategoryJpaEntity;
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
public class ShopQueryDao {

    /**
     * 카테고리의 활성/비활성 아이콘을 한 쿼리에서 함께 투영하기 위한 파일 테이블 별칭.
     */
    private static final QUploadedFileJpaEntity activeFile = new QUploadedFileJpaEntity("activeFile");
    private static final QUploadedFileJpaEntity inactiveFile = new QUploadedFileJpaEntity("inactiveFile");

    /**
     * 콘텐츠보드/이미지 변경요청 조회에서 이미지 파일을 조인하기 위한 파일 테이블 별칭.
     */
    private static final QUploadedFileJpaEntity contentBoardImageFile = new QUploadedFileJpaEntity("contentBoardImageFile");
    private static final QUploadedFileJpaEntity imageChangeRequestImageFile = new QUploadedFileJpaEntity("imageChangeRequestImageFile");

    /**
     * 가게 상세 조립 시 썸네일/상표 이미지를 함께 조회하기 위한 파일 테이블 별칭.
     */
    private static final QUploadedFileJpaEntity shopThumbnailFile = new QUploadedFileJpaEntity("shopThumbnailFile");
    private static final QUploadedFileJpaEntity shopTrademarkFile = new QUploadedFileJpaEntity("shopTrademarkFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    // ------------------------------------------------------------------- 가게명

    /**
     * 가게명 단건 — 다른 컨텍스트가 표시 문구를 조립할 때 쓴다(알림 본문 등).
     *
     * <p>도메인 모델({@code Shop})을 통째로 로드하지 않는 이유는 소비처가 이름 한 필드만 필요하고, 그
     * 소비처가 애그리거트 경계 밖(알림 리스너)이라 도메인 모델을 넘기면 컨텍스트가 결합되기 때문이다.
     */
    public Optional<String> findShopName(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(shopJpaEntity.name)
                .from(shopJpaEntity)
                .where(shopJpaEntity.id.eq(shopId))
                .fetchOne()
        );
    }

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

    // ------------------------------------------------------------ 가게 상세 이미지

    /**
     * 가게 썸네일/상표 이미지 표시용 URL(가게 상세 조립용). 도메인 모델({@code Shop})은 다른 필드를
     * 위해 계속 로드하되, 이미지 URL만 이 조회로 대체해 파일 단건 재조회를 없앤다.
     */
    public Optional<ShopImageUrlsResult> findShopImageUrls(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopImageUrlsResult.class,
                    shopJpaEntity.id,
                    shopThumbnailFile.filePath,
                    shopTrademarkFile.filePath
                ))
                .from(shopJpaEntity)
                .leftJoin(shopThumbnailFile).on(shopThumbnailFile.id.eq(shopJpaEntity.thumbnailImageFileId))
                .leftJoin(shopTrademarkFile).on(shopTrademarkFile.id.eq(shopJpaEntity.trademarkImageFileId))
                .where(shopJpaEntity.id.eq(shopId))
                .fetchOne()
        ).map(this::withResolvedImageUrls);
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
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
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
                shopContentBoardJpaEntity.shopId.eq(shopId),
                contentBoardHiddenEq(hidden),
                contentBoardContentTypeEq(contentType)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopContentBoardResult> content = contentBoardProjection()
            .where(
                shopContentBoardJpaEntity.shopId.eq(shopId),
                contentBoardHiddenEq(hidden),
                contentBoardContentTypeEq(contentType)
            )
            .orderBy(shopContentBoardJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopContentBoardResult> contentBoardProjection() {
        return queryFactory
            .select(Projections.constructor(ShopContentBoardResult.class,
                shopContentBoardJpaEntity.id,
                shopContentBoardJpaEntity.shopId,
                shopContentBoardJpaEntity.contentType,
                shopContentBoardJpaEntity.topic,
                contentBoardImageFile.filePath,
                shopContentBoardJpaEntity.youtubeUrl,
                shopContentBoardJpaEntity.description,
                shopContentBoardJpaEntity.hidden,
                shopContentBoardJpaEntity.createdAt
            ))
            .from(shopContentBoardJpaEntity)
            .leftJoin(contentBoardImageFile).on(contentBoardImageFile.id.eq(shopContentBoardJpaEntity.imageFileId));
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
     * 가게의 이미지 변경요청 목록(점주 화면) — 이미지 유형별로 걸러 최근 요청 순.
     *
     * <p>상표·대표이미지는 화면에서 각각 독립된 항목으로 "검수 대기 중" 배지를 표시하므로,
     * 유형 필터 없이 반환하면 한쪽 유형의 PENDING 요청이 다른 쪽 배지까지 켠다.
     */
    public List<ShopImageChangeRequestResult> findImageChangeRequests(Long shopId, ShopImageType imageType) {
        return imageChangeRequestProjection()
            .where(
                shopImageChangeRequestJpaEntity.shopId.eq(shopId),
                imageChangeImageTypeEq(imageType)
            )
            .orderBy(shopImageChangeRequestJpaEntity.id.desc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private JPQLQuery<ShopImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ShopImageChangeRequestResult.class,
                shopImageChangeRequestJpaEntity.id,
                shopImageChangeRequestJpaEntity.shopId,
                shopImageChangeRequestJpaEntity.imageType,
                imageChangeRequestImageFile.filePath,
                shopImageChangeRequestJpaEntity.status,
                shopImageChangeRequestJpaEntity.rejectReason
            ))
            .from(shopImageChangeRequestJpaEntity)
            .leftJoin(imageChangeRequestImageFile).on(imageChangeRequestImageFile.id.eq(shopImageChangeRequestJpaEntity.imageFileId));
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrls)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrl)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrl)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedIconUrl)
            .toList();
    }

    /**
     * 가게에 배정된 음식 유형의 <b>화면 표시명만</b> 뽑는다.
     *
     * <p>{@link #findFoodTypeAssignments}와 목적이 다르다. 그쪽은 관리 화면용이라 아이콘 파일을
     * 조인하고 URL까지 완성하지만, 이 메서드는 <b>정책 판정</b>에 쓰이는 이름 집합만 필요하다 —
     * 채식 메뉴 등록 불가 카테고리 판정(product 컨텍스트)이 소비자다. 아이콘 조인을 함께 끌고 오면
     * 판정에 쓰이지 않는 파일 조회가 얹히고, {@code activeImageFileId} 결측 시 inner join으로
     * 카테고리가 조용히 누락돼 <b>거절해야 할 요청이 통과</b>한다.
     */
    public List<String> findFoodTypeCategoryNames(Long shopId) {
        return queryFactory
            .select(shopFoodTypeCategoryJpaEntity.displayName)
            .from(shopFoodTypeJpaEntity)
            .join(shopFoodTypeCategoryJpaEntity).on(shopFoodTypeCategoryJpaEntity.id.eq(shopFoodTypeJpaEntity.shopFoodTypeCategoryId))
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
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    /**
     * 전체 사진 카테고리 이미지 목록(파일 경로 포함) — 정렬 순. 소비 측이 카테고리별로 묶어 쓴다.
     */
    public List<ShopPhotoCategoryImageResult> findAllPhotoCategoryImages() {
        return photoCategoryImageProjection()
            .orderBy(shopPhotoCategoryImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
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
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    /**
     * 가게 사진 카테고리 목록(회원 상세·관리 화면) — 등록 순.
     */
    public List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopPhotoCategoryResult.class,
                shopPhotoCategoryJpaEntity.id,
                shopPhotoCategoryJpaEntity.name
            ))
            .from(shopPhotoCategoryJpaEntity)
            .where(shopPhotoCategoryJpaEntity.shopId.eq(shopId))
            .orderBy(shopPhotoCategoryJpaEntity.id.asc())
            .fetch();
    }

    // ------------------------------------------------------------ 주문방식

    /**
     * 가게에 배정된 주문방식 목록(회원 상세·관리 화면) — 등록 순.
     */
    public List<ShopOrderMethodResult> findOrderMethods(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopOrderMethodResult.class,
                shopOrderMethodJpaEntity.id,
                shopOrderMethodJpaEntity.orderMethod
            ))
            .from(shopOrderMethodJpaEntity)
            .where(shopOrderMethodJpaEntity.shopId.eq(shopId))
            .orderBy(shopOrderMethodJpaEntity.id.asc())
            .fetch();
    }

    // -------------------------------------------------------- 사장님 한마디

    /**
     * 가게의 최신 사장님 한마디(회원 가게정보·점주 가게소개 화면). 없으면 비어 있다.
     */
    public Optional<ShopOwnerMessageResult> findLatestOwnerMessage(Long shopId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(ShopOwnerMessageResult.class,
                    shopOwnerMessageHistoryJpaEntity.message,
                    shopOwnerMessageHistoryJpaEntity.createdAt
                ))
                .from(shopOwnerMessageHistoryJpaEntity)
                .where(shopOwnerMessageHistoryJpaEntity.shopId.eq(shopId))
                .orderBy(shopOwnerMessageHistoryJpaEntity.createdAt.desc())
                .fetchFirst()
        );
    }

    // -------------------------------------------------- 영업시간·정기휴무(표현용)

    /**
     * 가게 영업시간 목록(회원 가게정보·점주 설정·관리 화면) — 요일 순.
     *
     * <p>같은 데이터를 도메인 서비스도 읽지만 그쪽은 write 포트로 도메인 모델을 로드한다 —
     * 목적(불변식 검증 vs 화면 표현)과 반환 타입이 다르므로 중복이 아니다.
     */
    public List<ShopBusinessHourResult> findBusinessHours(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBusinessHourResult.class,
                shopBusinessHourJpaEntity.id,
                shopBusinessHourJpaEntity.dayType,
                shopBusinessHourJpaEntity.openTime,
                shopBusinessHourJpaEntity.closeTime,
                shopBusinessHourJpaEntity.isClosed,
                shopBusinessHourJpaEntity.is24Hours
            ))
            .from(shopBusinessHourJpaEntity)
            .where(shopBusinessHourJpaEntity.shopId.eq(shopId))
            .orderBy(shopBusinessHourJpaEntity.dayType.asc())
            .fetch();
    }

    /**
     * 가게 휴게시간 목록(회원 가게정보·점주 설정·관리 화면) — 요일 순.
     *
     * <p>{@link #findBusinessHours(Long)}과 같은 이유로 write 포트의 목록 조회와 공존한다.
     */
    public List<ShopBreakTimeResult> findBreakTimes(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopBreakTimeResult.class,
                shopBreakTimeJpaEntity.id,
                shopBreakTimeJpaEntity.dayType,
                shopBreakTimeJpaEntity.startTime,
                shopBreakTimeJpaEntity.endTime
            ))
            .from(shopBreakTimeJpaEntity)
            .where(shopBreakTimeJpaEntity.shopId.eq(shopId))
            .orderBy(shopBreakTimeJpaEntity.dayType.asc())
            .fetch();
    }

    /**
     * 가게 정기휴무 목록(회원 가게정보·점주 설정·관리 화면) — 등록 순.
     *
     * <p>{@link #findBusinessHours(Long)}과 같은 이유로 write 포트의 목록 조회와 공존한다.
     */
    public List<ShopClosedDayResult> findClosedDays(Long shopId) {
        return queryFactory
            .select(Projections.constructor(ShopClosedDayResult.class,
                shopClosedDayJpaEntity.id,
                shopClosedDayJpaEntity.closedDayType
            ))
            .from(shopClosedDayJpaEntity)
            .where(shopClosedDayJpaEntity.shopId.eq(shopId))
            .orderBy(shopClosedDayJpaEntity.id.asc())
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. 아래 메서드들은 {@code Projections.constructor}가
     * 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
    private ShopFoodTypeCategoryResult withResolvedIconUrls(ShopFoodTypeCategoryResult row) {
        return new ShopFoodTypeCategoryResult(
            row.id(),
            row.foodType(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl()),
            fileUrlResolver.resolve(row.inactiveIconUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ShopAmenityCategoryResult withResolvedIconUrls(ShopAmenityCategoryResult row) {
        return new ShopAmenityCategoryResult(
            row.id(),
            row.amenity(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl()),
            fileUrlResolver.resolve(row.inactiveIconUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ShopAmenityAssignmentResult withResolvedIconUrl(ShopAmenityAssignmentResult row) {
        return new ShopAmenityAssignmentResult(
            row.id(),
            row.amenityCategoryId(),
            row.amenity(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl())
        );
    }

    private ShopAmenityWithCategoryResult withResolvedIconUrl(ShopAmenityWithCategoryResult row) {
        return new ShopAmenityWithCategoryResult(
            row.amenity(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl())
        );
    }

    private ShopFoodTypeAssignmentResult withResolvedIconUrl(ShopFoodTypeAssignmentResult row) {
        return new ShopFoodTypeAssignmentResult(
            row.id(),
            row.foodTypeCategoryId(),
            row.foodType(),
            row.displayName(),
            fileUrlResolver.resolve(row.activeIconUrl())
        );
    }

    private ShopContentBoardResult withResolvedImageUrl(ShopContentBoardResult row) {
        return new ShopContentBoardResult(
            row.id(),
            row.shopId(),
            row.contentType(),
            row.topic(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.youtubeUrl(),
            row.description(),
            row.hidden(),
            row.createdAt()
        );
    }

    private ShopImageChangeRequestResult withResolvedImageUrl(ShopImageChangeRequestResult row) {
        return new ShopImageChangeRequestResult(
            row.id(),
            row.shopId(),
            row.imageType(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.status(),
            row.rejectReason()
        );
    }

    private ShopBannerImageResult withResolvedImageUrl(ShopBannerImageResult row) {
        return new ShopBannerImageResult(
            row.id(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort()
        );
    }

    private ShopPhotoCategoryImageResult withResolvedImageUrl(ShopPhotoCategoryImageResult row) {
        return new ShopPhotoCategoryImageResult(
            row.id(),
            row.shopPhotoCategoryId(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort()
        );
    }

    private ShopPhotoCategoryImageManagementResult withResolvedImageUrl(ShopPhotoCategoryImageManagementResult row) {
        return new ShopPhotoCategoryImageManagementResult(
            row.id(),
            row.shopPhotoCategoryId(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.sort(),
            row.visible()
        );
    }

    private ShopImageUrlsResult withResolvedImageUrls(ShopImageUrlsResult row) {
        return new ShopImageUrlsResult(
            row.shopId(),
            fileUrlResolver.resolve(row.thumbnailImageUrl()),
            fileUrlResolver.resolve(row.trademarkImageUrl())
        );
    }
}

package com.tastyhouse.infrastructure.review.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.review.model.ReviewListTab;
import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;
import com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity;
import com.tastyhouse.infrastructure.review.persistence.QReviewLikeJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderJpaEntity.orderJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewBlindRequestJpaEntity.reviewBlindRequestJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewImageJpaEntity.reviewImageJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewJpaEntity.reviewJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewOwnerReplyJpaEntity.reviewOwnerReplyJpaEntity;
import static com.tastyhouse.infrastructure.review.persistence.QReviewTagJpaEntity.reviewTagJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QTagJpaEntity.tagJpaEntity;

/**
 * 점주 리뷰 관리(ceo) 전용 read 어댑터(CQRS query 측).
 *
 * <p>기존 {@code ReviewQueryDao}(web 소비)와 분리한다 — 조회 용도가 다르다. 가장 큰 차이는
 * <b>{@code hidden} 필터를 끄지 않는다</b>는 점이다: 점주는 차단 탭에서 숨겨진 리뷰를 봐야 하므로
 * web 목록({@code hidden = false} 고정)과 같은 쿼리를 쓸 수 없다.
 *
 * <p><b>다건 컬렉션은 본 쿼리에 join하지 않는다.</b> 리뷰 사진·주문 메뉴명은 리뷰당 다건이라 join하면
 * 행이 불어나 페이징 카운트가 어긋난다. 페이지 하나를 먼저 뽑고 그 ID 집합으로 별도 조회한 뒤 위더로
 * 채운다(N+1이 아니라 페이지당 고정 2회).
 *
 * <p>날짜 필터는 <b>반열림 구간</b> {@code [startDate 00:00, endDate+1일 00:00)}으로 만든다 —
 * {@code DATE(created_at)}처럼 컬럼에 함수를 씌우면 {@code idx_review_shop_id_created_at}를 타지 못한다.
 *
 * <p>동적 조건은 {@code BooleanExpression} 헬퍼 + varargs {@code .where(...)}로 조립한다
 * ({@code BooleanBuilder} 금지 — 프로젝트 공통 규약).
 */
@Repository
public class ShopReviewManagementQueryDao {

    /** 정렬용 좋아요 별칭 — 존재 판정 서브쿼리와 같은 테이블이라 별칭을 분리한다. */
    private static final QReviewLikeJpaEntity sortReviewLike = new QReviewLikeJpaEntity("sortReviewLike");

    /** 사진 존재 판정 서브쿼리 별칭 — 목록 join(있으면 행이 불어남) 대신 EXISTS로만 쓴다. */
    private static final QReviewImageJpaEntity subReviewImage = new QReviewImageJpaEntity("subReviewImage");

    /** 최근 게시중단 요청 판정 서브쿼리 별칭. */
    private static final QReviewBlindRequestJpaEntity subBlindRequest =
        new QReviewBlindRequestJpaEntity("subBlindRequest");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopReviewManagementQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 점주 리뷰 목록을 페이징 조회한다.
     *
     * <p>사장님 답변은 {@code UNIQUE(review_id)}라 left join이 행을 늘리지 않으므로 본 쿼리에서 함께
     * 투영한다(미답변 탭 판정도 이 join의 {@code null} 여부로 한다).
     */
    public PageResult<ShopReviewManagementListItemResult> findShopReviews(
        ShopReviewManagementSearchCondition condition,
        PageQuery pageQuery
    ) {
        BooleanExpression[] predicates = {
            reviewJpaEntity.shopId.eq(condition.shopId()),
            tabPredicate(condition.tab()),
            createdAtGoe(condition.startDate()),
            createdAtLt(condition.endDate()),
            ratingEq(condition.rating()),
            orderMethodEq(condition.orderMethod()),
            hasImageEq(condition.hasImage()),
        };

        Long total = queryFactory
            .select(reviewJpaEntity.count())
            .from(reviewJpaEntity)
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity)
            .on(orderJpaEntity.id.eq(reviewJpaEntity.orderId))
            .where(predicates)
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        JPAQuery<ShopReviewManagementListItemResult> query = queryFactory
            .select(Projections.constructor(ShopReviewManagementListItemResult.class,
                reviewJpaEntity.id,
                memberJpaEntity.nickname,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                Expressions.constant(List.<String>of()),
                Expressions.constant(List.<String>of()),
                orderJpaEntity.orderMethod,
                reviewJpaEntity.hidden,
                reviewJpaEntity.ownerOnly,
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt,
                latestBlindRequestStatus(),
                reviewJpaEntity.createdAt
            ))
            .from(reviewJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(memberJpaEntity.id.eq(reviewJpaEntity.memberId))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity)
            .on(orderJpaEntity.id.eq(reviewJpaEntity.orderId))
            .where(predicates);

        applySort(query, condition.sortType());

        List<ShopReviewManagementListItemResult> content = query
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(withCollections(content), total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 점주 리뷰 상세. 목록과 달리 항목별 평점·태그·게시중단 요청 이력까지 담는다.
     */
    public Optional<ShopReviewManagementDetailResult> findShopReviewDetail(ReviewId reviewId) {
        Long id = reviewId.value();

        ShopReviewManagementDetailResult detail = queryFactory
            .select(Projections.constructor(ShopReviewManagementDetailResult.class,
                reviewJpaEntity.id,
                reviewJpaEntity.shopId,
                memberJpaEntity.nickname,
                reviewJpaEntity.totalRating,
                reviewJpaEntity.content,
                Expressions.constant(List.<String>of()),
                Expressions.constant(List.<String>of()),
                orderJpaEntity.orderMethod,
                reviewJpaEntity.hidden,
                reviewJpaEntity.ownerOnly,
                reviewJpaEntity.tasteRating,
                reviewJpaEntity.amountRating,
                reviewJpaEntity.priceRating,
                reviewJpaEntity.atmosphereRating,
                reviewJpaEntity.kindnessRating,
                reviewJpaEntity.hygieneRating,
                reviewJpaEntity.willRevisit,
                Expressions.constant(List.<String>of()),
                reviewOwnerReplyJpaEntity.id,
                reviewOwnerReplyJpaEntity.content,
                reviewOwnerReplyJpaEntity.createdAt,
                reviewOwnerReplyJpaEntity.updatedAt,
                Expressions.constant(List.<ReviewBlindRequestHistoryResult>of()),
                reviewJpaEntity.createdAt,
                reviewJpaEntity.deliveryRating,
                reviewJpaEntity.deliveryComment
            ))
            .from(reviewJpaEntity)
            .leftJoin(memberJpaEntity)
            .on(memberJpaEntity.id.eq(reviewJpaEntity.memberId))
            .leftJoin(reviewOwnerReplyJpaEntity)
            .on(reviewOwnerReplyJpaEntity.reviewId.eq(reviewJpaEntity.id))
            .leftJoin(orderJpaEntity)
            .on(orderJpaEntity.id.eq(reviewJpaEntity.orderId))
            .where(reviewJpaEntity.id.eq(id))
            .fetchOne();

        if (detail == null) {
            return Optional.empty();
        }

        return Optional.of(detail.withCollections(
            findImageUrls(id),
            findProductNames(List.of(id)).getOrDefault(id, List.of()),
            findTagNames(id),
            findBlindRequestHistory(reviewId)
        ));
    }

    /**
     * 리뷰 1건의 게시중단 요청 이력 — 최신순. 취소·반려된 과거 요청도 남긴다(재요청 판단 근거).
     */
    public List<ReviewBlindRequestHistoryResult> findBlindRequestHistory(ReviewId reviewId) {
        return queryFactory
            .select(Projections.constructor(ReviewBlindRequestHistoryResult.class,
                reviewBlindRequestJpaEntity.id,
                reviewBlindRequestJpaEntity.reason,
                reviewBlindRequestJpaEntity.detailReason,
                reviewBlindRequestJpaEntity.status,
                reviewBlindRequestJpaEntity.rejectReason,
                reviewBlindRequestJpaEntity.createdAt
            ))
            .from(reviewBlindRequestJpaEntity)
            .where(reviewBlindRequestJpaEntity.reviewId.eq(reviewId.value()))
            .orderBy(reviewBlindRequestJpaEntity.createdAt.desc(), reviewBlindRequestJpaEntity.id.desc())
            .fetch();
    }

    /**
     * 목록 한 페이지의 사진 URL·주문 메뉴명을 채운다. 페이지 단위로 2회만 조회한다.
     */
    private List<ShopReviewManagementListItemResult> withCollections(
        List<ShopReviewManagementListItemResult> content
    ) {
        if (content.isEmpty()) {
            return content;
        }

        List<Long> reviewIds = content.stream().map(ShopReviewManagementListItemResult::id).toList();
        Map<Long, List<String>> imageUrls = findImageUrlsByReviewIds(reviewIds);
        Map<Long, List<String>> productNames = findProductNames(reviewIds);

        return content.stream()
            .map(row -> row
                .withImageUrls(imageUrls.getOrDefault(row.id(), List.of()))
                .withProductNames(productNames.getOrDefault(row.id(), List.of())))
            .toList();
    }

    /**
     * 여러 리뷰의 사진 URL을 리뷰 ID별로 묶어 조회한다(정렬값 오름차순). 저장 경로는
     * {@link FileUrlResolver}로 표시용 URL까지 완성해 돌려준다 — Result에 {@code ~FileId}를 담지 않는다.
     */
    private Map<Long, List<String>> findImageUrlsByReviewIds(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewImageJpaEntity.reviewId, uploadedFileJpaEntity.filePath)
            .from(reviewImageJpaEntity)
            .innerJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(reviewImageJpaEntity.imageFileId))
            .where(reviewImageJpaEntity.reviewId.in(reviewIds))
            .orderBy(reviewImageJpaEntity.sort.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewImageJpaEntity.reviewId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(reviewImageJpaEntity.reviewId)),
                Collectors.mapping(
                    tuple -> fileUrlResolver.resolve(Objects.toString(tuple.get(uploadedFileJpaEntity.filePath), "")),
                    Collectors.toList()
                )
            ));
    }

    private List<String> findImageUrls(Long reviewId) {
        return findImageUrlsByReviewIds(List.of(reviewId)).getOrDefault(reviewId, List.of());
    }

    /**
     * 리뷰의 주문 메뉴명을 리뷰 ID별로 묶어 조회한다.
     *
     * <p>{@code REVIEW}는 {@code order_id}와 {@code product_id}를 함께 갖지만 <b>주문 전체의 메뉴</b>를
     * 보여준다(원문 ⑤) — 리뷰 대상 상품 하나만 보여주면 "무엇을 먹고 쓴 리뷰인지"를 알 수 없다.
     * {@code order_id}가 {@code NULL}인 미인증 리뷰는 결과에 들어오지 않아 빈 목록이 된다.
     */
    private Map<Long, List<String>> findProductNames(List<Long> reviewIds) {
        List<Tuple> results = queryFactory
            .select(reviewJpaEntity.id, orderProductJpaEntity.name)
            .from(reviewJpaEntity)
            .innerJoin(orderProductJpaEntity)
            .on(orderProductJpaEntity.orderId.eq(reviewJpaEntity.orderId))
            .where(reviewJpaEntity.id.in(reviewIds))
            .orderBy(orderProductJpaEntity.id.asc())
            .fetch();

        return results.stream()
            .filter(tuple -> tuple.get(reviewJpaEntity.id) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(reviewJpaEntity.id)),
                Collectors.mapping(
                    tuple -> Objects.toString(tuple.get(orderProductJpaEntity.name), ""),
                    Collectors.toList()
                )
            ));
    }

    /**
     * 리뷰에 달린 태그명 목록.
     */
    private List<String> findTagNames(Long reviewId) {
        return queryFactory
            .select(tagJpaEntity.tagName)
            .from(reviewTagJpaEntity)
            .innerJoin(tagJpaEntity).on(tagJpaEntity.id.eq(reviewTagJpaEntity.tagId))
            .where(reviewTagJpaEntity.reviewId.eq(reviewId))
            .fetch();
    }

    /**
     * 정렬 적용. 기존 {@code ReviewQueryDao#applySort}와 같은 정책이다(추천순은 좋아요 desc, 동수는 최신순).
     *
     * <p>추천순은 좋아요 수 집계가 필요해 {@code group by}가 붙는데, 투영에 든 모든 비집계 컬럼을 함께
     * 묶어야 한다 — MySQL의 {@code ONLY_FULL_GROUP_BY}에서 하나라도 빠지면 쿼리가 거부된다.
     */
    private void applySort(JPAQuery<ShopReviewManagementListItemResult> query, ReviewSortType sortType) {
        switch (sortType) {
            case RECOMMENDED -> query
                .leftJoin(sortReviewLike).on(sortReviewLike.reviewId.eq(reviewJpaEntity.id))
                .groupBy(
                    reviewJpaEntity.id,
                    memberJpaEntity.nickname,
                    reviewJpaEntity.totalRating,
                    reviewJpaEntity.content,
                    orderJpaEntity.orderMethod,
                    reviewJpaEntity.hidden,
                    reviewJpaEntity.ownerOnly,
                    reviewOwnerReplyJpaEntity.content,
                    reviewOwnerReplyJpaEntity.createdAt,
                    reviewJpaEntity.createdAt
                )
                .orderBy(sortReviewLike.count().desc(), reviewJpaEntity.createdAt.desc());
            case OLDEST -> query.orderBy(reviewJpaEntity.createdAt.asc(), reviewJpaEntity.id.asc());
            case LATEST -> query.orderBy(reviewJpaEntity.createdAt.desc(), reviewJpaEntity.id.desc());
        }
    }

    /**
     * 가장 최근 게시중단 요청의 상태(없으면 {@code null}).
     *
     * <p>상관 서브쿼리 2단({@code id = (select max(id) ...)})으로 최신 1건을 특정한다 — 목록에
     * {@code REVIEW_BLIND_REQUEST}를 직접 join하면 요청을 여러 번 낸 리뷰에서 행이 불어난다.
     */
    private Expression<ApprovalStatus> latestBlindRequestStatus() {
        return JPAExpressions
            .select(subBlindRequest.status)
            .from(subBlindRequest)
            .where(subBlindRequest.id.eq(
                JPAExpressions
                    .select(reviewBlindRequestJpaEntity.id.max())
                    .from(reviewBlindRequestJpaEntity)
                    .where(reviewBlindRequestJpaEntity.reviewId.eq(reviewJpaEntity.id))
            ));
    }

    /**
     * 탭 조건. {@code ALL}은 조건 없음이며, <b>어느 탭에서도 {@code hidden}을 강제로 끄지 않는다</b>.
     *
     * <p>{@code UNANSWERED}는 목록에 이미 left join된 사장님 답변이 없는 행이다. {@code BLINDED}는
     * "게시중단 요청이 승인된 것"이 아니라 <b>리뷰가 실제로 숨겨진 것</b>을 기준으로 한다 — 관리자가
     * 요청 없이 직접 숨긴 리뷰도 점주에게는 차단된 리뷰이기 때문이다.
     *
     * <p>{@code OWNER_ONLY}는 작성자가 비공개로 등록한 리뷰다. {@code BLINDED}와 <b>직교</b>하므로 한
     * 리뷰가 두 탭에 동시에 나타날 수 있다(비공개 리뷰가 정책 위반이라 게시중단된 경우).
     */
    private BooleanExpression tabPredicate(ReviewListTab tab) {
        if (tab == null) {
            return null;
        }
        return switch (tab) {
            case ALL -> null;
            case UNANSWERED -> reviewOwnerReplyJpaEntity.id.isNull();
            case BLINDED -> reviewJpaEntity.hidden.isTrue();
            case OWNER_ONLY -> reviewJpaEntity.ownerOnly.isTrue();
        };
    }

    private BooleanExpression createdAtGoe(LocalDate startDate) {
        return startDate != null ? reviewJpaEntity.createdAt.goe(startDate.atStartOfDay()) : null;
    }

    /**
     * 종료일 필터는 <b>다음날 00:00 미만</b>으로 만든다 — {@code loe(endDate.atStartOfDay())}로 쓰면
     * 종료일 당일에 작성된 리뷰가 통째로 빠진다.
     */
    private BooleanExpression createdAtLt(LocalDate endDate) {
        if (endDate == null) {
            return null;
        }
        LocalDateTime until = endDate.plusDays(1).atStartOfDay();
        return reviewJpaEntity.createdAt.lt(until);
    }

    /**
     * 별점 필터는 <b>내림 정수</b> 기준이다 — 4점 필터가 4.0~4.9를 포함해야 별점 분포 통계
     * ({@code getRatingCounts}의 {@code floor})와 같은 집합을 가리킨다.
     */
    private BooleanExpression ratingEq(Integer rating) {
        return rating != null ? reviewJpaEntity.totalRating.floor().intValue().eq(rating) : null;
    }

    private BooleanExpression orderMethodEq(OrderMethod orderMethod) {
        return orderMethod != null ? orderJpaEntity.orderMethod.eq(orderMethod) : null;
    }

    /**
     * 사진 유무 필터. join이 아니라 {@code EXISTS}로 판정해 행이 불어나지 않게 한다.
     *
     * <p>{@code false}는 "필터 무시"가 아니라 <b>사진 없는 리뷰만</b>이다({@code NOT EXISTS}) — 미지정
     * ({@code null})이 전체를 뜻하므로 {@code false}에 같은 의미를 주면 값 하나가 낭비된다. web 목록의
     * {@code hasImage}와 같은 해석이다.
     */
    private BooleanExpression hasImageEq(Boolean hasImage) {
        if (hasImage == null) {
            return null;
        }
        BooleanExpression exists = JPAExpressions
            .selectOne()
            .from(subReviewImage)
            .where(subReviewImage.reviewId.eq(reviewJpaEntity.id))
            .exists();
        return hasImage ? exists : exists.not();
    }
}

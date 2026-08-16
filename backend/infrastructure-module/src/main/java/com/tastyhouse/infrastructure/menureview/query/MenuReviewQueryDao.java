package com.tastyhouse.infrastructure.menureview.query;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;
import static com.tastyhouse.infrastructure.menureview.persistence.QMenuReviewJpaEntity.menuReviewJpaEntity;
import static com.tastyhouse.infrastructure.order.persistence.QOrderProductJpaEntity.orderProductJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;

/**
 * 메뉴 평가 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code MenuReviewRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api)의
 * {@code MenuReviewQueryService}가 이 DAO를 주입해 쓰며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>집계(상품 평점 재집계·기간 집계)는 용도가 달라 {@link MenuReviewStatisticsQueryDao}가 담당한다.
 *
 * <p>파일 join으로 얻은 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다 —
 * {@code @QueryProjection}은 생성자 직접 투영이라 변환을 투영식에 끼울 수 없어 fetch 직후 재조립한다.
 */
@Repository
public class MenuReviewQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public MenuReviewQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 한 주문의 평가 가능 메뉴 목록.
     *
     * <p><b>{@code PRODUCT.is_rating_excluded = 0}인 항목만 담는다</b>(주류·사이드 제외) — 판정은 서버가
     * 하며 프론트가 카테고리 이름으로 거르지 않는다.
     *
     * <p>기존 평가를 left join해 작성 여부와 기존 값을 함께 내려준다. 상품 이미지는 주문 시점 스냅샷
     * ({@code ORDER_PRODUCT.image_file_id})을 쓴다 — 이후 상품 이미지가 바뀌어도 주문 당시 본 메뉴를
     * 그대로 보여주기 위함이다.
     */
    public List<MenuReviewWritableItemResult> findWritableItemsByOrderId(Long orderId) {
        return queryFactory
            .select(new QMenuReviewWritableItemResult(
                orderProductJpaEntity.id,
                orderProductJpaEntity.productId,
                orderProductJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                menuReviewJpaEntity.id,
                menuReviewJpaEntity.rating,
                menuReviewJpaEntity.comment
            ))
            .from(orderProductJpaEntity)
            .join(productJpaEntity).on(productJpaEntity.id.eq(orderProductJpaEntity.productId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(orderProductJpaEntity.imageFileId))
            .leftJoin(menuReviewJpaEntity).on(menuReviewJpaEntity.orderProductId.eq(orderProductJpaEntity.id))
            .where(
                orderProductJpaEntity.orderId.eq(orderId),
                productJpaEntity.ratingExcluded.isFalse()
            )
            .orderBy(orderProductJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(this::withResolvedProductImageUrl)
            .toList();
    }

    /**
     * 상품별 메뉴 평가 목록(고객 공개 조회) — 숨김 제외, 최신순.
     */
    public PageResult<MenuReviewListItemResult> findVisibleByProductId(Long productId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(menuReviewJpaEntity.count())
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .fetchOne();

        List<MenuReviewListItemResult> content = queryFactory
            .select(new QMenuReviewListItemResult(
                menuReviewJpaEntity.id,
                memberJpaEntity.nickname,
                uploadedFileJpaEntity.filePath,
                menuReviewJpaEntity.rating,
                menuReviewJpaEntity.comment,
                menuReviewJpaEntity.createdAt
            ))
            .from(menuReviewJpaEntity)
            .leftJoin(memberJpaEntity).on(memberJpaEntity.id.eq(menuReviewJpaEntity.memberId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(memberJpaEntity.profileImageFileId))
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .orderBy(menuReviewJpaEntity.createdAt.desc(), menuReviewJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedMemberProfileImageUrl)
            .toList();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private MenuReviewWritableItemResult withResolvedProductImageUrl(MenuReviewWritableItemResult row) {
        return row.withProductImageUrl(fileUrlResolver.resolve(row.productImageUrl()));
    }

    private MenuReviewListItemResult withResolvedMemberProfileImageUrl(MenuReviewListItemResult row) {
        return row.withMemberProfileImageUrl(fileUrlResolver.resolve(row.memberProfileImageUrl()));
    }
}

package com.tastyhouse.infrastructure.product.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductPriceJpaEntity.productPriceJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QStorePriceVerificationItemJpaEntity.storePriceVerificationItemJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QStorePriceVerificationJpaEntity.storePriceVerificationJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

/**
 * 매장 가격 인증 요청 검수 화면 전용 read 어댑터.
 *
 * <p><b>왜 {@code ProductQueryDao}에 메서드를 더하지 않고 DAO를 새로 두는가</b>: {@code ProductQueryDao}는
 * 이미 2000줄이 넘고 메뉴·옵션·카테고리·승인요청 3종을 한 클래스가 떠맡고 있다. 인증 요청은 자체
 * 테이블 2개({@code SHOP_STORE_PRICE_VERIFICATION}·{@code ..._ITEM})와 자체 조인 그래프를 갖는 독립
 * 조회 대상이라, 별 파일로 두면 그 그래프가 한눈에 보인다({@code ShopDeliveryAreaAdjustmentQueryDao}가
 * 같은 이유로 분리돼 있다).
 *
 * <p>쓰기 포트 {@code StorePriceVerificationRepository}와 역할이 갈린다 — 그 포트는 불변식 검증용
 * 조회만 갖고, 표현 목적 투영(가게명 조인·항목 수 집계·파일 URL 완성)은 전부 이 DAO가 담당한다.
 */
@Repository
public class StorePriceVerificationQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public StorePriceVerificationQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 관리자 검수용 인증 요청 페이징 목록 — 상태로 필터하며 최근 요청 순.
     *
     * <p>{@code status}가 {@code null}이면 전체를 조회한다(상태 미지정 = "전체").
     */
    public PageResult<StorePriceVerificationListItemResult> findVerificationPage(
        StorePriceVerificationStatus status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(storePriceVerificationJpaEntity.count())
            .from(storePriceVerificationJpaEntity)
            .where(statusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<StorePriceVerificationListItemResult> content = verificationProjection()
            .where(statusEq(status))
            .orderBy(storePriceVerificationJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedFileUrl)
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    /**
     * 인증 요청 단건 — 상세 화면 헤더(가게·가격표 이미지·상태)용이다. 항목은
     * {@link #findVerificationItems(Long)}가 따로 조회한다.
     */
    public Optional<StorePriceVerificationListItemResult> findVerificationById(Long verificationId) {
        return Optional.ofNullable(
                verificationProjection()
                    .where(storePriceVerificationJpaEntity.id.eq(verificationId))
                    .fetchOne())
            .map(this::withResolvedFileUrl);
    }

    /**
     * 인증 요청의 대상 항목 목록 — 검수자가 앱 노출가({@code deliveryPrice})와 신고된
     * 매장가({@code storePrice})를 나란히 대조하는 화면의 본체다.
     *
     * <p><b>항목을 목록 조회에 합치지 않고 별 쿼리로 두는 이유</b>는 카디널리티다. 요청 1건에 메뉴가
     * N건 달리므로 목록에 조인하면 행이 부풀어 페이징이 깨진다 — 목록은 {@code itemCount} 집계만 담고
     * 항목 자체는 상세에서 가져온다.
     */
    public List<StorePriceVerificationItemResult> findVerificationItems(Long verificationId) {
        return queryFactory
            .select(Projections.constructor(StorePriceVerificationItemResult.class,
                storePriceVerificationItemJpaEntity.productId,
                productJpaEntity.name,
                storePriceVerificationItemJpaEntity.productPriceId,
                productPriceJpaEntity.priceName,
                storePriceVerificationItemJpaEntity.storePrice,
                productPriceJpaEntity.deliveryPrice,
                storePriceVerificationItemJpaEntity.applyPickupSamePrice
            ))
            .from(storePriceVerificationItemJpaEntity)
            .leftJoin(productJpaEntity)
            .on(productJpaEntity.id.eq(storePriceVerificationItemJpaEntity.productId))
            .leftJoin(productPriceJpaEntity)
            .on(productPriceJpaEntity.id.eq(storePriceVerificationItemJpaEntity.productPriceId))
            .where(storePriceVerificationItemJpaEntity.verificationId.eq(verificationId))
            .orderBy(storePriceVerificationItemJpaEntity.id.asc())
            .fetch();
    }

    /**
     * 목록·상세가 공유하는 투영. 따로 두면 한쪽만 고쳐져 같은 요청이 화면마다 다른 필드를 갖게 된다.
     *
     * <p>{@code itemCount}는 <b>스칼라 서브쿼리</b>로 센다 — 항목 테이블을 조인해 {@code GROUP BY}로
     * 세면 페이징 대상 행이 부풀고, 항목이 0건인 요청이 조인에서 탈락한다.
     *
     * <p>가격표 이미지는 {@code left join}이다. 컬럼이 {@code NOT NULL}이라 정상 데이터라면 항상 맞지만,
     * inner join으로 두면 파일 행이 유실된 요청이 <b>검수 목록에서 조용히 사라져</b> 처리 불가 상태가 된다.
     */
    private com.querydsl.jpa.JPQLQuery<StorePriceVerificationListItemResult> verificationProjection() {
        return queryFactory
            .select(Projections.constructor(StorePriceVerificationListItemResult.class,
                storePriceVerificationJpaEntity.id,
                storePriceVerificationJpaEntity.shopId,
                shopJpaEntity.name,
                storePriceVerificationJpaEntity.status,
                uploadedFileJpaEntity.filePath,
                storePriceVerificationJpaEntity.rejectReason,
                JPAExpressions
                    .select(storePriceVerificationItemJpaEntity.count())
                    .from(storePriceVerificationItemJpaEntity)
                    .where(storePriceVerificationItemJpaEntity.verificationId
                        .eq(storePriceVerificationJpaEntity.id)),
                storePriceVerificationJpaEntity.createdAt,
                storePriceVerificationJpaEntity.processedAt
            ))
            .from(storePriceVerificationJpaEntity)
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(storePriceVerificationJpaEntity.shopId))
            .leftJoin(uploadedFileJpaEntity)
            .on(uploadedFileJpaEntity.id.eq(storePriceVerificationJpaEntity.priceListFileId));
    }

    private BooleanExpression statusEq(StorePriceVerificationStatus status) {
        return status != null ? storePriceVerificationJpaEntity.status.eq(status) : null;
    }

    private StorePriceVerificationListItemResult withResolvedFileUrl(StorePriceVerificationListItemResult row) {
        return new StorePriceVerificationListItemResult(
            row.id(),
            row.shopId(),
            row.shopName(),
            row.status(),
            fileUrlResolver.resolve(row.priceListFileUrl()),
            row.rejectReason(),
            row.itemCount(),
            row.requestedAt(),
            row.processedAt()
        );
    }
}

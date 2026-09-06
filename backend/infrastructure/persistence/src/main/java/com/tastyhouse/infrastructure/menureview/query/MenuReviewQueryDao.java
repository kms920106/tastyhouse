package com.tastyhouse.infrastructure.menureview.query;

import com.tastyhouse.application.menureview.port.out.MenuReviewQueryPort;
import com.tastyhouse.application.menureview.port.out.MenuReviewListItemResult;
import com.tastyhouse.application.menureview.port.out.MenuReviewWritableItemResult;
import com.querydsl.core.types.Projections;
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

@Repository
public class MenuReviewQueryDao implements MenuReviewQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public MenuReviewQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public List<MenuReviewWritableItemResult> findWritableItemsByOrderId(Long orderId) {
        return queryFactory
            .select(Projections.constructor(MenuReviewWritableItemResult.class,
                orderProductJpaEntity.id,
                orderProductJpaEntity.productId,
                orderProductJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                menuReviewJpaEntity.id,
                menuReviewJpaEntity.rating,
                menuReviewJpaEntity.comment
            ))
            .from(orderProductJpaEntity)
            .leftJoin(productJpaEntity).on(productJpaEntity.id.eq(orderProductJpaEntity.productId))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(orderProductJpaEntity.imageFileId))
            .leftJoin(menuReviewJpaEntity).on(menuReviewJpaEntity.orderProductId.eq(orderProductJpaEntity.id))
            .where(
                orderProductJpaEntity.orderId.eq(orderId),
                productJpaEntity.ratingExcluded.isNull().or(productJpaEntity.ratingExcluded.isFalse())
            )
            .orderBy(orderProductJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(this::withResolvedProductImageUrl)
            .toList();
    }

    @Override
    public PageResult<MenuReviewListItemResult> findVisibleByProductId(Long productId, PageQuery pageQuery) {
        Long total = queryFactory
            .select(menuReviewJpaEntity.count())
            .from(menuReviewJpaEntity)
            .where(menuReviewJpaEntity.productId.eq(productId), menuReviewJpaEntity.hidden.isFalse())
            .fetchOne();

        List<MenuReviewListItemResult> content = queryFactory
            .select(Projections.constructor(MenuReviewListItemResult.class,
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

package com.tastyhouse.infrastructure.jpa.product.query;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.product.port.out.ProductApprovalRequestManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductApprovalRequestOwnerQueryPort;
import com.tastyhouse.application.product.port.out.ProductImageChangeRequestResult;
import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;
import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.infrastructure.jpa.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageChangeRequestJpaEntity.productImageChangeRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductRepresentativeRequestJpaEntity.productRepresentativeRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.product.persistence.QProductVegetarianRequestJpaEntity.productVegetarianRequestJpaEntity;
import static com.tastyhouse.infrastructure.jpa.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
class ProductApprovalRequestQueryAdapter implements ProductApprovalRequestManagementQueryPort, ProductApprovalRequestOwnerQueryPort {

    private static final com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity
        imageChangeRequestFile =
        new com.tastyhouse.infrastructure.jpa.file.persistence.QUploadedFileJpaEntity("imageChangeRequestFile");

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ProductApprovalRequestQueryAdapter(
        JPAQueryFactory queryFactory,
        FileUrlResolver fileUrlResolver
    ) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public List<ProductImageChangeRequestResult> findImageChangeRequests(Long productId) {
        return imageChangeRequestProjection()
            .where(productImageChangeRequestJpaEntity.productId.eq(productId))
            .orderBy(productImageChangeRequestJpaEntity.id.desc())
            .fetch();
    }

    @Override
    public PageResult<ProductImageChangeRequestResult> findImageChangeRequestPage(
        String status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(productImageChangeRequestJpaEntity.count())
            .from(productImageChangeRequestJpaEntity)
            .where(imageChangeStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductImageChangeRequestResult> content = imageChangeRequestProjection()
            .where(imageChangeStatusEq(status))
            .orderBy(productImageChangeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public List<ProductVegetarianRequestResult> findVegetarianRequests(Long productId) {
        return vegetarianRequestProjection()
            .where(productVegetarianRequestJpaEntity.productId.eq(productId))
            .orderBy(productVegetarianRequestJpaEntity.id.desc())
            .fetch();
    }

    @Override
    public PageResult<ProductVegetarianRequestResult> findVegetarianRequestPage(
        String status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(productVegetarianRequestJpaEntity.count())
            .from(productVegetarianRequestJpaEntity)
            .where(vegetarianStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductVegetarianRequestResult> content = vegetarianRequestProjection()
            .where(vegetarianStatusEq(status))
            .orderBy(productVegetarianRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<ProductRepresentativeRequestResult> findRepresentativeRequestPage(
        String status,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(productRepresentativeRequestJpaEntity.count())
            .from(productRepresentativeRequestJpaEntity)
            .where(representativeStatusEq(status))
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ProductRepresentativeRequestResult> content = representativeRequestProjection()
            .where(representativeStatusEq(status))
            .orderBy(productRepresentativeRequestJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private com.querydsl.jpa.JPQLQuery<ProductImageChangeRequestResult> imageChangeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductImageChangeRequestResult.class,
                productImageChangeRequestJpaEntity.id,
                productImageChangeRequestJpaEntity.productId,
                productJpaEntity.shopId,
                productJpaEntity.name,
                fileUrlResolver.urlOf(imageChangeRequestFile.filePath),
                productImageChangeRequestJpaEntity.status.stringValue(),
                productImageChangeRequestJpaEntity.rejectReason
            ))
            .from(productImageChangeRequestJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productImageChangeRequestJpaEntity.productId))
            .leftJoin(imageChangeRequestFile)
            .on(imageChangeRequestFile.id.eq(productImageChangeRequestJpaEntity.imageFileId));
    }

    private com.querydsl.jpa.JPQLQuery<ProductVegetarianRequestResult> vegetarianRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductVegetarianRequestResult.class,
                productVegetarianRequestJpaEntity.id,
                productVegetarianRequestJpaEntity.productId,
                productJpaEntity.shopId,
                productJpaEntity.name,
                productVegetarianRequestJpaEntity.vegetarianType.stringValue(),
                productVegetarianRequestJpaEntity.ingredients,
                productVegetarianRequestJpaEntity.description,
                productVegetarianRequestJpaEntity.status.stringValue(),
                productVegetarianRequestJpaEntity.rejectReason
            ))
            .from(productVegetarianRequestJpaEntity)
            .innerJoin(productJpaEntity).on(productJpaEntity.id.eq(productVegetarianRequestJpaEntity.productId));
    }

    private BooleanExpression imageChangeStatusEq(String status) {
        return status != null ? productImageChangeRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression vegetarianStatusEq(String status) {
        return status != null ? productVegetarianRequestJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression representativeStatusEq(String status) {
        return status != null ? productRepresentativeRequestJpaEntity.status.eq(status) : null;
    }

    private com.querydsl.jpa.JPQLQuery<ProductRepresentativeRequestResult> representativeRequestProjection() {
        return queryFactory
            .select(Projections.constructor(ProductRepresentativeRequestResult.class,
                productRepresentativeRequestJpaEntity.id,
                productRepresentativeRequestJpaEntity.productId,
                productRepresentativeRequestJpaEntity.shopId,
                shopJpaEntity.name,
                productJpaEntity.name,
                fileUrlResolver.urlOf(uploadedFileJpaEntity.filePath),
                productRepresentativeRequestJpaEntity.status.stringValue(),
                productRepresentativeRequestJpaEntity.rejectReason
            ))
            .from(productRepresentativeRequestJpaEntity)
            .innerJoin(productJpaEntity)
            .on(productJpaEntity.id.eq(productRepresentativeRequestJpaEntity.productId))
            .leftJoin(shopJpaEntity).on(shopJpaEntity.id.eq(productRepresentativeRequestJpaEntity.shopId))
            .leftJoin(productImageJpaEntity).on(ProductQueryPredicates.representativeImageOf(productJpaEntity.id))
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id));
    }
}

package com.tastyhouse.infrastructure.shop.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.core.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.application.dto.result.QProductSimpleResult;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductImageJpaEntity.productImageJpaEntity;
import static com.tastyhouse.infrastructure.product.persistence.QProductJpaEntity.productJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopChoiceJpaEntity.shopChoiceJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;

@Repository
@RequiredArgsConstructor
public class ShopChoiceRepositoryImpl implements ShopChoiceRepository {

    private static final QProductImageJpaEntity subProductImage = new QProductImageJpaEntity("subProductImage");

    private final JPAQueryFactory queryFactory;
    private final ShopChoiceJpaRepository shopChoiceJpaRepository;

    @Override
    public PageResult<EditorChoiceResult> findEditorChoice(PageQuery pageQuery) {
        Long totalCount = queryFactory
            .select(shopChoiceJpaEntity.count())
            .from(shopChoiceJpaEntity)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Tuple> shopChoices = queryFactory
            .select(
                shopChoiceJpaEntity.id,
                shopChoiceJpaEntity.shopId,
                shopJpaEntity.name,
                shopChoiceJpaEntity.title,
                shopChoiceJpaEntity.content,
                uploadedFileJpaEntity.filePath
            )
            .from(shopChoiceJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(shopChoiceJpaEntity.shopId).and(shopJpaEntity.permanentlyClosed.eq(false)).and(shopJpaEntity.hidden.eq(false)))
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopJpaEntity.thumbnailImageFileId))
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<Long> shopIds = shopChoices.stream()
            .map(tuple -> tuple.get(shopChoiceJpaEntity.shopId))
            .distinct()
            .toList();

        List<Tuple> productTuples = queryFactory
            .select(
                productJpaEntity.shopId,
                new QProductSimpleResult(
                    productJpaEntity.id,
                    shopJpaEntity.name,
                    productJpaEntity.name,
                    uploadedFileJpaEntity.filePath,
                    productJpaEntity.originalPrice,
                    productJpaEntity.discountInfo.discountPrice,
                    productJpaEntity.discountInfo.discountRate
                )
            )
            .from(productJpaEntity)
            .innerJoin(shopJpaEntity).on(shopJpaEntity.id.eq(productJpaEntity.shopId))
            .leftJoin(productImageJpaEntity).on(
                productImageJpaEntity.productId.eq(productJpaEntity.id)
                    .and(productImageJpaEntity.visible.eq(true))
                    .and(productImageJpaEntity.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(productJpaEntity.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFileJpaEntity).on(productImageJpaEntity.imageFileId.eq(uploadedFileJpaEntity.id))
            .where(productJpaEntity.shopId.in(shopIds))
            .fetch();

        Map<Long, List<ProductSimpleResult>> productsByShopId = productTuples.stream()
            .filter(tuple -> tuple.get(productJpaEntity.shopId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(productJpaEntity.shopId)),
                Collectors.mapping(
                    tuple -> tuple.get(1, ProductSimpleResult.class),
                    Collectors.toList()
                )
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().limit(2).toList()
            ));

        List<EditorChoiceResult> content = shopChoices.stream()
            .map(tuple -> {
                Long shopIdValue = tuple.get(shopChoiceJpaEntity.shopId);
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceResult(
                    tuple.get(shopChoiceJpaEntity.id),
                    shopIdValue,
                    tuple.get(shopJpaEntity.name),
                    tuple.get(shopChoiceJpaEntity.title),
                    tuple.get(shopChoiceJpaEntity.content),
                    tuple.get(uploadedFileJpaEntity.filePath),
                    products
                );
            })
            .toList();

        return PageResult.of(content, totalCount, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopChoice> findById(Long id) {
        return shopChoiceJpaRepository.findById(id).map(ShopChoiceMapper::toDomain);
    }

    @Override
    public ShopChoice save(ShopChoice shopChoice) {
        if (shopChoice.getId() == null) {
            ShopChoiceJpaEntity saved = shopChoiceJpaRepository.save(ShopChoiceMapper.toEntity(shopChoice));
            return ShopChoiceMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush.
        ShopChoiceJpaEntity entity = shopChoiceJpaRepository.findById(shopChoice.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 에디터 초이스입니다: " + shopChoice.getId()));
        ShopChoiceMapper.applyChanges(entity, shopChoice);
        return ShopChoiceMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        shopChoiceJpaRepository.deleteById(id);
    }
}

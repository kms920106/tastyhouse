package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.product.domain.model.QProductImage;
import com.tastyhouse.core.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.core.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.application.dto.result.QProductSimpleResult;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.product.domain.model.QProduct.product;
import static com.tastyhouse.core.domain.product.domain.model.QProductImage.productImage;
import static com.tastyhouse.core.domain.shop.domain.model.QShopChoice.shopChoice;

/**
 * {@code shop}은 infrastructure-module로 이동한 {@code ShopJpaEntity}를 가리킨다.
 * core-module은 infrastructure-module을 의존할 수 없어(의존 방향: infrastructure → core)
 * 생성된 Q타입을 import할 수 없으므로, {@link PathBuilder}로 JPA 엔티티명("ShopJpaEntity")을
 * 문자열 참조해 필요한 컬럼만 타입 세이프하게 노출한다.
 */
@Repository
@RequiredArgsConstructor
public class ShopChoiceRepositoryImpl implements ShopChoiceRepository {

    private static final QProductImage subProductImage = new QProductImage("subProductImage");

    private static final PathBuilder<Object> shop = new PathBuilder<>(Object.class, "ShopJpaEntity");
    private static final NumberPath<Long> shopIdCol = shop.getNumber("id", Long.class);
    private static final StringPath shopNameCol = shop.getString("name");
    private static final NumberPath<Long> shopThumbnailImageFileIdCol = shop.getNumber("thumbnailImageFileId", Long.class);
    private static final BooleanPath shopPermanentlyClosedCol = shop.getBoolean("permanentlyClosed");

    private final JPAQueryFactory queryFactory;
    private final ShopChoiceJpaRepository shopChoiceJpaRepository;

    @Override
    public PageResult<EditorChoiceResult> findEditorChoice(PageQuery pageQuery) {
        Long totalCount = queryFactory
            .select(shopChoice.count())
            .from(shopChoice)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<Tuple> shopChoices = queryFactory
            .select(
                shopChoice.id,
                shopChoice.shopId,
                shopNameCol,
                shopChoice.title,
                shopChoice.content,
                uploadedFile.filePath
            )
            .from(shopChoice)
            .innerJoin(shop).on(shopIdCol.eq(shopChoice.shopId).and(shopPermanentlyClosedCol.eq(false)))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shopThumbnailImageFileIdCol))
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        List<Long> shopIds = shopChoices.stream()
            .map(tuple -> tuple.get(shopChoice.shopId))
            .distinct()
            .toList();

        List<Tuple> productTuples = queryFactory
            .select(
                product.shopId,
                new QProductSimpleResult(
                    product.id,
                    shopNameCol,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountInfo.discountPrice,
                    product.discountInfo.discountRate
                )
            )
            .from(product)
            .innerJoin(shop).on(shopIdCol.eq(product.shopId))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.visible.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.visible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(product.shopId.in(shopIds))
            .fetch();

        Map<Long, List<ProductSimpleResult>> productsByShopId = productTuples.stream()
            .filter(tuple -> tuple.get(product.shopId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(product.shopId)),
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
                Long shopIdValue = tuple.get(shopChoice.shopId);
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceResult(
                    tuple.get(shopChoice.id),
                    shopIdValue,
                    tuple.get(shopNameCol),
                    tuple.get(shopChoice.title),
                    tuple.get(shopChoice.content),
                    tuple.get(uploadedFile.filePath),
                    products
                );
            })
            .toList();

        return PageResult.of(content, totalCount, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<ShopChoice> findById(Long id) {
        return shopChoiceJpaRepository.findById(id);
    }

    @Override
    public ShopChoice save(ShopChoice shopChoice) {
        return shopChoiceJpaRepository.save(shopChoice);
    }

    @Override
    public void deleteById(Long id) {
        shopChoiceJpaRepository.deleteById(id);
    }
}

package com.tastyhouse.core.domain.shop.infrastructure.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.domain.shop.domain.repository.ShopChoiceRepository;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.application.dto.result.QProductSimpleResult;
import com.tastyhouse.core.domain.product.domain.model.QProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.shop.domain.model.QShop.shop;
import static com.tastyhouse.core.domain.shop.domain.model.QShopChoice.shopChoice;
import static com.tastyhouse.core.domain.product.domain.model.QProduct.product;
import static com.tastyhouse.core.domain.product.domain.model.QProductImage.productImage;

@Repository
@RequiredArgsConstructor
public class ShopChoiceRepositoryImpl implements ShopChoiceRepository {

    private static final QProductImage subProductImage = new QProductImage("subProductImage");

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EditorChoiceDto> findEditorChoice() {
        List<Tuple> shopChoices = queryFactory
            .select(
                shopChoice.id,
                shopChoice.shopId,
                shop.name,
                shopChoice.title,
                shopChoice.content,
                uploadedFile.filePath
            )
            .from(shopChoice)
            .innerJoin(shop).on(shop.id.eq(shopChoice.shopId).and(shop.permanentlyClosed.eq(false)))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
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
                    shop.name,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountInfo.discountPrice,
                    product.discountInfo.discountRate
                )
            )
            .from(product)
            .innerJoin(shop).on(shop.id.eq(product.shopId))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.isVisible.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isVisible.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(productImage.imageFileId))
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

        return shopChoices.stream()
            .map(tuple -> {
                Long shopIdValue = tuple.get(shopChoice.shopId);
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceDto(
                    tuple.get(shopChoice.id),
                    shopIdValue,
                    tuple.get(shop.name),
                    tuple.get(shopChoice.title),
                    tuple.get(shopChoice.content),
                    tuple.get(uploadedFile.filePath),
                    products
                );
            })
            .toList();
    }

    @Override
    public Page<EditorChoiceDto> findEditorChoice(Pageable pageable) {
        Long totalCount = queryFactory
            .select(shopChoice.count())
            .from(shopChoice)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<Tuple> shopChoices = queryFactory
            .select(
                shopChoice.id,
                shopChoice.shopId,
                shop.name,
                shopChoice.title,
                shopChoice.content,
                uploadedFile.filePath
            )
            .from(shopChoice)
            .innerJoin(shop).on(shop.id.eq(shopChoice.shopId).and(shop.permanentlyClosed.eq(false)))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(shop.thumbnailImageFileId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
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
                    shop.name,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountInfo.discountPrice,
                    product.discountInfo.discountRate
                )
            )
            .from(product)
            .innerJoin(shop).on(shop.id.eq(product.shopId))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.isVisible.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isVisible.eq(true)))
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

        List<EditorChoiceDto> content = shopChoices.stream()
            .map(tuple -> {
                Long shopIdValue = tuple.get(shopChoice.shopId);
                List<ProductSimpleResult> products = productsByShopId.getOrDefault(shopIdValue, new ArrayList<>());
                return new EditorChoiceDto(
                    tuple.get(shopChoice.id),
                    shopIdValue,
                    tuple.get(shop.name),
                    tuple.get(shopChoice.title),
                    tuple.get(shopChoice.content),
                    tuple.get(uploadedFile.filePath),
                    products
                );
            })
            .toList();

        return new PageImpl<>(content, pageable, totalCount);
    }
}

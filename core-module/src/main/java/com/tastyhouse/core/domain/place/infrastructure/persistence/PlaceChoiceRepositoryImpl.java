package com.tastyhouse.core.domain.place.infrastructure.persistence;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.place.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.domain.place.domain.repository.PlaceChoiceRepository;
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
import static com.tastyhouse.core.domain.place.domain.model.QPlace.place;
import static com.tastyhouse.core.domain.place.domain.model.QPlaceChoice.placeChoice;
import static com.tastyhouse.core.domain.product.domain.model.QProduct.product;
import static com.tastyhouse.core.domain.product.domain.model.QProductImage.productImage;

@Repository
@RequiredArgsConstructor
public class PlaceChoiceRepositoryImpl implements PlaceChoiceRepository {

    private static final QProductImage subProductImage = new QProductImage("subProductImage");

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EditorChoiceDto> findEditorChoice() {
        List<Tuple> placeChoices = queryFactory
            .select(
                placeChoice.id,
                placeChoice.placeId,
                place.name,
                placeChoice.title,
                placeChoice.content,
                uploadedFile.filePath
            )
            .from(placeChoice)
            .innerJoin(place).on(place.id.eq(placeChoice.placeId).and(place.permanentlyClosed.eq(false)))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .fetch();

        List<Long> placeIds = placeChoices.stream()
            .map(tuple -> tuple.get(placeChoice.placeId))
            .distinct()
            .toList();

        List<Tuple> productTuples = queryFactory
            .select(
                product.placeId,
                new QProductSimpleResult(
                    product.id,
                    place.name,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountInfo.discountPrice,
                    product.discountInfo.discountRate
                )
            )
            .from(product)
            .innerJoin(place).on(place.id.eq(product.placeId))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.isActive.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isActive.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(productImage.imageFileId))
            .where(product.placeId.in(placeIds))
            .fetch();

        Map<Long, List<ProductSimpleResult>> productsByPlaceId = productTuples.stream()
            .filter(tuple -> tuple.get(product.placeId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(product.placeId)),
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

        return placeChoices.stream()
            .map(tuple -> {
                Long placeIdValue = tuple.get(placeChoice.placeId);
                List<ProductSimpleResult> products = productsByPlaceId.getOrDefault(placeIdValue, new ArrayList<>());
                return new EditorChoiceDto(
                    tuple.get(placeChoice.id),
                    placeIdValue,
                    tuple.get(place.name),
                    tuple.get(placeChoice.title),
                    tuple.get(placeChoice.content),
                    tuple.get(uploadedFile.filePath),
                    products
                );
            })
            .toList();
    }

    @Override
    public Page<EditorChoiceDto> findEditorChoice(Pageable pageable) {
        Long totalCount = queryFactory
            .select(placeChoice.count())
            .from(placeChoice)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<Tuple> placeChoices = queryFactory
            .select(
                placeChoice.id,
                placeChoice.placeId,
                place.name,
                placeChoice.title,
                placeChoice.content,
                uploadedFile.filePath
            )
            .from(placeChoice)
            .innerJoin(place).on(place.id.eq(placeChoice.placeId).and(place.permanentlyClosed.eq(false)))
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailImageFileId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<Long> placeIds = placeChoices.stream()
            .map(tuple -> tuple.get(placeChoice.placeId))
            .distinct()
            .toList();

        List<Tuple> productTuples = queryFactory
            .select(
                product.placeId,
                new QProductSimpleResult(
                    product.id,
                    place.name,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountInfo.discountPrice,
                    product.discountInfo.discountRate
                )
            )
            .from(product)
            .innerJoin(place).on(place.id.eq(product.placeId))
            .leftJoin(productImage).on(
                productImage.productId.eq(product.id)
                    .and(productImage.isActive.eq(true))
                    .and(productImage.sort.eq(
                        JPAExpressions
                            .select(subProductImage.sort.min())
                            .from(subProductImage)
                            .where(subProductImage.productId.eq(product.id)
                                .and(subProductImage.isActive.eq(true)))
                    ))
            )
            .leftJoin(uploadedFile).on(productImage.imageFileId.eq(uploadedFile.id))
            .where(product.placeId.in(placeIds))
            .fetch();

        Map<Long, List<ProductSimpleResult>> productsByPlaceId = productTuples.stream()
            .filter(tuple -> tuple.get(product.placeId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(product.placeId)),
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

        List<EditorChoiceDto> content = placeChoices.stream()
            .map(tuple -> {
                Long placeIdValue = tuple.get(placeChoice.placeId);
                List<ProductSimpleResult> products = productsByPlaceId.getOrDefault(placeIdValue, new ArrayList<>());
                return new EditorChoiceDto(
                    tuple.get(placeChoice.id),
                    placeIdValue,
                    tuple.get(place.name),
                    tuple.get(placeChoice.title),
                    tuple.get(placeChoice.content),
                    tuple.get(uploadedFile.filePath),
                    products
                );
            })
            .toList();

        return new PageImpl<>(content, pageable, totalCount);
    }
}

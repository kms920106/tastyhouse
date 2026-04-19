package com.tastyhouse.core.repository.place;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.place.dto.EditorChoiceDto;
import com.tastyhouse.core.entity.product.QProductImage;
import com.tastyhouse.core.entity.product.dto.ProductSimpleDto;
import com.tastyhouse.core.entity.product.dto.QProductSimpleDto;
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

import static com.tastyhouse.core.entity.file.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.place.dto.QPlaceChoice.placeChoice;
import static com.tastyhouse.core.entity.place.QPlace.place;
import static com.tastyhouse.core.entity.product.QProduct.product;

@Repository
@RequiredArgsConstructor
public class PlaceChoiceRepositoryImpl implements PlaceChoiceRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EditorChoiceDto> findEditorChoice() {
        // 1. PlaceChoice 기본 정보 조회
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
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailUploadedFileId))
            .fetch();

        // 2. 모든 PlaceChoice의 placeId 추출
        List<Long> placeIds = placeChoices.stream()
            .map(tuple -> tuple.get(placeChoice.placeId))
            .distinct()
            .toList();

        // 3. 해당 placeId들의 모든 products 조회 (placeId도 함께 조회)
        QProductImage productImage = QProductImage.productImage;
        QProductImage subProductImage = new QProductImage("subProductImage");

        List<Tuple> productTuples = queryFactory
            .select(
                product.placeId,
                new QProductSimpleDto(
                    product.id,
                    place.name,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountPrice,
                    product.discountRate
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
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(productImage.uploadedFileId))
            .where(product.placeId.in(placeIds))
            .fetch();

        // 4. placeId별로 products 그룹핑 (각 매장마다 2개씩만)
        Map<Long, List<ProductSimpleDto>> productsByPlaceId = productTuples.stream()
            .filter(tuple -> tuple.get(product.placeId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(product.placeId)),
                Collectors.mapping(
                    tuple -> tuple.get(1, ProductSimpleDto.class),
                    Collectors.toList()
                )
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().limit(2).toList()
            ));

        // 5. EditorChoiceDto 생성
        return placeChoices.stream()
            .map(tuple -> {
                Long placeIdValue = tuple.get(placeChoice.placeId);
                List<ProductSimpleDto> products = productsByPlaceId.getOrDefault(placeIdValue, new ArrayList<>());

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
        // 1. 전체 개수 조회
        Long totalCount = queryFactory
            .select(placeChoice.count())
            .from(placeChoice)
            .fetchOne();

        if (totalCount == null || totalCount == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        // 2. PlaceChoice 기본 정보 조회 (페이징 적용)
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
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(place.thumbnailUploadedFileId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 3. 모든 PlaceChoice의 placeId 추출
        List<Long> placeIds = placeChoices.stream()
            .map(tuple -> tuple.get(placeChoice.placeId))
            .distinct()
            .toList();

        // 4. 해당 placeId들의 모든 products 조회 (placeId도 함께 조회)
        QProductImage productImage = QProductImage.productImage;
        QProductImage subProductImage = new QProductImage("subProductImage");

        List<Tuple> productTuples = queryFactory
            .select(
                product.placeId,
                new QProductSimpleDto(
                    product.id,
                    place.name,
                    product.name,
                    uploadedFile.filePath,
                    product.originalPrice,
                    product.discountPrice,
                    product.discountRate
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
            .leftJoin(uploadedFile).on(productImage.uploadedFileId.eq(uploadedFile.id))
            .where(product.placeId.in(placeIds))
            .fetch();

        // 5. placeId별로 products 그룹핑 (각 매장마다 2개씩만)
        Map<Long, List<ProductSimpleDto>> productsByPlaceId = productTuples.stream()
            .filter(tuple -> tuple.get(product.placeId) != null)
            .collect(Collectors.groupingBy(
                tuple -> Objects.requireNonNull(tuple.get(product.placeId)),
                Collectors.mapping(
                    tuple -> tuple.get(1, ProductSimpleDto.class),
                    Collectors.toList()
                )
            ))
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().limit(2).toList()
            ));

        // 6. EditorChoiceDto 생성
        List<EditorChoiceDto> content = placeChoices.stream()
            .map(tuple -> {
                Long placeIdValue = tuple.get(placeChoice.placeId);
                List<ProductSimpleDto> products = productsByPlaceId.getOrDefault(placeIdValue, new ArrayList<>());

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

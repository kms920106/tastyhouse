package com.tastyhouse.core.domain.product.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.model.ProductCommonOption;
import com.tastyhouse.core.domain.product.domain.model.ProductCommonOptionGroup;
import com.tastyhouse.core.domain.product.domain.model.ProductOption;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductBbqRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductCommonOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductImageRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionGroupRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductOptionRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepresentativeImage;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionGroupId;
import com.tastyhouse.core.domain.product.domain.vo.ProductOptionId;
import com.tastyhouse.core.domain.product.application.dto.ProductSearchCondition;
import com.tastyhouse.core.domain.product.application.dto.command.BatchItem;
import com.tastyhouse.core.domain.product.application.dto.command.ProductBatchQuery;
import com.tastyhouse.core.domain.product.application.dto.result.BatchOptionResult;
import com.tastyhouse.core.domain.product.application.dto.result.OptionGroupResult;
import com.tastyhouse.core.domain.product.application.dto.result.OptionInfo;
import com.tastyhouse.core.domain.product.application.dto.result.OptionResult;
import com.tastyhouse.core.domain.product.application.dto.result.ProductBatchResult;
import com.tastyhouse.core.domain.product.application.dto.result.ProductListItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.ProductOptionsResult;
import com.tastyhouse.core.domain.product.application.dto.result.SearchProductItemResult;
import com.tastyhouse.core.domain.product.application.dto.result.TodayDiscountProductResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductCommonOptionGroupRepository productCommonOptionGroupRepository;
    private final ProductCommonOptionRepository productCommonOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductBbqRepository productBbqRepository;

    public Optional<Product> findProductById(ProductId productId) {
        return productRepository.findById(productId);
    }

    public List<Product> findActiveProductsByShopId(Long shopId) {
        return productRepository.findActiveByShopIdOrderByRepresentativeAndRating(shopId);
    }

    public List<ProductCategory> findProductCategoriesByShopId(Long shopId) {
        return productCategoryRepository.findActiveCategoriesByShopIdOrderBySort(shopId);
    }

    public List<ProductCategory> findProductCategoriesByNameAndShopId(String name, Long shopId) {
        return productCategoryRepository.findCategoriesByNameAndShopId(name, shopId);
    }

    public Optional<ProductOptionGroup> findProductOptionGroupById(ProductOptionGroupId optionGroupId) {
        return productOptionGroupRepository.findById(optionGroupId);
    }

    public Optional<ProductOption> findProductOptionById(ProductOptionId optionId) {
        return productOptionRepository.findById(optionId);
    }

    public String getFirstImageFilePath(Long productId) {
        return productImageRepository.findActiveByProductIdOrderBySort(productId)
            .stream()
            .findFirst()
            .map(image -> productImageRepository.findFilePathByImageFileId(image.getImageFileId()))
            .orElse(null);
    }

    public List<String> getAllImageFilePaths(Long productId) {
        return productImageRepository.findActiveByProductIdOrderBySort(productId)
            .stream()
            .map(image -> productImageRepository.findFilePathByImageFileId(image.getImageFileId()))
            .toList();
    }

    public PageResult<TodayDiscountProductResult> findTodayDiscountProducts(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productRepository.findTodayDiscountProducts(pageQuery);
    }

    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productRepository.searchByKeyword(keyword, pageQuery);
    }

    public PageResult<ProductListItemResult> findProducts(ProductSearchCondition condition, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productRepository.findProducts(condition, pageQuery);
    }

    public ProductOptionsResult findProductOptions(Long productId) {
        List<OptionGroupResult> result = new ArrayList<>();

        List<ProductOptionGroup> optionGroups = productOptionGroupRepository.findActiveByProductIdOrderBySort(productId);
        if (!optionGroups.isEmpty()) {
            List<Long> groupIds = optionGroups.stream().map(ProductOptionGroup::getId).toList();
            List<ProductOption> options = productOptionRepository.findActiveByOptionGroupIdsOrderBySort(groupIds);
            Map<Long, List<ProductOption>> byGroupId = options.stream()
                .collect(Collectors.groupingBy(ProductOption::getOptionGroupId));

            for (ProductOptionGroup group : optionGroups) {
                List<OptionResult> optionResults = byGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(o -> new OptionResult(o.getId(), o.getName(), o.getAdditionalPrice(), o.isSoldOut()))
                    .toList();
                result.add(new OptionGroupResult(
                    group.getId(), group.getName(), group.getDescription(),
                    group.isRequired(), group.isMultipleSelect(),
                    group.getMinSelect(), group.getMaxSelect(), false, optionResults
                ));
            }
        }

        List<ProductCommonOptionGroup> commonGroups = productCommonOptionGroupRepository.findActiveByProductIdOrderBySort(productId);
        if (!commonGroups.isEmpty()) {
            List<Long> commonGroupIds = commonGroups.stream().map(ProductCommonOptionGroup::getId).toList();
            List<ProductCommonOption> commonOptions = productCommonOptionRepository.findActiveByOptionGroupIdsOrderBySort(commonGroupIds);
            Map<Long, List<ProductCommonOption>> byCommonGroupId = commonOptions.stream()
                .collect(Collectors.groupingBy(ProductCommonOption::getOptionGroupId));

            for (ProductCommonOptionGroup group : commonGroups) {
                List<OptionResult> optionResults = byCommonGroupId
                    .getOrDefault(group.getId(), Collections.emptyList())
                    .stream()
                    .map(o -> new OptionResult(o.getId(), o.getName(), o.getAdditionalPrice(), o.isSoldOut()))
                    .toList();
                result.add(new OptionGroupResult(
                    group.getId(), group.getName(), group.getDescription(),
                    group.isRequired(), group.isMultipleSelect(),
                    group.getMinSelect(), group.getMaxSelect(), true, optionResults
                ));
            }
        }

        return new ProductOptionsResult(result);
    }

    /**
     * 장바구니 배치 조회. (상품ID, 옵션ID) 조합 목록을 받아 상품 단위로 그룹핑하여 반환합니다.
     * - 존재하지 않거나 비활성인 상품은 제외하지 않고 available=false 로 남깁니다.
     *   (프론트가 "판매 종료" 안내를 띄울 수 있도록 — 쿠팡 cartItemEnable 방식)
     * - 옵션은 해당 상품에 실제로 속하고 조회에 성공한 경우에만 options 에 포함됩니다.
     * - 요청한 productId 의 최초 등장 순서를 유지합니다.
     * 상품/옵션/그룹을 각각 배치(in) 조회하여 N+1 을 방지합니다.
     */
    public List<ProductBatchResult> findProductsBatch(ProductBatchQuery query) {
        List<BatchItem> items = query.items();
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        List<Long> productIds = items.stream()
            .map(BatchItem::productId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();
        List<Long> optionIds = items.stream()
            .map(BatchItem::optionId)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();

        Map<Long, Product> productById = productRepository.findAllByIds(productIds).stream()
            .collect(Collectors.toMap(Product::getId, p -> p));

        // 대표 이미지 경로 배치 조회. productId -> filePath (대표 이미지 없으면 키 없음)
        Map<Long, String> imagePathByProductId = productImageRepository
            .findRepresentativeImagePathsByProductIds(productIds).stream()
            .collect(Collectors.toMap(
                ProductRepresentativeImage::productId,
                ProductRepresentativeImage::filePath,
                (existing, ignored) -> existing
            ));

        // 옵션 조회 (일반 + 공통). optionId -> (groupId, name, additionalPrice)
        Map<Long, OptionInfo> optionById = new java.util.HashMap<>();
        List<ProductOption> options = productOptionRepository.findActiveByIds(optionIds);
        for (ProductOption o : options) {
            optionById.put(o.getId(), new OptionInfo(o.getOptionGroupId(), o.getName(), o.getAdditionalPrice(), false));
        }
        List<ProductCommonOption> commonOptions = productCommonOptionRepository.findActiveByIds(optionIds);
        for (ProductCommonOption o : commonOptions) {
            optionById.putIfAbsent(o.getId(), new OptionInfo(o.getOptionGroupId(), o.getName(), o.getAdditionalPrice(), true));
        }

        // 그룹 조회 (옵션의 소속 상품 검증용). groupId -> productId
        List<Long> normalGroupIds = optionById.values().stream()
            .filter(info -> !info.common()).map(OptionInfo::groupId).distinct().toList();
        List<Long> commonGroupIds = optionById.values().stream()
            .filter(OptionInfo::common).map(OptionInfo::groupId).distinct().toList();

        Map<Long, Long> normalGroupProductId = productOptionGroupRepository.findAllByIds(normalGroupIds).stream()
            .collect(Collectors.toMap(ProductOptionGroup::getId, ProductOptionGroup::getProductId));
        Map<Long, Long> commonGroupProductId = productCommonOptionGroupRepository.findAllByIds(commonGroupIds).stream()
            .collect(Collectors.toMap(ProductCommonOptionGroup::getId, ProductCommonOptionGroup::getProductId));

        // 요청 순서(productId 최초 등장순)를 유지하며 상품별로 옵션을 그룹핑.
        // 미존재 상품도 available=false 로 남기기 위해 모든 요청 productId 를 키로 등록한다.
        Map<Long, List<BatchOptionResult>> optionsByProductId = new java.util.LinkedHashMap<>();
        for (BatchItem item : items) {
            Long productId = item.productId();
            if (productId == null) {
                continue;
            }
            optionsByProductId.computeIfAbsent(productId, k -> new ArrayList<>());

            // 상품이 없으면 옵션도 채울 수 없으므로 건너뛴다(키는 위에서 이미 등록됨).
            if (!productById.containsKey(productId)) {
                continue;
            }

            Long optionId = item.optionId();
            if (optionId == null) {
                continue;
            }
            OptionInfo optionInfo = optionById.get(optionId);
            if (optionInfo == null) {
                continue;
            }
            Long ownerProductId = optionInfo.common()
                ? commonGroupProductId.get(optionInfo.groupId())
                : normalGroupProductId.get(optionInfo.groupId());
            // 그룹이 없거나, 옵션이 요청한 상품에 속하지 않으면 제외
            // (ownerProductId 가 null 이면 productId.equals 가 false 이므로 함께 걸러진다)
            if (!productId.equals(ownerProductId)) {
                continue;
            }
            List<BatchOptionResult> bucket = optionsByProductId.get(productId);
            boolean alreadyAdded = bucket.stream().anyMatch(o -> o.id().equals(optionId));
            if (!alreadyAdded) {
                bucket.add(new BatchOptionResult(
                    optionId, optionInfo.name(), optionInfo.additionalPrice()
                ));
            }
        }

        return optionsByProductId.entrySet().stream()
            .map(entry -> {
                Product product = productById.get(entry.getKey());
                if (product == null) {
                    // 존재하지 않거나 비활성인 상품: available=false 로 남긴다.
                    return new ProductBatchResult(entry.getKey(), false, null, null, null, null, null, List.of());
                }
                return new ProductBatchResult(
                    product.getId(),
                    true,
                    product.getName(),
                    imagePathByProductId.get(product.getId()),
                    product.getOriginalPrice(),
                    product.getDiscountPrice(),
                    product.getDiscountRate(),
                    entry.getValue()
                );
            })
            .toList();
    }

    public Optional<ProductBbq> findFirstBbqWithOptionsSyncPending() {
        return productBbqRepository.findFirstWithOptionsSyncPending();
    }
}

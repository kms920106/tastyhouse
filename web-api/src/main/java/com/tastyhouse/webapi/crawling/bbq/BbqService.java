package com.tastyhouse.webapi.crawling.bbq;

import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.ProductImage;
import com.tastyhouse.core.service.ProductCoreService;
import com.tastyhouse.external.crawling.bbq.BbqApiClient;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuCategoryResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuSubOptionResponse;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductCategoryResponse;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductResponse;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductSubOptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BBQ 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BbqService {

    private final BbqApiClient bbqApiClient;
    private final ProductCoreService productCoreService;

    /**
     * BBQ 메뉴 카테고리 목록 조회
     */
    public List<BbqProductCategoryResponse> getMenuCategories() {
        try {
            List<BbqMenuCategoryResponse> externalCategories = bbqApiClient.getMenuCategoriesSync();
            return externalCategories.stream()
                    .map(this::convertToProductCategoryResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("BBQ 메뉴 카테고리 조회 중 오류 발생", e);
            throw new RuntimeException("BBQ 메뉴 카테고리 조회 실패", e);
        }
    }

    /**
     * BBQ 카테고리별 메뉴 목록 조회
     */
    public List<BbqProductResponse> getMenusByCategoryId(Long categoryId) {
        try {
            List<BbqMenuResponse> externalMenus = bbqApiClient.getMenusByCategoryIdSync(categoryId);
            return externalMenus.stream()
                    .map(this::convertToProductResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("BBQ 카테고리별 메뉴 조회 중 오류 발생: categoryId={}", categoryId, e);
            throw new RuntimeException("BBQ 카테고리별 메뉴 조회 실패", e);
        }
    }

    /**
     * 외부 API 응답을 ProductCategory 구조에 맞는 응답으로 변환
     */
    private BbqProductCategoryResponse convertToProductCategoryResponse(BbqMenuCategoryResponse externalResponse) {
        return BbqProductCategoryResponse.from(
                externalResponse.getId(),
                null, // 외부 API에는 placeId 정보가 없으므로 null로 설정
                externalResponse.getCategoryName(),
                externalResponse.getPriority(),
                true // 외부 API에서 조회된 카테고리는 활성화 상태로 간주
        );
    }

    /**
     * BBQ 메뉴 상세 조회
     */
    public BbqProductResponse getMenuDetail(Long menuId) {
        try {
            BbqMenuResponse externalMenu = bbqApiClient.getMenuDetailSync(menuId);
            return convertToProductResponse(externalMenu);
        } catch (Exception e) {
            log.error("BBQ 메뉴 상세 조회 중 오류 발생: menuId={}", menuId, e);
            throw new RuntimeException("BBQ 메뉴 상세 조회 실패", e);
        }
    }

    /**
     * 외부 API 응답을 Product 구조에 맞는 응답으로 변환
     */
    private BbqProductResponse convertToProductResponse(BbqMenuResponse externalResponse) {
        return BbqProductResponse.from(
                externalResponse.getId(),
                externalResponse.getMenuName(),
                externalResponse.getDescription(),
                externalResponse.getMenuImageUrl(),
                externalResponse.getMenuPrice(),
                externalResponse.getAddPrice(),
                externalResponse.getIsSoldOut() != null ? externalResponse.getIsSoldOut() : false,
                externalResponse.getIsAdultOnly() != null ? externalResponse.getIsAdultOnly() : false,
                externalResponse.getCanDeliver() != null ? externalResponse.getCanDeliver() : false,
                externalResponse.getCanTakeout() != null ? externalResponse.getCanTakeout() : false
        );
    }

    /**
     * BBQ 메뉴 서브 옵션 조회
     */
    public List<BbqProductSubOptionResponse> getMenuSubOptions(Long menuId) {
        try {
            List<BbqMenuSubOptionResponse> externalSubOptions = bbqApiClient.getMenuSubOptionsSync(menuId);
            return externalSubOptions.stream()
                    .map(this::convertToProductSubOptionResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("BBQ 메뉴 서브 옵션 조회 중 오류 발생: menuId={}", menuId, e);
            throw new RuntimeException("BBQ 메뉴 서브 옵션 조회 실패", e);
        }
    }

    /**
     * 외부 API 응답을 서브 옵션 응답으로 변환
     */
    private BbqProductSubOptionResponse convertToProductSubOptionResponse(BbqMenuSubOptionResponse externalResponse) {
        List<BbqProductSubOptionResponse.SubOptionItemDetailResponse> itemDetails = null;
        if (externalResponse.getSubOptionItemDetailResponseList() != null) {
            itemDetails = externalResponse.getSubOptionItemDetailResponseList().stream()
                    .map(item -> BbqProductSubOptionResponse.SubOptionItemDetailResponse.from(
                            item.getId(),
                            item.getItemTitle(),
                            item.getAddPrice(),
                            item.getIsSoldOut() != null ? item.getIsSoldOut() : false,
                            item.getIsHidden() != null ? item.getIsHidden() : false
                    ))
                    .collect(Collectors.toList());
        }

        return BbqProductSubOptionResponse.from(
                externalResponse.getId(),
                externalResponse.getSubOptionTitle(),
                externalResponse.getRequiredSelectCount(),
                externalResponse.getMaxSelectCount(),
                itemDetails
        );
    }

    /**
     * BBQ 메뉴 크롤링 및 저장
     */
    @Transactional
    public void crawlAndSaveNewMenu(Long placeId) {
        try {
            // 1. getMenuCategories 호출하여 카테고리 저장
            List<BbqProductCategoryResponse> menuCategories = getMenuCategories();
            log.info("BBQ 카테고리 {}개 조회 완료", menuCategories.size());

            for (int categoryIndex = 0; categoryIndex < menuCategories.size(); categoryIndex++) {
                BbqProductCategoryResponse categoryResponse = menuCategories.get(categoryIndex);

                // 카테고리 저장 또는 조회
                ProductCategory savedCategory = saveOrGetCategory(placeId, categoryResponse, categoryIndex);

                // 2. getMenusByCategoryId 호출하여 상품 목록 가져오기
                List<BbqProductResponse> menus = getMenusByCategoryId(categoryResponse.id());
                log.info("카테고리 '{}' - 상품 {}개 조회", categoryResponse.name(), menus.size());

                // 상품(+이미지) 저장
                for (int menuIndex = 0; menuIndex < menus.size(); menuIndex++) {
                    BbqProductResponse menuResponse = menus.get(menuIndex);
                    saveProductWithImage(placeId, savedCategory.getId(), menuResponse, categoryResponse.id(), menuIndex);
                }

                // 마지막 카테고리가 아닌 경우 10초 대기
                if (categoryIndex < menuCategories.size() - 1) {
                    log.info("다음 카테고리 처리를 위해 10초 대기...");
                    Thread.sleep(10000);
                }
            }

            log.info("BBQ 메뉴 크롤링 및 저장 완료. placeId: {}", placeId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("크롤링 중 인터럽트 발생: placeId={}", placeId, e);
            throw new RuntimeException("크롤링 중단됨", e);
        } catch (Exception e) {
            log.error("BBQ 메뉴 크롤링 및 저장 중 오류 발생: placeId={}", placeId, e);
            throw new RuntimeException("BBQ 메뉴 크롤링 및 저장 실패", e);
        }
    }

    /**
     * 카테고리 저장 또는 기존 카테고리 조회
     */
    private ProductCategory saveOrGetCategory(Long placeId, BbqProductCategoryResponse categoryResponse, int sort) {
        List<ProductCategory> existingCategories = productCoreService.findProductCategoriesByNameAndPlaceId(categoryResponse.name(), placeId);
        if (!existingCategories.isEmpty()) {
            return existingCategories.get(0);
        }

        ProductCategory category = ProductCategory.builder()
                .placeId(placeId)
                .name(categoryResponse.name())
                .sort(sort)
                .isActive(true)
                .build();
        return productCoreService.saveProductCategory(category);
    }

    /**
     * 상품 및 이미지 저장
     */
    private void saveProductWithImage(Long placeId, Long categoryId, BbqProductResponse menuResponse, Long bbqCategoryId, int sort) {
        // 상품 상세 정보 조회
        BbqProductResponse menuDetail = getMenuDetail(menuResponse.id());

        Product product = Product.builder()
                .placeId(placeId)
                .productCategoryId(categoryId)
                .name(menuDetail.name())
                .description(menuDetail.description())
                .originalPrice(menuDetail.originalPrice())
                .discountPrice(null)
                .discountRate(null)
                .rating(null)
                .reviewCount(0)
                .isRepresentative(false)
                .spiciness(null)
                .isSoldOut(menuDetail.isSoldOut() != null ? menuDetail.isSoldOut() : false)
                .isActive(true)
                .sort(sort)
                .build();
        Product savedProduct = productCoreService.saveProduct(product);

        // 상품 이미지 저장
        if (menuDetail.imageUrl() != null && !menuDetail.imageUrl().isEmpty()) {
            ProductImage productImage = ProductImage.builder()
                    .productId(savedProduct.getId())
                    .imageUrl(menuDetail.imageUrl())
                    .sort(0)
                    .isActive(true)
                    .build();
            productCoreService.saveProductImage(productImage);
        }

        // ProductBbq 매핑 저장 (외부 BBQ 메뉴 ID 저장)
        ProductBbq productBbq = ProductBbq.builder()
                .productId(savedProduct.getId())
                .bbqMenuId(menuResponse.id())
                .bbqCategoryId(bbqCategoryId)
                .build();
        productCoreService.saveProductBbq(productBbq);

        log.debug("상품 저장 완료: productId={}, name={}", savedProduct.getId(), savedProduct.getName());
    }
}

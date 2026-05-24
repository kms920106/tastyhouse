package com.tastyhouse.webapi.crawling.bbq;

import com.tastyhouse.core.domain.product.application.ProductCommandService;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.command.CreateProductCategoryCommand;
import com.tastyhouse.core.domain.product.application.dto.command.CreateProductCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductBbqCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductImageCommand;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.external.crawling.bbq.BbqApiClient;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuCategoryResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuSubOptionResponse;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductCategoryResponse;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductResponse;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductSubOptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BbqService {

    private final BbqApiClient bbqApiClient;
    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;
    private final FileService fileService;

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

    private BbqProductCategoryResponse convertToProductCategoryResponse(BbqMenuCategoryResponse externalResponse) {
        return BbqProductCategoryResponse.from(
                externalResponse.getId(),
                null,
                externalResponse.getCategoryName(),
                externalResponse.getPriority(),
                true
        );
    }

    public BbqProductResponse getMenuDetail(Long menuId) {
        try {
            BbqMenuResponse externalMenu = bbqApiClient.getMenuDetailSync(menuId);
            return convertToProductResponse(externalMenu);
        } catch (Exception e) {
            log.error("BBQ 메뉴 상세 조회 중 오류 발생: menuId={}", menuId, e);
            throw new RuntimeException("BBQ 메뉴 상세 조회 실패", e);
        }
    }

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

    @Transactional
    public void crawlAndSaveNewMenu(Long placeId) {
        try {
            List<BbqProductCategoryResponse> menuCategories = getMenuCategories();
            log.info("BBQ 카테고리 {}개 조회 완료", menuCategories.size());

            for (int categoryIndex = 0; categoryIndex < menuCategories.size(); categoryIndex++) {
                BbqProductCategoryResponse categoryResponse = menuCategories.get(categoryIndex);

                ProductCategory savedCategory = saveOrGetCategory(placeId, categoryResponse, categoryIndex);

                List<BbqProductResponse> menus = getMenusByCategoryId(categoryResponse.id());
                log.info("카테고리 '{}' - 상품 {}개 조회", categoryResponse.name(), menus.size());

                for (int menuIndex = 0; menuIndex < menus.size(); menuIndex++) {
                    BbqProductResponse menuResponse = menus.get(menuIndex);
                    saveProductWithImage(placeId, savedCategory.getId(), menuResponse, categoryResponse.id(), menuIndex);
                }

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

    private ProductCategory saveOrGetCategory(Long placeId, BbqProductCategoryResponse categoryResponse, int sort) {
        List<ProductCategory> existingCategories = productQueryService.findProductCategoriesByNameAndPlaceId(categoryResponse.name(), placeId);
        if (!existingCategories.isEmpty()) {
            return existingCategories.get(0);
        }
        return productCommandService.createProductCategory(new CreateProductCategoryCommand(
            placeId, categoryResponse.name(), sort, true
        ));
    }

    private void saveProductWithImage(Long placeId, Long categoryId, BbqProductResponse menuResponse, Long bbqCategoryId, int sort) {
        BbqProductResponse menuDetail = getMenuDetail(menuResponse.id());

        Product savedProduct = productCommandService.createProduct(new CreateProductCommand(
            placeId, categoryId, menuDetail.name(), menuDetail.description(),
            menuDetail.originalPrice(), null, null, null, 0, false, null,
            menuDetail.isSoldOut() != null ? menuDetail.isSoldOut() : false, true, sort
        ));

        if (menuDetail.imageUrl() != null && !menuDetail.imageUrl().isEmpty()) {
            Long uploadedFileId = fileService.uploadFromUrl(menuDetail.imageUrl());
            productCommandService.saveProductImage(new SaveProductImageCommand(
                savedProduct.getId(), uploadedFileId, 0, true
            ));
        }

        productCommandService.saveProductBbq(new SaveProductBbqCommand(
            savedProduct.getId(), menuResponse.id(), bbqCategoryId, false
        ));

        log.debug("상품 저장 완료: productId={}, name={}", savedProduct.getId(), savedProduct.getName());
    }
}

package com.tastyhouse.webapi.crawling.bbq;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                externalResponse.getSoldOut() != null ? externalResponse.getSoldOut() : false,
                externalResponse.getAdultOnly() != null ? externalResponse.getAdultOnly() : false,
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
                            item.getSoldOut() != null ? item.getSoldOut() : false,
                            item.getHidden() != null ? item.getHidden() : false
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

    @SuppressWarnings("unused")
    @Transactional
    public void crawlAndSaveNewMenu(Long shopId) {
        try {
            List<BbqProductCategoryResponse> menuCategories = getMenuCategories();
            log.info("BBQ 카테고리 {}개 조회 완료", menuCategories.size());

            for (int categoryIndex = 0; categoryIndex < menuCategories.size(); categoryIndex++) {
                BbqProductCategoryResponse categoryResponse = menuCategories.get(categoryIndex);

                ProductCategory savedCategory = saveOrGetCategory(shopId, categoryResponse, categoryIndex);

                List<BbqProductResponse> menus = getMenusByCategoryId(categoryResponse.id());
                log.info("카테고리 '{}' - 상품 {}개 조회", categoryResponse.name(), menus.size());

                for (int menuIndex = 0; menuIndex < menus.size(); menuIndex++) {
                    BbqProductResponse menuResponse = menus.get(menuIndex);
                    saveProductWithImage(shopId, savedCategory.getId(), menuResponse, categoryResponse.id(), menuIndex);
                }

                if (categoryIndex < menuCategories.size() - 1) {
                    // 외부 BBQ 서버 부하 방지를 위한 의도적인 요청 간 지연 (busy-wait 아님)
                    log.info("다음 카테고리 처리를 위해 10초 대기...");
                    //noinspection BusyWait
                    Thread.sleep(10000);
                }
            }

            log.info("BBQ 메뉴 크롤링 및 저장 완료. shopId: {}", shopId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("크롤링 중 인터럽트 발생: shopId={}", shopId, e);
            throw new RuntimeException("크롤링 중단됨", e);
        } catch (Exception e) {
            log.error("BBQ 메뉴 크롤링 및 저장 중 오류 발생: shopId={}", shopId, e);
            throw new RuntimeException("BBQ 메뉴 크롤링 및 저장 실패", e);
        }
    }

    private ProductCategory saveOrGetCategory(Long shopId, BbqProductCategoryResponse categoryResponse, int sort) {
        List<ProductCategory> existingCategories = productQueryService.findProductCategoriesByNameAndShopId(categoryResponse.name(), shopId);
        if (!existingCategories.isEmpty()) {
            return existingCategories.getFirst();
        }
        return productCommandService.createProductCategory(CreateProductCategoryCommand.of(
            shopId, categoryResponse.name(), sort, true
        ));
    }

    private void saveProductWithImage(Long shopId, Long categoryId, BbqProductResponse menuResponse, Long bbqCategoryId, int sort) {
        BbqProductResponse menuDetail = getMenuDetail(menuResponse.id());

        Product savedProduct = productCommandService.createProduct(CreateProductCommand.of(
            shopId, categoryId, menuDetail.name(), menuDetail.description(),
            menuDetail.originalPrice(), null, null, null, 0, false, null,
            menuDetail.soldOut(), true, sort
        ));

        if (menuDetail.imageUrl() != null && !menuDetail.imageUrl().isEmpty()) {
            Long uploadedFileId = fileService.uploadFromUrl(menuDetail.imageUrl());
            productCommandService.saveProductImage(SaveProductImageCommand.of(
                savedProduct.getId(), uploadedFileId, 0, true
            ));
        }

        productCommandService.saveProductBbq(SaveProductBbqCommand.of(
            savedProduct.getId(), menuResponse.id(), bbqCategoryId, false
        ));

        log.debug("상품 저장 완료: productId={}, name={}", savedProduct.getId(), savedProduct.getName());
    }
}

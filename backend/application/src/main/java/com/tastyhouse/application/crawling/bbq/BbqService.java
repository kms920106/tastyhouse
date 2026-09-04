package com.tastyhouse.application.crawling.bbq;

import com.tastyhouse.application.shared.marker.BatchApp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.crawling.bbq.port.out.BbqMenuPort;
import com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductCategoryResponse;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductResponse;
import com.tastyhouse.application.crawling.bbq.port.out.BbqProductSubOptionResponse;
import com.tastyhouse.application.shared.exception.BatchJobException;

@Service
@BatchApp
public class BbqService {

    private static final Logger log = LoggerFactory.getLogger(BbqService.class);

    private final BbqMenuPort bbqMenuPort;
    private final BbqProductSyncService bbqProductSyncService;
    private final RemoteImagePort remoteImagePort;

    public BbqService(
        BbqMenuPort bbqMenuPort,
        BbqProductSyncService bbqProductSyncService,
        RemoteImagePort remoteImagePort
    ) {
        this.bbqMenuPort = bbqMenuPort;
        this.bbqProductSyncService = bbqProductSyncService;
        this.remoteImagePort = remoteImagePort;
    }

    public List<BbqProductCategoryResponse> getMenuCategories() {
        try {
            return bbqMenuPort.fetchMenuCategories();
        } catch (Exception e) {
            log.error("BBQ 메뉴 카테고리 조회 중 오류 발생", e);
            throw new BatchJobException("BBQ 메뉴 카테고리 조회 실패", e);
        }
    }

    public List<BbqProductResponse> getMenusByCategoryId(Long categoryId) {
        try {
            return bbqMenuPort.fetchMenusByCategoryId(categoryId);
        } catch (Exception e) {
            log.error("BBQ 카테고리별 메뉴 조회 중 오류 발생: categoryId={}", categoryId, e);
            throw new BatchJobException("BBQ 카테고리별 메뉴 조회 실패", e);
        }
    }

    public BbqProductResponse getMenuDetail(Long menuId) {
        try {
            return bbqMenuPort.fetchMenuDetail(menuId);
        } catch (Exception e) {
            log.error("BBQ 메뉴 상세 조회 중 오류 발생: menuId={}", menuId, e);
            throw new BatchJobException("BBQ 메뉴 상세 조회 실패", e);
        }
    }

    public List<BbqProductSubOptionResponse> getMenuSubOptions(Long menuId) {
        try {
            return bbqMenuPort.fetchMenuSubOptions(menuId);
        } catch (Exception e) {
            log.error("BBQ 메뉴 서브 옵션 조회 중 오류 발생: menuId={}", menuId, e);
            throw new BatchJobException("BBQ 메뉴 서브 옵션 조회 실패", e);
        }
    }

    @SuppressWarnings("unused")
    @Transactional
    public void crawlAndSaveNewMenu(Long shopId) {
        try {
            List<BbqProductCategoryResponse> menuCategories = getMenuCategories();
            log.info("BBQ 카테고리 {}개 조회 완료", menuCategories.size());

            for (int categoryIndex = 0; categoryIndex < menuCategories.size(); categoryIndex++) {
                BbqProductCategoryResponse categoryResponse = menuCategories.get(categoryIndex);

                Long categoryId = bbqProductSyncService.resolveCategoryId(
                    shopId, categoryResponse.name(), categoryIndex
                );

                List<BbqProductResponse> menus = getMenusByCategoryId(categoryResponse.id());
                log.info("카테고리 '{}' - 상품 {}개 조회", categoryResponse.name(), menus.size());

                for (int menuIndex = 0; menuIndex < menus.size(); menuIndex++) {
                    BbqProductResponse menuResponse = menus.get(menuIndex);
                    saveProductWithImage(shopId, categoryId, menuResponse, categoryResponse.id(), menuIndex);
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
            throw new BatchJobException("크롤링 중단됨", e);
        } catch (Exception e) {
            log.error("BBQ 메뉴 크롤링 및 저장 중 오류 발생: shopId={}", shopId, e);
            throw new BatchJobException("BBQ 메뉴 크롤링 및 저장 실패", e);
        }
    }

    private void saveProductWithImage(Long shopId, Long categoryId, BbqProductResponse menuResponse, Long bbqCategoryId, int sort) {
        BbqProductResponse menuDetail = getMenuDetail(menuResponse.id());

        Long uploadedFileId = null;
        if (menuDetail.imageUrl() != null && !menuDetail.imageUrl().isEmpty()) {
            uploadedFileId = remoteImagePort.uploadFromUrl(menuDetail.imageUrl());
        }

        BbqProductRegistration registration = BbqProductRegistration.of(
            shopId,
            categoryId,
            menuDetail.name(),
            menuDetail.description(),
            menuDetail.originalPrice(),
            menuDetail.soldOut(),
            sort,
            uploadedFileId,
            menuResponse.id(),
            bbqCategoryId
        );
        Long productId = bbqProductSyncService.createCrawledProduct(registration);

        log.debug("상품 저장 완료: productId={}, name={}", productId, menuDetail.name());
    }
}

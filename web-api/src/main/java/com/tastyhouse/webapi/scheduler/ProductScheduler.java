package com.tastyhouse.webapi.scheduler;

import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.ProductOption;
import com.tastyhouse.core.entity.product.ProductOptionGroup;
import com.tastyhouse.core.repository.product.*;
import com.tastyhouse.webapi.crawling.bbq.BbqService;
import com.tastyhouse.webapi.crawling.bbq.response.BbqProductSubOptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductScheduler {

    private final BbqService bbqService;
    private final ProductJpaRepository productJpaRepository;
    private final ProductOptionGroupJpaRepository productOptionGroupJpaRepository;
    private final ProductOptionJpaRepository productOptionJpaRepository;
    private final ProductBbqRepository productBbqRepository;
    private final ProductBbqJpaRepository productBbqJpaRepository;

    private static final Long BBQ_PLACE_ID = 1L;

    /**
     * BBQ 상품 옵션 크롤링 스케줄러
     *
     * 옵션 동기화가 완료되지 않은 상품을 찾아서
     * getMenuSubOptions를 호출하여 옵션 저장 (10초 간격 실행)
     */
//    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void crawlAndSaveProductOptions() {
        try {
            // 옵션 동기화가 완료되지 않은 ProductBbq 조회
            Optional<ProductBbq> productBbqOpt = productBbqRepository.findFirstByIsOptionsSyncedFalse();
            if (productBbqOpt.isEmpty()) {
                log.debug("옵션 동기화가 필요한 상품이 없습니다.");
                return;
            }

            ProductBbq productBbq = productBbqOpt.get();
            Long productId = productBbq.getProductId();
            Long bbqMenuId = productBbq.getBbqMenuId();

            log.info("상품 옵션 크롤링 시작: productId={}, bbqMenuId={}", productId, bbqMenuId);

            // getMenuSubOptions 호출하여 옵션 저장
            saveProductOptions(productId, bbqMenuId);

            // 옵션 동기화 완료 표시 (옵션이 0개여도 완료 처리)
            productBbq.markOptionsSynced();
            productBbqJpaRepository.save(productBbq);

            log.info("상품 옵션 저장 완료: productId={}", productId);
        } catch (Exception e) {
            log.error("상품 옵션 크롤링 중 오류 발생", e);
        }
    }

    /**
     * 상품 옵션 저장
     */
    private void saveProductOptions(Long productId, Long bbqMenuId) {
        List<BbqProductSubOptionResponse> subOptions = bbqService.getMenuSubOptions(bbqMenuId);

        // subOptions가 비어있으면 기본 옵션 그룹 및 옵션 저장
        if (subOptions.isEmpty()) {
            // 상품 정보 조회
            Product product = productJpaRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: productId=" + productId));

            // 기본 옵션 그룹 저장
            ProductOptionGroup defaultGroup = ProductOptionGroup.builder()
                .productId(productId)
                .name("기본 선택")
                .description(null)
                .isRequired(false)
                .isMultipleSelect(false)
                .minSelect(0)
                .maxSelect(1)
                .sort(0)
                .isActive(true)
                .build();
            ProductOptionGroup savedOptionGroup = productOptionGroupJpaRepository.save(defaultGroup);

            // 기본 옵션 저장 (상품명 사용)
            ProductOption defaultOption = ProductOption.builder()
                .optionGroupId(savedOptionGroup.getId())
                .name(product.getName())
                .additionalPrice(0)
                .sort(0)
                .isSoldOut(false)
                .isActive(true)
                .build();
            productOptionJpaRepository.save(defaultOption);

            log.info("서브 옵션이 없어 기본 옵션 그룹 및 옵션 저장: productId={}, 상품명={}", productId, product.getName());
            return;
        }

        for (int i = 0; i < subOptions.size(); i++) {
            BbqProductSubOptionResponse subOption = subOptions.get(i);

            // ProductOptionGroup 저장
            ProductOptionGroup optionGroup = ProductOptionGroup.builder()
                .productId(productId)
                .name(subOption.subOptionTitle())
                .description(null)
                .isRequired(subOption.requiredSelectCount() != null && subOption.requiredSelectCount() > 0)
                .isMultipleSelect(subOption.maxSelectCount() != null && subOption.maxSelectCount() > 1)
                .minSelect(subOption.requiredSelectCount())
                .maxSelect(subOption.maxSelectCount())
                .sort(i)
                .isActive(true)
                .build();
            ProductOptionGroup savedOptionGroup = productOptionGroupJpaRepository.save(optionGroup);

            // ProductOption 저장
            if (subOption.subOptionItemDetailResponseList() != null) {
                for (int j = 0; j < subOption.subOptionItemDetailResponseList().size(); j++) {
                    BbqProductSubOptionResponse.SubOptionItemDetailResponse itemDetail =
                        subOption.subOptionItemDetailResponseList().get(j);

                    ProductOption productOption = ProductOption.builder()
                        .optionGroupId(savedOptionGroup.getId())
                        .name(itemDetail.itemTitle())
                        .additionalPrice(itemDetail.addPrice() != null ? itemDetail.addPrice() : 0)
                        .sort(j)
                        .isSoldOut(itemDetail.isSoldOut() != null ? itemDetail.isSoldOut() : false)
                        .isActive(!(itemDetail.isHidden() != null && itemDetail.isHidden()))
                        .build();
                    productOptionJpaRepository.save(productOption);
                }
            }
        }
    }
}

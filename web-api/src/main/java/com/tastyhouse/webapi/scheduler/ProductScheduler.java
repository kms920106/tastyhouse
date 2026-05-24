package com.tastyhouse.webapi.scheduler;

import com.tastyhouse.core.domain.product.application.ProductCommandService;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductOptionCommand;
import com.tastyhouse.core.domain.product.application.dto.command.SaveProductOptionGroupCommand;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductBbq;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
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
    private final ProductCommandService productCommandService;
    private final ProductQueryService productQueryService;

    //    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void crawlAndSaveProductOptions() {
        try {
            Optional<ProductBbq> productBbqOpt = productQueryService.findFirstBbqWithOptionsSyncPending();
            if (productBbqOpt.isEmpty()) {
                log.debug("옵션 동기화가 필요한 상품이 없습니다.");
                return;
            }

            ProductBbq productBbq = productBbqOpt.get();
            Long productId = productBbq.getProductId();
            Long bbqMenuId = productBbq.getBbqMenuId();

            log.info("상품 옵션 크롤링 시작: productId={}, bbqMenuId={}", productId, bbqMenuId);

            saveProductOptions(productId, bbqMenuId);

            productCommandService.markBbqOptionsSynced(productId);

            log.info("상품 옵션 저장 완료: productId={}", productId);
        } catch (Exception e) {
            log.error("상품 옵션 크롤링 중 오류 발생", e);
        }
    }

    private void saveProductOptions(Long productId, Long bbqMenuId) {
        List<BbqProductSubOptionResponse> subOptions = bbqService.getMenuSubOptions(bbqMenuId);

        if (subOptions.isEmpty()) {
            Product product = productQueryService.findProductById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: productId=" + productId));

            ProductOptionGroup savedOptionGroup = productCommandService.saveProductOptionGroup(
                new SaveProductOptionGroupCommand(productId, "기본 선택", null, false, false, 0, 1, 0, true)
            );

            productCommandService.saveProductOption(
                new SaveProductOptionCommand(savedOptionGroup.getId(), product.getName(), 0, 0, false, true)
            );

            log.info("서브 옵션이 없어 기본 옵션 그룹 및 옵션 저장: productId={}, 상품명={}", productId, product.getName());
            return;
        }

        for (int i = 0; i < subOptions.size(); i++) {
            BbqProductSubOptionResponse subOption = subOptions.get(i);

            ProductOptionGroup savedOptionGroup = productCommandService.saveProductOptionGroup(
                new SaveProductOptionGroupCommand(
                    productId,
                    subOption.subOptionTitle(),
                    null,
                    subOption.requiredSelectCount() != null && subOption.requiredSelectCount() > 0,
                    subOption.maxSelectCount() != null && subOption.maxSelectCount() > 1,
                    subOption.requiredSelectCount(),
                    subOption.maxSelectCount(),
                    i,
                    true
                )
            );

            if (subOption.subOptionItemDetailResponseList() != null) {
                for (int j = 0; j < subOption.subOptionItemDetailResponseList().size(); j++) {
                    BbqProductSubOptionResponse.SubOptionItemDetailResponse itemDetail =
                        subOption.subOptionItemDetailResponseList().get(j);

                    productCommandService.saveProductOption(
                        new SaveProductOptionCommand(
                            savedOptionGroup.getId(),
                            itemDetail.itemTitle(),
                            itemDetail.addPrice() != null ? itemDetail.addPrice() : 0,
                            j,
                            itemDetail.isSoldOut() != null ? itemDetail.isSoldOut() : false,
                            !(itemDetail.isHidden() != null && itemDetail.isHidden())
                        )
                    );
                }
            }
        }
    }
}

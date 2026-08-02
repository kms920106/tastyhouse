package com.tastyhouse.batch.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.infrastructure.product.query.ProductBbqSyncTargetResult;
import com.tastyhouse.batch.crawling.bbq.BbqOptionGroupRegistration;
import com.tastyhouse.batch.crawling.bbq.BbqOptionRegistration;
import com.tastyhouse.batch.crawling.bbq.BbqProductSyncService;
import com.tastyhouse.batch.crawling.bbq.BbqService;
import com.tastyhouse.batch.crawling.bbq.response.BbqProductSubOptionResponse;
import com.tastyhouse.batch.crawling.bbq.response.SubOptionItemDetailResponse;

/**
 * 상품 옵션 동기화 스케줄 진입점.
 *
 * <p>외부 BBQ API 호출(느린 I/O)은 트랜잭션 밖에서 수행하고, 저장은 {@link BbqProductSyncService}의
 * 트랜잭션 경계 안에서 옵션 그룹 단위로 처리한다.
 */
@Service
public class ProductSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(ProductSchedulerService.class);

    private final BbqService bbqService;
    private final BbqProductSyncService bbqProductSyncService;

    public ProductSchedulerService(BbqService bbqService, BbqProductSyncService bbqProductSyncService) {
        this.bbqService = bbqService;
        this.bbqProductSyncService = bbqProductSyncService;
    }

    public void crawlAndSaveProductOptions() {
        Optional<ProductBbqSyncTargetResult> targetOpt = bbqProductSyncService.findFirstOptionSyncTarget();
        if (targetOpt.isEmpty()) {
            log.debug("옵션 동기화가 필요한 상품이 없습니다.");
            return;
        }

        ProductBbqSyncTargetResult target = targetOpt.get();
        log.info("상품 옵션 크롤링 시작: productId={}, bbqMenuId={}", target.productId(), target.bbqMenuId());

        // 외부 API 호출은 트랜잭션 밖에서 먼저 끝내고, 저장은 아래 한 트랜잭션에서 원자적으로 처리한다.
        List<BbqOptionGroupRegistration> optionGroups = crawlOptionGroups(target);
        bbqProductSyncService.syncOptions(target.productId(), optionGroups);

        log.info("상품 옵션 저장 완료: productId={}", target.productId());
    }

    private List<BbqOptionGroupRegistration> crawlOptionGroups(ProductBbqSyncTargetResult target) {
        List<BbqProductSubOptionResponse> subOptions = bbqService.getMenuSubOptions(target.bbqMenuId());

        if (subOptions.isEmpty()) {
            log.info(
                "서브 옵션이 없어 기본 옵션 그룹 및 옵션 저장: productId={}, 상품명={}",
                target.productId(),
                target.productName()
            );
            return List.of(BbqOptionGroupRegistration.of(
                target.productId(),
                "기본 선택",
                false,
                false,
                0,
                1,
                0,
                List.of(BbqOptionRegistration.of(target.productName(), 0, false, true))
            ));
        }

        List<BbqOptionGroupRegistration> optionGroups = new ArrayList<>();
        for (int i = 0; i < subOptions.size(); i++) {
            BbqProductSubOptionResponse subOption = subOptions.get(i);

            optionGroups.add(BbqOptionGroupRegistration.of(
                target.productId(),
                subOption.subOptionTitle(),
                subOption.requiredSelectCount() != null && subOption.requiredSelectCount() > 0,
                subOption.maxSelectCount() != null && subOption.maxSelectCount() > 1,
                subOption.requiredSelectCount(),
                subOption.maxSelectCount(),
                i,
                toOptionRegistrations(subOption)
            ));
        }
        return optionGroups;
    }

    private List<BbqOptionRegistration> toOptionRegistrations(BbqProductSubOptionResponse subOption) {
        if (subOption.subOptionItemDetailResponseList() == null) {
            return List.of();
        }
        return subOption.subOptionItemDetailResponseList().stream()
            .map(this::toOptionRegistration)
            .toList();
    }

    private BbqOptionRegistration toOptionRegistration(SubOptionItemDetailResponse itemDetail) {
        return BbqOptionRegistration.of(
            itemDetail.itemTitle(),
            itemDetail.addPrice() != null ? itemDetail.addPrice() : 0,
            itemDetail.soldOut(),
            itemDetail.hidden()
        );
    }
}

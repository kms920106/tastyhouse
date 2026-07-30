package com.tastyhouse.batch.crawling.bbq;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.product.domain.model.ProductOptionGroup;
import com.tastyhouse.core.domain.product.domain.repository.ProductCategoryRepository;
import com.tastyhouse.core.domain.product.domain.service.ProductRegistrationService;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.infrastructure.product.query.ProductBbqSyncTargetResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;

/**
 * BBQ 크롤링 동기화 use case의 batch 전용 application 서비스.
 *
 * <p>트랜잭션 경계를 소유하고, 상품·카테고리·옵션·이미지 저장 불변식은 도메인 서비스
 * {@link ProductRegistrationService}에 위임한다. 동기화 대상 탐색은 infrastructure read 어댑터
 * {@link ProductQueryDao}가 담당한다(batch도 이 infra DAO를 소비한다).
 *
 * <p>같은 이름의 카테고리 재사용 판정은 화면 표시가 아니라 <b>중복 등록 방지 불변식</b>이므로 write 포트
 * {@link ProductCategoryRepository#findCategoriesByNameAndShopId}를 쓴다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class BbqProductSyncService {

    private final ProductRegistrationService productRegistrationService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductQueryDao productQueryDao;

    /**
     * 같은 가게에 같은 이름의 카테고리가 이미 있으면 재사용하고, 없으면 새로 등록한다.
     */
    public Long resolveCategoryId(Long shopId, String name, int sort) {
        List<ProductCategory> existing = productCategoryRepository.findCategoriesByNameAndShopId(name, shopId);
        if (!existing.isEmpty()) {
            return existing.getFirst().getId();
        }
        return productRegistrationService.createProductCategory(shopId, name, sort, true).getId();
    }

    /**
     * 크롤링한 메뉴 1건을 상품으로 등록하고, 이미지·BBQ 매핑을 함께 저장한다.
     * 이미지 업로드(외부 호출)는 호출자가 수행하고 업로드된 파일 식별자만 넘겨받는다.
     */
    public Long createCrawledProduct(BbqProductRegistration registration) {
        Product product = productRegistrationService.createProduct(
            registration.shopId(),
            registration.productCategoryId(),
            registration.name(),
            registration.description(),
            registration.originalPrice(),
            null,
            null,
            null,
            0,
            false,
            null,
            registration.soldOut(),
            true,
            registration.sort()
        );

        if (registration.imageFileId() != null) {
            productRegistrationService.saveProductImage(product.getId(), registration.imageFileId(), 0, true);
        }

        productRegistrationService.saveProductBbq(
            product.getId(),
            registration.bbqMenuId(),
            registration.bbqCategoryId(),
            false
        );

        return product.getId();
    }

    /**
     * 옵션 동기화가 필요한 상품 1건. 없으면 빈 Optional.
     */
    @Transactional(readOnly = true)
    public Optional<ProductBbqSyncTargetResult> findFirstOptionSyncTarget() {
        return productQueryDao.findFirstBbqSyncTarget();
    }

    /**
     * 크롤링해 온 옵션 그룹 전체와 동기화 완료 표시를 한 트랜잭션에서 함께 저장한다.
     *
     * <p>외부 BBQ API 호출(느린 I/O)은 호출자가 트랜잭션 밖에서 먼저 수행하고, 그 결과만 이 메서드에
     * 넘긴다. 그룹별로 트랜잭션을 나누지 않는 이유는 <b>일부 그룹만 저장된 채 동기화 미완료로 남는
     * 부분 반영을 막기 위함</b>이다(전환 이전의 단일 트랜잭션 원자성 유지).
     */
    public void syncOptions(Long productId, List<BbqOptionGroupRegistration> optionGroups) {
        for (BbqOptionGroupRegistration registration : optionGroups) {
            saveOptionGroupWithOptions(registration);
        }
        markOptionsSynced(productId);
    }

    /**
     * 옵션 그룹 1개와 그에 속한 옵션들을 함께 저장한다.
     */
    private void saveOptionGroupWithOptions(BbqOptionGroupRegistration registration) {
        ProductOptionGroup optionGroup = productRegistrationService.saveProductOptionGroup(
            registration.productId(),
            registration.name(),
            null,
            registration.required(),
            registration.multipleSelect(),
            registration.minSelect(),
            registration.maxSelect(),
            registration.sort(),
            true
        );

        List<BbqOptionRegistration> options = registration.options();
        for (int i = 0; i < options.size(); i++) {
            BbqOptionRegistration option = options.get(i);
            productRegistrationService.saveProductOption(
                optionGroup.getId(),
                option.name(),
                option.additionalPrice(),
                i,
                option.soldOut(),
                option.visible()
            );
        }
    }

    /**
     * 옵션 동기화 완료 표시.
     */
    private void markOptionsSynced(Long productId) {
        ProductId targetProductId = ProductId.of(productId);
        productRegistrationService.markBbqOptionsSynced(targetProductId);
    }
}

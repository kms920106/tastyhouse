package com.tastyhouse.batch.crawling.bbq;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.repository.ProductCategoryRepository;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.BbqCategoryId;
import com.tastyhouse.domain.product.vo.BbqMenuId;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;
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
public class BbqProductSyncService {

    private final ProductRegistrationService productRegistrationService;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductQueryDao productQueryDao;

    public BbqProductSyncService(
        ProductRegistrationService productRegistrationService,
        ProductCategoryRepository productCategoryRepository,
        ProductQueryDao productQueryDao
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productCategoryRepository = productCategoryRepository;
        this.productQueryDao = productQueryDao;
    }

    /**
     * 같은 가게에 같은 이름의 카테고리가 이미 있으면 재사용하고, 없으면 새로 등록한다.
     */
    public Long resolveCategoryId(Long shopId, String name, int sort) {
        ShopId targetShopId = ShopId.of(shopId);
        List<ProductCategory> existing = productCategoryRepository.findCategoriesByNameAndShopId(name, targetShopId);
        if (!existing.isEmpty()) {
            return existing.getFirst().getId();
        }
        return productRegistrationService.createProductCategory(targetShopId, name, null, sort, true).getId();
    }

    /**
     * 크롤링한 메뉴 1건을 상품으로 등록하고, 이미지·BBQ 매핑을 함께 저장한다.
     * 이미지 업로드(외부 호출)는 호출자가 수행하고 업로드된 파일 식별자만 넘겨받는다.
     */
    public Long createCrawledProduct(BbqProductRegistration registration) {
        Product product = productRegistrationService.createProduct(
            ShopId.of(registration.shopId()),
            ProductCategoryId.of(registration.productCategoryId()),
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
            registration.sort(),
            false, // 크롤링 메뉴는 평가 제외 여부를 알 수 없으므로 기본값(포함)으로 둔다
            null,  // 메뉴구성은 크롤링 원본에 없다
            false  // 1인분 여부도 크롤링 원본에 없다
        );

        if (registration.imageFileId() != null) {
            productRegistrationService.saveProductImage(
                product.getProductId(), UploadedFileId.of(registration.imageFileId()), 0, true
            );
        }

        productRegistrationService.saveProductBbq(
            product.getProductId(),
            BbqMenuId.of(registration.bbqMenuId()),
            BbqCategoryId.of(registration.bbqCategoryId()),
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
            ProductId.of(registration.productId()),
            registration.name(),
            null,
            registration.required(),
            registration.multipleSelect(),
            registration.minSelect(),
            registration.maxSelect(),
            registration.sort(),
            true,
            // 외부(BBQ) 메뉴 동기화는 일반 옵션그룹만 만든다 — 일회용컵 보증금은 규제 대상 사업자에게
            // 관리자가 플래그를 켠 뒤 점주가 직접 설정하는 값이라, 크롤링 결과로 생길 수 없다.
            ProductOptionGroupType.NORMAL
        );

        List<BbqOptionRegistration> options = registration.options();
        for (int i = 0; i < options.size(); i++) {
            BbqOptionRegistration option = options.get(i);
            productRegistrationService.saveProductOption(
                ProductOptionGroupId.of(optionGroup.getId()),
                option.name(),
                option.additionalPrice(),
                i,
                option.soldOut(),
                option.visible(),
                // 일반 옵션이므로 컵 개수·개인컵 할인이 없다(있으면 도메인 규칙이 거부한다).
                null,
                null
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

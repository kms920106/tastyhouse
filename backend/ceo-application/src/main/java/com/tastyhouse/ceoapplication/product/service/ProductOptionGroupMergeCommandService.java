package com.tastyhouse.ceoapplication.product.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeCommand;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeCommandUseCase;
import com.tastyhouse.ceoapplication.product.port.in.ProductOptionGroupMergeExclusionCreateCommand;
import com.tastyhouse.ceoapplication.shop.service.ShopOwnershipValidator;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeEntryType;
import com.tastyhouse.domain.product.model.ProductOptionGroupMergeExclusion;
import com.tastyhouse.domain.product.repository.ProductOptionGroupMergeExclusionRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupMergeService;
import com.tastyhouse.domain.product.service.ProductOptionGroupSignature;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 옵션그룹 합치기의 명령 측(합치기 실행 · 추천 제외).
 *
 * <p>불변식 본체는 {@link ProductOptionGroupMergeService}(도메인)가 소유하고, 이 서비스는 소유권
 * 검증과 트랜잭션 경계만 담당한다.
 *
 * <p><b>query DAO를 주입하지 않는다</b>(CQRS 교차 주입 금지) — 제외 요청의 서명 재계산도 DAO가 아니라
 * write 포트({@code ProductOptionRepository})에서 읽은 도메인 모델로 수행한다.
 */
@Service
@Transactional
public class ProductOptionGroupMergeCommandService implements ProductOptionGroupMergeCommandUseCase {

    private final ProductOptionGroupMergeService productOptionGroupMergeService;
    private final ProductOptionGroupMergeExclusionRepository exclusionRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator;

    public ProductOptionGroupMergeCommandService(
        ProductOptionGroupMergeService productOptionGroupMergeService,
        ProductOptionGroupMergeExclusionRepository exclusionRepository,
        ProductOptionRepository productOptionRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ProductOptionGroupOwnershipValidator productOptionGroupOwnershipValidator
    ) {
        this.productOptionGroupMergeService = productOptionGroupMergeService;
        this.exclusionRepository = exclusionRepository;
        this.productOptionRepository = productOptionRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.productOptionGroupOwnershipValidator = productOptionGroupOwnershipValidator;
    }

    /**
     * 합치기를 실행하고 <b>살아남은 기준 그룹 id</b>를 반환한다.
     *
     * <p>클라이언트는 이 id로 목록을 재조회한다 — 합치기는 여러 그룹·링크·옵션을 한꺼번에 바꾸므로
     * 부분 응답으로는 화면 상태를 맞출 수 없다.
     */
    @Override
    public Long mergeProductOptionGroups(ProductOptionGroupMergeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        Long baseOptionGroupId = command.baseOptionGroupId();
        List<Long> optionGroupIds = command.optionGroupIds();
        String entryType = command.entryType();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        // 경로의 기준 그룹이 이 가게 것인지 먼저 대조한다 — 도메인 서비스도 단일 가게 불변식을 다시
        // 검증하지만, 남의 가게 그룹 id는 도메인에 닿기 전에 404로 끊는 편이 정보 노출이 적다.
        productOptionGroupOwnershipValidator.validateOptionGroupShop(shopId, baseOptionGroupId);

        return productOptionGroupMergeService.merge(
            ShopId.of(shopId),
            ProductOptionGroupId.of(baseOptionGroupId),
            distinct(optionGroupIds).stream().map(ProductOptionGroupId::of).toList(),
            ProductOptionGroupMergeEntryType.from(entryType),
            CeoId.of(ceoId)
        );
    }

    /**
     * 추천 묶음을 영구 제외하고 생성된(또는 기존) 제외 id를 반환한다.
     *
     * <p><b>클라이언트가 보낸 서명을 그대로 믿지 않는다</b> — 함께 받은 {@code optionGroupIds}로 서명을
     * 재계산해 대조한다. 그러지 않으면 임의 문자열을 저장해 제외 테이블을 오염시킬 수 있고, 무엇보다
     * 목록을 띄워 둔 사이 옵션이 수정된 <b>낡은 토큰</b>이 엉뚱한 묶음을 영구히 숨긴다.
     *
     * <p>재클릭은 멱등이다 — 기존 행이 있으면 그 id를 그대로 돌려준다
     * ({@code UNIQUE (shop_id, group_signature)}가 최종 방어선).
     */
    @Override
    public Long excludeMergeSuggestion(ProductOptionGroupMergeExclusionCreateCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String signature = command.signature();
        List<Long> optionGroupIds = command.optionGroupIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<Long> targetIds = distinct(optionGroupIds);
        if (targetIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_TARGET_EMPTY);
        }
        validateSignature(shopId, signature, targetIds);

        return exclusionRepository.findByShopIdAndGroupSignature(ShopId.of(shopId), signature)
            .map(ProductOptionGroupMergeExclusion::getId)
            .orElseGet(() -> exclusionRepository.save(ProductOptionGroupMergeExclusion.of(
                ShopId.of(shopId),
                signature,
                CeoId.of(ceoId)
            )).getId());
    }

    /**
     * 요청의 모든 그룹이 <b>같은 서명</b>을 갖고 그 값이 클라이언트가 보낸 것과 일치하는지 확인한다.
     *
     * <p>하나라도 어긋나면 목록이 최신 상태가 아니라는 뜻이므로
     * {@code PRODUCT_OPTION_GROUP_MERGE_SIGNATURE_MISMATCH}로 거부하고 새로고침을 안내한다.
     */
    private void validateSignature(Long shopId, String signature, List<Long> optionGroupIds) {
        for (Long optionGroupId : optionGroupIds) {
            ProductOptionGroup group =
                productOptionGroupOwnershipValidator.loadOwnedOptionGroup(shopId, optionGroupId);
            List<ProductOption> options =
                productOptionRepository.findAllByOptionGroupId(group.getProductOptionGroupId());

            if (!Objects.equals(signature, ProductOptionGroupSignature.of(group, options))) {
                throw new BusinessException(ErrorCode.PRODUCT_OPTION_GROUP_MERGE_SIGNATURE_MISMATCH);
            }
        }
    }

    private List<Long> distinct(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }
        Set<Long> unique = new LinkedHashSet<>();
        ids.stream().filter(Objects::nonNull).forEach(unique::add);
        return List.copyOf(unique);
    }
}

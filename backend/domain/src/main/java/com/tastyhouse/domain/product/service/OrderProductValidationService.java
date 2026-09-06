package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 주문 라인의 상품·옵션 검증(도메인 서비스, product 컨텍스트 소유).
 *
 * <p>주문 접수가 상품에 대해 확인해야 하는 것 — <b>상품이 실재하는가·판매중지는 아닌가·선택한 옵션과
 * 그 그룹이 실재하는가</b> — 를 모아 수행하고, 주문 라인에 박제할 스냅샷({@link OrderProductSnapshot})을
 * 돌려준다.
 *
 * <p><b>왜 product가 소유하는가</b>: 이 검증은 상품 애그리거트의 규칙이므로 그 소유 컨텍스트가 판정해야
 * 한다. 과거에는 {@code OrderPlacementService}가 product의 모델 3개와 리포지토리 4개를 직접 주입해
 * 검증을 수행했는데, 그러면 "판매중지 상품은 주문할 수 없다" 같은 규칙이 order 안에서 재구현되어
 * product의 판매 정책이 바뀔 때 두 곳을 함께 고쳐야 한다. 지금은 order가 이 서비스 하나만 주입한다.
 *
 * <p><b>동작을 바꾸지 않는다</b> — 검증 순서(상품 로드 → 판매중지 → 대표 이미지 → 옵션 그룹 → 옵션)와
 * 에러코드({@code ORDER_PRODUCT_NOT_FOUND}·{@code ORDER_PRODUCT_SOLD_OUT}·
 * {@code ORDER_OPTION_GROUP_NOT_FOUND}·{@code ORDER_OPTION_NOT_FOUND})는 이관 전과 동일하다.
 * 에러코드 상수명이 {@code ORDER_} 접두인 것은 이 검증이 <b>주문 맥락</b>의 거절 사유이기 때문이며,
 * 응답 {@code code}는 프론트가 분기하는 wire 계약이라 이관을 이유로 바꾸지 않는다.
 *
 * <p><b>같은 트랜잭션 안의 동기 호출이다</b> — 주문 접수는 한 트랜잭션이어야 하므로 이 이관으로
 * 트랜잭션 경계가 바뀌지 않는다(이벤트로 바꾸지 않는다).
 *
 * <p><b>여기서 고친 두 가지 기존 결함</b>(메뉴 관리 도입과 무관하게 존재하던 것):
 * <ol>
 *   <li><b>옵션이 그 상품의 것인지 검증하지 않았다.</b> id로 존재만 확인하고 스냅샷에 담았는데
 *       금액이 옵션에서 오므로, 임의 상품의 저가 옵션을 주입해 결제 금액을 낮출 수 있었다.
 *       링크 테이블이 생겨 "이 그룹이 이 메뉴에 연결됐는가"를 처음으로 물을 수 있게 되어 함께 고친다.</li>
 *   <li><b>노출 여부를 검사하지 않았다.</b> 숨긴 메뉴·노출기간 밖 메뉴도 productId만 알면 주문됐다.</li>
 * </ol>
 *
 * <p><b>노출 검사를 품절 검사보다 앞에 둔다</b> — 스케줄 밖 메뉴에 "품절입니다"라고 답하면
 * 사용자를 오도한다(그 메뉴는 품절이 아니라 아직/이미 판매 시간이 아니다).
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ProductDomainConfig}가 담당한다.
 */
public class OrderProductValidationService {

    private final ProductRepository productRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionGroupLinkRepository productOptionGroupLinkRepository;
    private final ProductExposureHourRepository productExposureHourRepository;
    private final ProductExposureCalculator productExposureCalculator;
    private final CupDepositPolicy cupDepositPolicy;

    public OrderProductValidationService(
        ProductRepository productRepository,
        ProductPriceRepository productPriceRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator,
        CupDepositPolicy cupDepositPolicy
    ) {
        this.productRepository = productRepository;
        this.productPriceRepository = productPriceRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.productOptionGroupLinkRepository = productOptionGroupLinkRepository;
        this.productExposureHourRepository = productExposureHourRepository;
        this.productExposureCalculator = productExposureCalculator;
        this.cupDepositPolicy = cupDepositPolicy;
    }

    /**
     * 주문 라인들을 <b>요청 순서 그대로</b> 검증해 스냅샷 목록을 돌려준다. 첫 위반에서 즉시 실패한다.
     *
     * <p>순서를 보존하는 이유는 두 가지다 — 실패 시 어느 라인에서 걸렸는지가 요청과 대응되어야 하고,
     * 주문 라인 저장 순서가 화면 표시 순서가 되기 때문이다.
     */
    public List<OrderProductSnapshot> validate(
        List<OrderLineSelection> lines,
        OrderMethod orderMethod,
        LocalDateTime now
    ) {
        List<OrderProductSnapshot> snapshots = new ArrayList<>();
        for (OrderLineSelection line : lines) {
            snapshots.add(validateLine(line, orderMethod, now));
        }
        return snapshots;
    }

    private OrderProductSnapshot validateLine(OrderLineSelection line, OrderMethod orderMethod, LocalDateTime now) {
        Product product = productRepository.findById(ProductId.of(line.productId()))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_PRODUCT_NOT_FOUND,
                ErrorCode.ORDER_PRODUCT_NOT_FOUND.getDefaultMessage() + ": " + line.productId()));

        // 노출 검사를 품절보다 앞에 둔다 — 스케줄 밖 메뉴에 "품절입니다"는 사용자를 오도한다.
        if (!isExposed(product, now)) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_NOT_AVAILABLE,
                ErrorCode.ORDER_PRODUCT_NOT_AVAILABLE.getDefaultMessage() + ": " + product.getName());
        }

        if (product.isSoldOut()) {
            throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + product.getName());
        }

        UploadedFileId representativeImageFileId =
            productImageRepository.findRepresentativeImageFileId(product.getProductId());

        ProductPrice price = resolvePrice(product, line);

        return new OrderProductSnapshot(
            product.getProductId(),
            price != null ? price.getProductPriceId() : null,
            product.getName(),
            price != null ? price.getPriceName() : null,
            representativeImageFileId,
            line.quantity(),
            // 주문유형에 따라 해석된 채널 가격이 단가가 된다. 가격 행이 없는 메뉴(이관 이전 데이터)는
            // 기존 PRODUCT.original_price로 폴백해 주문이 끊기지 않게 한다.
            price != null ? price.resolvePrice(orderMethod) : product.getOriginalPrice(),
            product.getDiscountPrice(),
            validateOptions(line)
        );
    }

    /**
     * 손님이 고른 가격 행을 확정한다 — {@code priceId}가 없으면 기본 가격 행({@code sort} 최소)을 쓴다.
     *
     * <p><b>가격 행이 그 메뉴의 것인지 반드시 확인한다.</b> 금액이 이 행에서 나오므로, 확인하지 않으면
     * 남의 메뉴의 저가 가격 행을 실어 보내 결제 금액을 낮출 수 있다(옵션의 그룹 소속을 확인하는 것과
     * 같은 종류의 방어다).
     *
     * <p>가격 행이 하나도 없으면 {@code null}을 돌려주고 호출부가 기존 {@code PRODUCT.original_price}로
     * 폴백한다 — 이관 이전 데이터에서도 주문이 성립해야 하기 때문이다.
     */
    private ProductPrice resolvePrice(Product product, OrderLineSelection line) {
        List<ProductPrice> prices = productPriceRepository.findAllByProductId(product.getProductId());
        if (prices.isEmpty()) {
            return null;
        }
        if (line.priceId() == null) {
            return prices.getFirst();
        }
        return prices.stream()
            .filter(price -> line.priceId().equals(price.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_PRICE_NOT_FOUND));
    }

    /**
     * 지금 이 메뉴가 손님에게 노출되는지 판정한다. 공휴일 여부는 이 컨텍스트에서 알 수 없으므로
     * {@code false}로 둔다 — 노출기간의 {@code HOLIDAY} 요일 묶음은 손님 메뉴판 화면에서만 의미가 있고,
     * 주문 시점 검증은 기간·시간대 축만으로 충분하다.
     */
    private boolean isExposed(Product product, LocalDateTime now) {
        ProductExposureContext context = ProductExposureContext.of(
            product.isVisible(),
            product.getExposureStartDate(),
            product.getExposureEndDate(),
            productExposureHourRepository.findAllByProductId(product.getProductId()),
            now,
            false,
            false
        );
        return productExposureCalculator.calculate(context).exposed();
    }

    /**
     * 선택한 옵션을 검증해 주문에 박제할 스냅샷으로 만든다.
     *
     * <p><b>옵션그룹별 선택 개수까지 검증한다</b> — 과거에는 존재·소유·그룹소속만 보고
     * {@code required}·{@code minSelect}·{@code maxSelect}를 전혀 보지 않아, 프론트만 막고 서버는
     * <b>필수 옵션그룹을 비운 주문을 그대로 접수</b>했다. 손님 화면이 값을 내려받아 검사하고 있었을 뿐
     * 계약이 강제된 적이 없었다.
     *
     * <p><b>옵션의 품절·노출도 함께 본다</b> — 상품 레벨 {@code product.isSoldOut()}만 검사하고 있어
     * 숨긴/품절 옵션 id를 실어 보내면 접수됐다. 에러코드는 기존 것을 재사용해 wire 계약을 바꾸지 않는다:
     * 숨김은 {@code ORDER_OPTION_NOT_FOUND}(존재 여부를 노출하지 않는다), 품절은
     * {@code ORDER_PRODUCT_SOLD_OUT}(옵션명 부기).
     */
    private List<OrderProductOptionSnapshot> validateOptions(OrderLineSelection line) {
        ProductId productId = ProductId.of(line.productId());
        List<OrderProductOptionSnapshot> options = new ArrayList<>();
        Map<Long, ProductOptionGroup> selectedGroups = new LinkedHashMap<>();
        Map<Long, Integer> selectedCountByGroupId = new LinkedHashMap<>();
        for (OrderLineOptionSelection selected : line.selectedOptions()) {
            ProductOptionGroupId groupId = ProductOptionGroupId.of(selected.groupId());
            ProductOptionGroup optionGroup = productOptionGroupRepository
                .findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND));

            // ★ 그룹이 이 메뉴에 실제로 연결됐는지 확인한다. 없으면 남의 메뉴 옵션을 끌어다 쓰는 것이다.
            //    기존 ErrorCode 를 재사용해 wire 계약을 바꾸지 않는다.
            if (!productOptionGroupLinkRepository.existsByProductIdAndOptionGroupId(productId, groupId)) {
                throw new ResourceNotFoundException(ErrorCode.ORDER_OPTION_GROUP_NOT_FOUND);
            }

            ProductOption option = productOptionRepository
                .findById(ProductOptionId.of(selected.optionId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND));

            // ★ 옵션이 그 그룹의 것인지 확인한다. 금액이 옵션에서 오므로 이 검사가 없으면
            //    임의 상품의 저가 옵션을 주입해 결제 금액을 낮출 수 있다.
            if (!option.getOptionGroupId().equals(groupId)) {
                throw new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND);
            }

            // 숨긴 옵션은 손님에게 존재하지 않는 것과 같다 — 존재 여부를 노출하지 않도록 NOT_FOUND로 답한다.
            if (!option.isVisible()) {
                throw new ResourceNotFoundException(ErrorCode.ORDER_OPTION_NOT_FOUND);
            }
            if (option.isSoldOut()) {
                throw new BusinessException(ErrorCode.ORDER_PRODUCT_SOLD_OUT,
                    ErrorCode.ORDER_PRODUCT_SOLD_OUT.getDefaultMessage() + ": " + option.getName());
            }

            selectedGroups.putIfAbsent(groupId.value(), optionGroup);
            selectedCountByGroupId.merge(groupId.value(), 1, Integer::sum);

            // 보증금 금액의 진실원은 옵션 행의 cupCount × 정책 요율이다 — 클라이언트가 보낸 값을
            // 쓰지 않는다. 일반 옵션은 cupCount가 없으므로 0원이 되어 기존 동작이 그대로 유지된다.
            int depositAmount = optionGroup.isCupDeposit()
                ? cupDepositPolicy.depositAmountOf(option.getCupCount())
                : 0;

            options.add(new OrderProductOptionSnapshot(
                optionGroup.getProductOptionGroupId(),
                optionGroup.getName(),
                option.getProductOptionId(),
                option.getName(),
                option.getAdditionalPrice(),
                optionGroup.getGroupType().name(),
                optionGroup.isCupDeposit() ? option.getCupCount() : null,
                depositAmount,
                // 개인컵 할인도 서버 값이 진실원이다 — 보증금 그룹 밖의 값은 설정 단계에서 이미
                // 거부되므로(CupDepositOptionRule) 여기서는 그대로 옮겨 담기만 한다.
                option.getPersonalCupDiscountAmount() != null ? option.getPersonalCupDiscountAmount() : 0
            ));
        }

        validateSelectCounts(productId, selectedGroups, selectedCountByGroupId);
        return options;
    }

    /**
     * 옵션그룹별 선택 개수가 그룹의 제약을 만족하는지 검증한다.
     *
     * <p><b>선택하지 않은 필수 그룹도 대상이다</b> — 선택된 그룹만 순회하면 "필수 그룹을 통째로 비운"
     * 주문이 그대로 통과한다. 그래서 이 메뉴에 연결된 <b>전체 옵션그룹</b>을 링크로 조회해,
     * 선택 수가 0인 필수 그룹까지 함께 본다.
     */
    private void validateSelectCounts(
        ProductId productId,
        Map<Long, ProductOptionGroup> selectedGroups,
        Map<Long, Integer> selectedCountByGroupId
    ) {
        for (ProductOptionGroupLink link : productOptionGroupLinkRepository.findAllByProductId(productId)) {
            Long groupId = link.getOptionGroupId().value();
            ProductOptionGroup group = selectedGroups.get(groupId);
            if (group == null) {
                group = productOptionGroupRepository.findById(ProductOptionGroupId.of(groupId)).orElse(null);
            }
            // 숨긴 옵션그룹은 손님 메뉴판에 없으므로 선택하지 않은 것이 정상이다.
            if (group == null || !group.isVisible()) {
                continue;
            }

            int selectedCount = selectedCountByGroupId.getOrDefault(groupId, 0);
            validateSelectCount(group, selectedCount);
        }
    }

    private void validateSelectCount(ProductOptionGroup group, int selectedCount) {
        if (group.isRequired() && selectedCount == 0) {
            throw new BusinessException(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }
        // 선택하지 않은 비필수 그룹에는 minSelect 하한을 적용하지 않는다 — "고르지 않음"이 유효한 선택이다.
        if (selectedCount == 0) {
            return;
        }

        Integer minSelect = group.getMinSelect();
        if (minSelect != null && selectedCount < minSelect) {
            throw new BusinessException(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }

        Integer maxSelect = group.getMaxSelect();
        if (maxSelect != null && selectedCount > maxSelect) {
            throw new BusinessException(ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID,
                ErrorCode.ORDER_OPTION_SELECT_COUNT_INVALID.getDefaultMessage() + ": " + group.getName());
        }
    }
}

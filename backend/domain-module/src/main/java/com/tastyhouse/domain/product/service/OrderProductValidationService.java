package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductExposureHourRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
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
    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionGroupLinkRepository productOptionGroupLinkRepository;
    private final ProductExposureHourRepository productExposureHourRepository;
    private final ProductExposureCalculator productExposureCalculator;

    public OrderProductValidationService(
        ProductRepository productRepository,
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductImageRepository productImageRepository,
        ProductOptionGroupLinkRepository productOptionGroupLinkRepository,
        ProductExposureHourRepository productExposureHourRepository,
        ProductExposureCalculator productExposureCalculator
    ) {
        this.productRepository = productRepository;
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productImageRepository = productImageRepository;
        this.productOptionGroupLinkRepository = productOptionGroupLinkRepository;
        this.productExposureHourRepository = productExposureHourRepository;
        this.productExposureCalculator = productExposureCalculator;
    }

    /**
     * 주문 라인들을 <b>요청 순서 그대로</b> 검증해 스냅샷 목록을 돌려준다. 첫 위반에서 즉시 실패한다.
     *
     * <p>순서를 보존하는 이유는 두 가지다 — 실패 시 어느 라인에서 걸렸는지가 요청과 대응되어야 하고,
     * 주문 라인 저장 순서가 화면 표시 순서가 되기 때문이다.
     */
    public List<OrderProductSnapshot> validate(List<OrderLineSelection> lines, LocalDateTime now) {
        List<OrderProductSnapshot> snapshots = new ArrayList<>();
        for (OrderLineSelection line : lines) {
            snapshots.add(validateLine(line, now));
        }
        return snapshots;
    }

    private OrderProductSnapshot validateLine(OrderLineSelection line, LocalDateTime now) {
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

        return new OrderProductSnapshot(
            product.getProductId(),
            product.getName(),
            representativeImageFileId,
            line.quantity(),
            product.getOriginalPrice(),
            product.getDiscountPrice(),
            validateOptions(line)
        );
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

    private List<OrderProductOptionSnapshot> validateOptions(OrderLineSelection line) {
        ProductId productId = ProductId.of(line.productId());
        List<OrderProductOptionSnapshot> options = new ArrayList<>();
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

            options.add(new OrderProductOptionSnapshot(
                optionGroup.getProductOptionGroupId(),
                optionGroup.getName(),
                option.getProductOptionId(),
                option.getName(),
                option.getAdditionalPrice()
            ));
        }
        return options;
    }
}

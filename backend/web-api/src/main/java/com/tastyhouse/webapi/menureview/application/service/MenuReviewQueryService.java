package com.tastyhouse.webapi.menureview.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.model.Order;
import com.tastyhouse.domain.order.repository.OrderRepository;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.menureview.query.MenuReviewListItemResult;
import com.tastyhouse.infrastructure.menureview.query.MenuReviewQueryDao;
import com.tastyhouse.infrastructure.menureview.query.MenuReviewWritableItemResult;
import com.tastyhouse.webapi.menureview.adapter.in.web.response.MenuReviewListItemResponse;
import com.tastyhouse.webapi.menureview.adapter.in.web.response.MenuReviewWritableItemResponse;
import com.tastyhouse.webapi.menureview.application.port.in.MenuReviewQueryUseCase;

/**
 * 메뉴 평가 조회 서비스(CQRS query 측).
 *
 * <p>{@link MenuReviewQueryDao}를 주입해 조회하고 Result → Response 변환을 private 매퍼로 조립한다.
 *
 * <p>평가 가능 메뉴 목록은 <b>주문 소유권을 먼저 검증</b>한다 — 생략하면 남의 주문 내역(메뉴 구성)이
 * 통째로 새는 IDOR이 된다. 그 검증에는 애그리거트 단건 로드가 필요하므로 write 포트
 * {@code OrderRepository#findById}를 쓴다(표현용 투영이 아니라 접근 판정 입력이다).
 */
@Service
@Transactional(readOnly = true)
public class MenuReviewQueryService implements MenuReviewQueryUseCase {

    private final MenuReviewQueryDao menuReviewQueryDao;
    private final OrderRepository orderRepository;

    public MenuReviewQueryService(MenuReviewQueryDao menuReviewQueryDao, OrderRepository orderRepository) {
        this.menuReviewQueryDao = menuReviewQueryDao;
        this.orderRepository = orderRepository;
    }

    /**
     * 한 주문의 평가 가능 메뉴 목록 — 평가 제외 상품({@code is_rating_excluded = 1})은 담기지 않는다.
     * 이미 평가한 항목도 기존 값과 함께 내려준다.
     */
    @Override
    public List<MenuReviewWritableItemResponse> findWritableItems(Long orderId, Long memberId) {
        Order order = orderRepository.findById(OrderId.of(orderId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(MemberId.of(memberId))) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }

        return menuReviewQueryDao.findWritableItemsByOrderId(orderId).stream()
            .map(this::toWritableItemResponse)
            .toList();
    }

    /**
     * 상품별 메뉴 평가 목록(공개 조회) — 숨김 제외, 최신순.
     */
    @Override
    public PaginationResponse<MenuReviewListItemResponse> findByProductId(Long productId, int page, int size) {
        PageResult<MenuReviewListItemResult> pageResult =
            menuReviewQueryDao.findVisibleByProductId(productId, PageQuery.of(page, size));

        return PaginationResponse.from(PageResult.of(
            pageResult.content().stream().map(this::toListItemResponse).toList(),
            pageResult.totalElements(),
            pageResult.page(),
            pageResult.size()
        ));
    }

    private MenuReviewWritableItemResponse toWritableItemResponse(MenuReviewWritableItemResult result) {
        return MenuReviewWritableItemResponse.from(
            result.orderProductId(),
            result.productId(),
            result.productName(),
            result.productImageUrl(),
            result.menuReviewId(),
            result.rating(),
            result.comment()
        );
    }

    private MenuReviewListItemResponse toListItemResponse(MenuReviewListItemResult result) {
        return MenuReviewListItemResponse.from(
            result.id(),
            result.memberNickname(),
            result.memberProfileImageUrl(),
            result.rating(),
            result.comment(),
            result.createdAt()
        );
    }
}

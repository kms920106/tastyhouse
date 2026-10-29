package com.tastyhouse.application.order.service;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.tastyhouse.application.order.port.out.OrderListPaymentFilter;
import com.tastyhouse.application.order.port.out.OrderQueryPort;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderListQueryServiceTest {

    private static final Long MEMBER_ID = 7L;

    @Test
    @DisplayName("주문 목록은 완료·취소 결제와, 결제 대기 중에서는 현장결제 수단만 포함하도록 조회한다")
    void getOrderList_includesSettledAndOnSitePendingPayments() {
        OrderQueryPort orderQueryPort = mock(OrderQueryPort.class);
        when(orderQueryPort.findOrders(eq(MEMBER_ID), any(), any())).thenReturn(PageResult.of(List.of(), 0L, 0, 10));
        OrderListQueryService service = new OrderListQueryService(orderQueryPort);

        service.getOrderList(MEMBER_ID, 0, 10);

        ArgumentCaptor<OrderListPaymentFilter> filterCaptor = ArgumentCaptor.forClass(OrderListPaymentFilter.class);
        ArgumentCaptor<PageQuery> pageCaptor = ArgumentCaptor.forClass(PageQuery.class);
        verify(orderQueryPort).findOrders(eq(MEMBER_ID), filterCaptor.capture(), pageCaptor.capture());

        OrderListPaymentFilter filter = filterCaptor.getValue();
        assertThat(filter.settledStatuses()).containsExactlyInAnyOrder("COMPLETED", "CANCELLED");
        assertThat(filter.awaitingStatus()).isEqualTo("PENDING");
        assertThat(filter.awaitingMethods()).containsExactlyInAnyOrder("CASH_ON_SITE", "CARD_ON_SITE");
        assertThat(pageCaptor.getValue().page()).isZero();
        assertThat(pageCaptor.getValue().size()).isEqualTo(10);
    }
}

package com.tastyhouse.webapi.order.response;

import java.util.List;

import com.tastyhouse.webapi.member.response.OrderListItemResponse;

public record OrderListPageResult(List<OrderListItemResponse> content, int page, int size, long totalElements) {
}

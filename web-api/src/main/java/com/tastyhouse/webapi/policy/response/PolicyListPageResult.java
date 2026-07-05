package com.tastyhouse.webapi.policy.response;

import java.util.List;

public record PolicyListPageResult(List<PolicyListItemResponse> content, int page, int size, long totalElements) {
    public static PolicyListPageResult of(List<PolicyListItemResponse> content, int page, int size, long totalElements) {
        return new PolicyListPageResult(content, page, size, totalElements);
    }
}

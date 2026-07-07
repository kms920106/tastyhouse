package com.tastyhouse.webapi.search.request;

import jakarta.validation.constraints.NotBlank;

public record SearchKeywordRequest(
    @NotBlank
    String query
) {
}

package com.tastyhouse.external.crawling.bbq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BBQ 메뉴 카테고리 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BbqMenuCategoryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("categoryImageUrl")
    private String categoryImageUrl;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("isFullSize")
    private Boolean fullSize;
}

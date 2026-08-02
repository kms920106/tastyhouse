package com.tastyhouse.external.crawling.bbq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * BBQ 메뉴 카테고리 응답 DTO
 */
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

    public BbqMenuCategoryResponse() {
    }

    public BbqMenuCategoryResponse(Long id, String categoryName, String categoryImageUrl, Integer priority, Boolean fullSize) {
        this.id = id;
        this.categoryName = categoryName;
        this.categoryImageUrl = categoryImageUrl;
        this.priority = priority;
        this.fullSize = fullSize;
    }

    public Long getId() {
        return this.id;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public Boolean getFullSize() {
        return this.fullSize;
    }
}

package com.tastyhouse.external.crawling.bbq.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * BBQ 메뉴 서브 옵션 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BbqMenuSubOptionResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("subOptionTitle")
    private String subOptionTitle;

    @JsonProperty("requiredSelectCount")
    private Integer requiredSelectCount;

    @JsonProperty("maxSelectCount")
    private Integer maxSelectCount;

    @JsonProperty("subOptionItemDetailResponseList")
    private List<SubOptionItemDetailResponse> subOptionItemDetailResponseList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubOptionItemDetailResponse {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("itemTitle")
        private String itemTitle;

        @JsonProperty("addPrice")
        private Integer addPrice;

        @JsonProperty("isSoldOut")
        private Boolean soldOut;

        @JsonProperty("hidden")
        private Boolean hidden;
    }
}

package com.tastyhouse.external.crawling.bbq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
        private Boolean isSoldOut;

        @JsonProperty("isHidden")
        private Boolean isHidden;
    }
}

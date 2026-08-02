package com.tastyhouse.external.crawling.bbq.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * BBQ 메뉴 서브 옵션 응답 DTO
 */
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

    public BbqMenuSubOptionResponse() {
    }

    public BbqMenuSubOptionResponse(
        Long id,
        String subOptionTitle,
        Integer requiredSelectCount,
        Integer maxSelectCount,
        List<SubOptionItemDetailResponse> subOptionItemDetailResponseList
    ) {
        this.id = id;
        this.subOptionTitle = subOptionTitle;
        this.requiredSelectCount = requiredSelectCount;
        this.maxSelectCount = maxSelectCount;
        this.subOptionItemDetailResponseList = subOptionItemDetailResponseList;
    }

    public Long getId() {
        return this.id;
    }

    public String getSubOptionTitle() {
        return this.subOptionTitle;
    }

    public Integer getRequiredSelectCount() {
        return this.requiredSelectCount;
    }

    public Integer getMaxSelectCount() {
        return this.maxSelectCount;
    }

    public List<SubOptionItemDetailResponse> getSubOptionItemDetailResponseList() {
        return this.subOptionItemDetailResponseList;
    }

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

        public SubOptionItemDetailResponse() {
        }

        public SubOptionItemDetailResponse(Long id, String itemTitle, Integer addPrice, Boolean soldOut, Boolean hidden) {
            this.id = id;
            this.itemTitle = itemTitle;
            this.addPrice = addPrice;
            this.soldOut = soldOut;
            this.hidden = hidden;
        }

        public Long getId() {
            return this.id;
        }

        public String getItemTitle() {
            return this.itemTitle;
        }

        public Integer getAddPrice() {
            return this.addPrice;
        }

        public Boolean getSoldOut() {
            return this.soldOut;
        }

        public Boolean getHidden() {
            return this.hidden;
        }
    }
}

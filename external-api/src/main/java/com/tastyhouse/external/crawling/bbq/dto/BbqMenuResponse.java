package com.tastyhouse.external.crawling.bbq.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BBQ 메뉴 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BbqMenuResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("menuName")
    private String menuName;

    @JsonProperty("description")
    private String description;

    @JsonProperty("menuImageUrl")
    private String menuImageUrl;

    @JsonProperty("menuType")
    private String menuType;

    @JsonProperty("menuPrice")
    private Integer menuPrice;

    @JsonProperty("addPrice")
    private Integer addPrice;

    @JsonProperty("nutrient")
    private Nutrient nutrient;

    @JsonProperty("allergy")
    private String allergy;

    @JsonProperty("origin")
    private List<Origin> origin;

    @JsonProperty("canDeliver")
    private Boolean canDeliver;

    @JsonProperty("canTakeout")
    private Boolean canTakeout;

    @JsonProperty("isAdultOnly")
    private Boolean adultOnly;

    @JsonProperty("isSoldOut")
    private Boolean soldOut;

    @JsonProperty("weightList")
    private List<Weight> weightList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Nutrient {
        @JsonProperty("calorie")
        private Integer calorie;

        @JsonProperty("sugars")
        private Integer sugars;

        @JsonProperty("protein")
        private Integer protein;

        @JsonProperty("saturatedFat")
        private Integer saturatedFat;

        @JsonProperty("natrium")
        private Integer natrium;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Origin {
        @JsonProperty("name")
        private String name;

        @JsonProperty("region")
        private String region;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Weight {
        @JsonProperty("subOptionId")
        private Long subOptionId;

        @JsonProperty("subOptionItemId")
        private Long subOptionItemId;

        @JsonProperty("weight")
        private String weight;
    }
}

package com.tastyhouse.external.crawling.bbq.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * BBQ 메뉴 응답 DTO
 */
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

    public BbqMenuResponse() {
    }

    public BbqMenuResponse(
        Long id,
        String menuName,
        String description,
        String menuImageUrl,
        String menuType,
        Integer menuPrice,
        Integer addPrice,
        Nutrient nutrient,
        String allergy,
        List<Origin> origin,
        Boolean canDeliver,
        Boolean canTakeout,
        Boolean adultOnly,
        Boolean soldOut,
        List<Weight> weightList
    ) {
        this.id = id;
        this.menuName = menuName;
        this.description = description;
        this.menuImageUrl = menuImageUrl;
        this.menuType = menuType;
        this.menuPrice = menuPrice;
        this.addPrice = addPrice;
        this.nutrient = nutrient;
        this.allergy = allergy;
        this.origin = origin;
        this.canDeliver = canDeliver;
        this.canTakeout = canTakeout;
        this.adultOnly = adultOnly;
        this.soldOut = soldOut;
        this.weightList = weightList;
    }

    public Long getId() {
        return this.id;
    }

    public String getMenuName() {
        return this.menuName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getMenuImageUrl() {
        return this.menuImageUrl;
    }

    public String getMenuType() {
        return this.menuType;
    }

    public Integer getMenuPrice() {
        return this.menuPrice;
    }

    public Integer getAddPrice() {
        return this.addPrice;
    }

    public Nutrient getNutrient() {
        return this.nutrient;
    }

    public String getAllergy() {
        return this.allergy;
    }

    public List<Origin> getOrigin() {
        return this.origin;
    }

    public Boolean getCanDeliver() {
        return this.canDeliver;
    }

    public Boolean getCanTakeout() {
        return this.canTakeout;
    }

    public Boolean getAdultOnly() {
        return this.adultOnly;
    }

    public Boolean getSoldOut() {
        return this.soldOut;
    }

    public List<Weight> getWeightList() {
        return this.weightList;
    }

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

        public Nutrient() {
        }

        public Nutrient(Integer calorie, Integer sugars, Integer protein, Integer saturatedFat, Integer natrium) {
            this.calorie = calorie;
            this.sugars = sugars;
            this.protein = protein;
            this.saturatedFat = saturatedFat;
            this.natrium = natrium;
        }

        public Integer getCalorie() {
            return this.calorie;
        }

        public Integer getSugars() {
            return this.sugars;
        }

        public Integer getProtein() {
            return this.protein;
        }

        public Integer getSaturatedFat() {
            return this.saturatedFat;
        }

        public Integer getNatrium() {
            return this.natrium;
        }
    }

    public static class Origin {
        @JsonProperty("name")
        private String name;

        @JsonProperty("region")
        private String region;

        public Origin() {
        }

        public Origin(String name, String region) {
            this.name = name;
            this.region = region;
        }

        public String getName() {
            return this.name;
        }

        public String getRegion() {
            return this.region;
        }
    }

    public static class Weight {
        @JsonProperty("subOptionId")
        private Long subOptionId;

        @JsonProperty("subOptionItemId")
        private Long subOptionItemId;

        @JsonProperty("weight")
        private String weight;

        public Weight() {
        }

        public Weight(Long subOptionId, Long subOptionItemId, String weight) {
            this.subOptionId = subOptionId;
            this.subOptionItemId = subOptionItemId;
            this.weight = weight;
        }

        public Long getSubOptionId() {
            return this.subOptionId;
        }

        public Long getSubOptionItemId() {
            return this.subOptionItemId;
        }

        public String getWeight() {
            return this.weight;
        }
    }
}

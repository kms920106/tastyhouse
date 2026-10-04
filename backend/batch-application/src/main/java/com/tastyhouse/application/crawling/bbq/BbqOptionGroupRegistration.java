package com.tastyhouse.application.crawling.bbq;

import java.util.List;

public record BbqOptionGroupRegistration(
    Long productId,
    String name,
    boolean required,
    boolean multipleSelect,
    Integer minSelect,
    Integer maxSelect,
    Integer sort,
    List<BbqOptionRegistration> options
) {

    public static BbqOptionGroupRegistration of(
        Long productId,
        String name,
        boolean required,
        boolean multipleSelect,
        Integer minSelect,
        Integer maxSelect,
        Integer sort,
        List<BbqOptionRegistration> options
    ) {
        return new BbqOptionGroupRegistration(
            productId,
            name,
            required,
            multipleSelect,
            minSelect,
            maxSelect,
            sort,
            options == null ? List.of() : options
        );
    }
}

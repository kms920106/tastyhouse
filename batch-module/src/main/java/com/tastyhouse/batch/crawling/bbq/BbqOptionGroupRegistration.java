package com.tastyhouse.batch.crawling.bbq;

import java.util.List;

/**
 * BBQ 크롤링 옵션 그룹 등록 입력. 그룹과 그에 속한 옵션들을 한 번에 담는 batch 전용 입력 record다.
 */
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

package com.tastyhouse.application.faq.port.out.write;

import java.util.Optional;

public interface FaqCategoryStatePort {
    Optional<FaqCategoryState> findById(Long id);

    boolean existsActiveItemsByCategoryId(Long faqCategoryId);

    FaqCategoryState save(FaqCategoryState state);
}

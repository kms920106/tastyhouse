package com.tastyhouse.application.faq.store;

import com.tastyhouse.application.faq.port.out.write.FaqCategoryState;
import com.tastyhouse.domain.faq.model.FaqCategory;

final class FaqCategoryStateMapper {
    private FaqCategoryStateMapper() {
    }

    static FaqCategory toDomain(FaqCategoryState state) {
        return FaqCategory.reconstitute(
            state.id(),
            state.name(),
            state.sort(),
            state.visible(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static FaqCategoryState toState(FaqCategory faqCategory) {
        return new FaqCategoryState(
            faqCategory.getId(),
            faqCategory.getName(),
            faqCategory.getSort(),
            faqCategory.isVisible(),
            faqCategory.isDeleted(),
            faqCategory.getCreatedAt(),
            faqCategory.getUpdatedAt()
        );
    }
}

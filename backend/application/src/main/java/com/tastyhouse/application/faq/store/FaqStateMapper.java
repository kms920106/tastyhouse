package com.tastyhouse.application.faq.store;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.application.faq.port.out.write.FaqState;

final class FaqStateMapper {
    private FaqStateMapper() {
    }

    static Faq toDomain(FaqState state) {
        return Faq.reconstitute(
            state.id(),
            state.faqCategoryId() == null ? null : FaqCategoryId.of(state.faqCategoryId()),
            state.question(),
            state.answer(),
            state.sort(),
            state.visible(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static FaqState toState(Faq faq) {
        return new FaqState(
            faq.getId(),
            faq.getFaqCategoryId() == null ? null : faq.getFaqCategoryId().value(),
            faq.getQuestion(),
            faq.getAnswer(),
            faq.getSort(),
            faq.isVisible(),
            faq.isDeleted(),
            faq.getCreatedAt(),
            faq.getUpdatedAt()
        );
    }
}

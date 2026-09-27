package com.tastyhouse.infrastructure.faq.persistence;

import com.tastyhouse.application.faq.port.out.write.FaqState;

final class FaqMapper {
    private FaqMapper() {
    }

    static FaqState toState(FaqJpaEntity entity) {
        return new FaqState(
            entity.getId(),
            entity.getFaqCategoryId(),
            entity.getQuestion(),
            entity.getAnswer(),
            entity.getSort(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static FaqJpaEntity toEntity(FaqState state) {
        return FaqJpaEntity.create(
            state.faqCategoryId(),
            state.question(),
            state.answer(),
            state.sort(),
            state.visible(),
            state.deleted()
        );
    }

    static void applyChanges(FaqJpaEntity entity, FaqState state) {
        entity.applyChanges(
            state.faqCategoryId(),
            state.question(),
            state.answer(),
            state.sort(),
            state.visible(),
            state.deleted()
        );
    }
}

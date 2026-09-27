package com.tastyhouse.application.faq.store;

import java.util.Optional;

import com.tastyhouse.application.faq.port.out.write.FaqCategoryStatePort;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

public class FaqCategoryStore implements FaqCategoryRepository {
    private final FaqCategoryStatePort faqCategoryStatePort;

    public FaqCategoryStore(FaqCategoryStatePort faqCategoryStatePort) {
        this.faqCategoryStatePort = faqCategoryStatePort;
    }

    @Override
    public Optional<FaqCategory> findById(FaqCategoryId faqCategoryId) {
        if (faqCategoryId == null) {
            return Optional.empty();
        }
        return faqCategoryStatePort.findById(faqCategoryId.value()).map(FaqCategoryStateMapper::toDomain);
    }

    @Override
    public boolean existsActiveItemsByCategoryId(FaqCategoryId faqCategoryId) {
        return faqCategoryStatePort.existsActiveItemsByCategoryId(faqCategoryId.value());
    }

    @Override
    public FaqCategory save(FaqCategory faqCategory) {
        return FaqCategoryStateMapper.toDomain(faqCategoryStatePort.save(FaqCategoryStateMapper.toState(faqCategory)));
    }
}

package com.tastyhouse.domain.faq.repository;

import java.util.Optional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqId;

public interface FaqRepository {
    Optional<Faq> findById(FaqId faqId);

    Faq save(Faq faq);
}

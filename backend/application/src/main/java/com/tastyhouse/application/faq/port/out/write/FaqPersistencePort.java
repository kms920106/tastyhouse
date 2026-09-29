package com.tastyhouse.application.faq.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqId;

public interface FaqPersistencePort {
    Optional<Faq> findById(FaqId faqId);

    Faq save(Faq faq);
}

package com.tastyhouse.application.faq.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqId;

public interface FaqLoadPort {

    Optional<Faq> findActiveById(FaqId faqId);
}

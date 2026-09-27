package com.tastyhouse.application.faq.store;

import java.util.Optional;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqId;
import com.tastyhouse.application.faq.port.out.write.FaqStatePort;

public class FaqStore implements FaqRepository {
    private final FaqStatePort faqStatePort;

    public FaqStore(FaqStatePort faqStatePort) {
        this.faqStatePort = faqStatePort;
    }

    @Override
    public Optional<Faq> findById(FaqId faqId) {
        if (faqId == null) {
            return Optional.empty();
        }
        return faqStatePort.findById(faqId.value()).map(FaqStateMapper::toDomain);
    }

    @Override
    public Faq save(Faq faq) {
        return FaqStateMapper.toDomain(faqStatePort.save(FaqStateMapper.toState(faq)));
    }
}

package com.tastyhouse.application.faq.port.out.write;

import com.tastyhouse.domain.faq.model.Faq;

public interface FaqSavePort {

    Faq save(Faq faq);
}

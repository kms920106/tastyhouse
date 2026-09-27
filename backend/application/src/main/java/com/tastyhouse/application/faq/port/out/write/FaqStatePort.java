package com.tastyhouse.application.faq.port.out.write;

import java.util.Optional;

public interface FaqStatePort {
    Optional<FaqState> findById(Long id);

    FaqState save(FaqState state);
}

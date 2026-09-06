package com.tastyhouse.domain.ceo.repository;

import java.util.Optional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.vo.CeoId;

public interface CeoRepository {
    Optional<Ceo> findById(CeoId id);

    Optional<Ceo> findByUsername(String username);

    boolean existsByUsername(String username);

    Ceo save(Ceo ceo);
}

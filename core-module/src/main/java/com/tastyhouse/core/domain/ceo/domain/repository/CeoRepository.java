package com.tastyhouse.core.domain.ceo.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.ceo.domain.model.Ceo;
import com.tastyhouse.core.domain.ceo.domain.vo.CeoId;

public interface CeoRepository {

    Optional<Ceo> findById(CeoId ceoId);

    List<Ceo> findAll();

    Optional<Ceo> findByUsername(String username);

    boolean existsByUsername(String username);

    Ceo save(Ceo ceo);
}

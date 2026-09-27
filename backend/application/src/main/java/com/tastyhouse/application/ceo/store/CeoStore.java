package com.tastyhouse.application.ceo.store;

import java.util.Optional;

import com.tastyhouse.domain.ceo.model.Ceo;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.application.ceo.port.out.write.CeoStatePort;

public class CeoStore implements CeoRepository {
    private final CeoStatePort ceoStatePort;

    public CeoStore(CeoStatePort ceoStatePort) {
        this.ceoStatePort = ceoStatePort;
    }

    @Override
    public Optional<Ceo> findById(CeoId id) {
        return ceoStatePort.findById(id.value()).map(CeoStateMapper::toDomain);
    }

    @Override
    public Optional<Ceo> findByUsername(String username) {
        return ceoStatePort.findByUsername(username).map(CeoStateMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return ceoStatePort.existsByUsername(username);
    }

    @Override
    public Ceo save(Ceo ceo) {
        return CeoStateMapper.toDomain(ceoStatePort.save(CeoStateMapper.toState(ceo)));
    }
}

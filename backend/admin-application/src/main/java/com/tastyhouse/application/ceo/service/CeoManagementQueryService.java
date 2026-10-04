package com.tastyhouse.application.ceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoManagementQueryUseCase;
import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.ceo.port.out.CeoQueryPort;

@Service
@Transactional(readOnly = true)
class CeoManagementQueryService implements CeoManagementQueryUseCase {

    private final CeoQueryPort ceoQueryPort;

    public CeoManagementQueryService(CeoQueryPort ceoQueryPort) {
        this.ceoQueryPort = ceoQueryPort;
    }

    @Override
    public List<CeoListItemResult> getCeos() {
        return ceoQueryPort.findAllCeos();
    }
}

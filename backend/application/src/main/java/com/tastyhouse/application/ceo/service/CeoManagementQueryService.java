package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.ceo.port.out.CeoQueryPort;
import com.tastyhouse.application.ceo.port.in.CeoManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class CeoManagementQueryService implements CeoManagementQueryUseCase {

    private final CeoQueryPort ceoQueryPort;

    public CeoManagementQueryService(CeoQueryPort ceoQueryPort) {
        this.ceoQueryPort = ceoQueryPort;
    }

    @Override
    public List<CeoListItemResult> getCeos() {
        return ceoQueryPort.findAllCeos();
    }
}

package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;

@AdminApp
public interface CeoManagementQueryUseCase {

    List<CeoListItemResult> getCeos();
}

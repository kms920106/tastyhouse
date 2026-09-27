package com.tastyhouse.application.ceo.port.in;

import java.util.List;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface CeoManagementQueryUseCase {

    List<CeoListItemResult> getCeos();
}

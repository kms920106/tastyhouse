package com.tastyhouse.application.ceo.port.in;

import java.util.List;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;

public interface CeoManagementQueryUseCase {

    List<CeoListItemResult> getCeos();
}

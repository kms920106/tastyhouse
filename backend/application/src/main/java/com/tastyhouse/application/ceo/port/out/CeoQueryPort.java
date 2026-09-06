package com.tastyhouse.application.ceo.port.out;

import java.util.List;

public interface CeoQueryPort {

    List<CeoListItemResult> findAllCeos();
}

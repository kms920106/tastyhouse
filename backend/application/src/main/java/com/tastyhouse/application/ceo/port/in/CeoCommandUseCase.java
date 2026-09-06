package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface CeoCommandUseCase {

    void createCeo(CeoCreateCommand command);
}

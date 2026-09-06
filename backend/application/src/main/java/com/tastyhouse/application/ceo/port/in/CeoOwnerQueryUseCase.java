package com.tastyhouse.application.ceo.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface CeoOwnerQueryUseCase {

    boolean existsByUsername(String username);
}

package com.tastyhouse.application.productsoldout.port.in;

import com.tastyhouse.application.shared.marker.BatchApp;

@BatchApp
public interface ReleaseExpiredSoldOutUseCase {

    void releaseExpiredSoldOut();
}

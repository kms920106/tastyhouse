package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.BatchApp;

@BatchApp
public interface SyncProductOptionsUseCase {

    void crawlAndSaveProductOptions();
}

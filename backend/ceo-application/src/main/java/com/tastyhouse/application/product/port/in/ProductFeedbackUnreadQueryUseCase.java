package com.tastyhouse.application.product.port.in;

public interface ProductFeedbackUnreadQueryUseCase {

    boolean getUnread(Long ceoId, Long shopId);
}

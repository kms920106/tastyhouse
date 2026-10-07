package com.tastyhouse.application.shop.port.in;

import java.util.List;

public interface ShopNoticeOwnerValidationQueryUseCase {

    List<String> validateNotice(Long ceoId, Long shopId, String content);
}

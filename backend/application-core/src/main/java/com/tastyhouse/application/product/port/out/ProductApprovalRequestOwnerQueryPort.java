package com.tastyhouse.application.product.port.out;

import java.util.List;

public interface ProductApprovalRequestOwnerQueryPort {

    List<ProductImageChangeRequestResult> findImageChangeRequests(Long productId);

    List<ProductVegetarianRequestResult> findVegetarianRequests(Long productId);
}

package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.util.List;

import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopRequestTypeCatalogResult;

@CeoApp
public interface ShopRequestQueryUseCase {

    PageResult<ShopRequestListItemViewResult> getRequests(
        Long ceoId,
        Long shopId,
        String requestType,
        String status,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    );

    ShopRequestDetailViewResult getRequestDetail(Long ceoId, Long shopId, Long requestId);

    List<ShopRequestCommentResult> getComments(Long ceoId, Long shopId, Long requestId);

    ShopRequestTypeCatalogResult getRequestTypes();
}

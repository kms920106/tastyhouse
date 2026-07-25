package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageChangeRequest;
import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.shared.model.ApprovalStatus;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopImageChangeRequestRepository {

    ShopImageChangeRequest save(ShopImageChangeRequest shopImageChangeRequest);

    Optional<ShopImageChangeRequest> findById(Long id);

    List<ShopImageChangeRequest> findByShopId(Long shopId);

    boolean existsByShopIdAndImageTypeAndStatus(Long shopId, ShopImageType imageType, ApprovalStatus status);

    boolean existsByShopIdAndStatus(Long shopId, ApprovalStatus status);

    PageResult<ShopImageChangeRequest> findByStatusAndImageType(ApprovalStatus status, ShopImageType imageType, PageQuery pageQuery);
}

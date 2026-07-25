package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.domain.shop.domain.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopImageChangeRequestResult;
import com.tastyhouse.core.shared.model.ApprovalStatus;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ShopImageChangeQueryService {

    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;

    public List<ShopImageChangeRequestResult> findByShopId(Long shopId) {
        return shopImageChangeRequestRepository.findByShopId(shopId).stream()
            .map(ShopImageChangeRequestResult::from)
            .toList();
    }

    public PageResult<ShopImageChangeRequestResult> findRequests(ApprovalStatus status, ShopImageType imageType, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return shopImageChangeRequestRepository.findByStatusAndImageType(status, imageType, pageQuery)
            .map(ShopImageChangeRequestResult::from);
    }

    public boolean existsPendingByShopId(Long shopId) {
        return shopImageChangeRequestRepository.existsByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
    }
}

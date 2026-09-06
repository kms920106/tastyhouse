package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface StorePriceVerificationQueryPort {

    PageResult<StorePriceVerificationListItemResult> findVerificationPage(StorePriceVerificationStatus status, PageQuery pageQuery);

    Optional<StorePriceVerificationListItemResult> findVerificationById(Long verificationId);

    List<StorePriceVerificationItemResult> findVerificationItems(Long verificationId);
}

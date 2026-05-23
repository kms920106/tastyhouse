package com.tastyhouse.core.domain.partnership.domain.repository;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PartnershipRepository {

    List<PartnershipRequest> findAllOrderByCreatedAtDesc();

    Page<PartnershipRequest> findAllOrderByCreatedAtDesc(Pageable pageable);

    PartnershipRequest save(PartnershipRequest partnershipRequest);
}

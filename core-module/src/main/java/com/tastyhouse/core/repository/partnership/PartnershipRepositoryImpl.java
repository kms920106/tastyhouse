package com.tastyhouse.core.repository.partnership;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.partnership.PartnershipRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.entity.partnership.QPartnershipRequest.partnershipRequest;

@Repository
@RequiredArgsConstructor
public class PartnershipRepositoryImpl implements PartnershipRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PartnershipRequest> findAllOrderByCreatedAtDesc() {
        return queryFactory
            .selectFrom(partnershipRequest)
            .orderBy(partnershipRequest.createdAt.desc())
            .fetch();
    }

    @Override
    public Page<PartnershipRequest> findAllOrderByCreatedAtDesc(Pageable pageable) {
        List<PartnershipRequest> content = queryFactory
            .selectFrom(partnershipRequest)
            .orderBy(partnershipRequest.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(partnershipRequest.count())
            .from(partnershipRequest)
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}

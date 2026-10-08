package com.tastyhouse.infrastructure.persistence.member.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressPersistencePort;

import static com.tastyhouse.infrastructure.persistence.member.persistence.QMemberDeliveryAddressJpaEntity.memberDeliveryAddressJpaEntity;

@Repository
class MemberDeliveryAddressPersistenceAdapter implements MemberDeliveryAddressPersistencePort {

    private final JPAQueryFactory queryFactory;
    private final MemberDeliveryAddressJpaRepository memberDeliveryAddressJpaRepository;

    public MemberDeliveryAddressPersistenceAdapter(JPAQueryFactory queryFactory, MemberDeliveryAddressJpaRepository memberDeliveryAddressJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberDeliveryAddressJpaRepository = memberDeliveryAddressJpaRepository;
    }

    @Override
    public Optional<MemberDeliveryAddress> findById(Long addressId) {
        return memberDeliveryAddressJpaRepository.findById(addressId).map(MemberDeliveryAddressMapper::toDomain);
    }

    @Override
    public List<MemberDeliveryAddress> findByMemberId(MemberId memberId) {
        return queryFactory.selectFrom(memberDeliveryAddressJpaEntity)
            .where(memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()))
            .orderBy(memberDeliveryAddressJpaEntity.id.asc())
            .fetch()
            .stream()
            .map(MemberDeliveryAddressMapper::toDomain)
            .toList();
    }

    @Override
    public long countByMemberId(MemberId memberId) {
        Long count = queryFactory.select(memberDeliveryAddressJpaEntity.count())
            .from(memberDeliveryAddressJpaEntity)
            .where(memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()))
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId) {
        return Optional.ofNullable(queryFactory.selectFrom(memberDeliveryAddressJpaEntity)
            .where(
                memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()),
                memberDeliveryAddressJpaEntity.defaultAddress.isTrue()
            )
            .fetchOne())
            .map(MemberDeliveryAddressMapper::toDomain);
    }

    @Override
    public MemberDeliveryAddress save(MemberDeliveryAddress memberDeliveryAddress) {
        if (memberDeliveryAddress.getId() == null) {
            MemberDeliveryAddressJpaEntity saved = memberDeliveryAddressJpaRepository.save(
                MemberDeliveryAddressMapper.toEntity(memberDeliveryAddress)
            );
            return MemberDeliveryAddressMapper.toDomain(saved);
        }

        MemberDeliveryAddressJpaEntity entity = memberDeliveryAddressJpaRepository.findById(memberDeliveryAddress.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달 주소입니다: " + memberDeliveryAddress.getId()));
        MemberDeliveryAddressMapper.applyChanges(entity, memberDeliveryAddress);
        return MemberDeliveryAddressMapper.toDomain(entity);
    }

    @Override
    public void deleteById(Long addressId) {
        memberDeliveryAddressJpaRepository.deleteById(addressId);
    }
}

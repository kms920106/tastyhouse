package com.tastyhouse.infrastructure.jpa.member.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressSavePort;

import static com.tastyhouse.infrastructure.jpa.member.persistence.QMemberDeliveryAddressJpaEntity.memberDeliveryAddressJpaEntity;

@Repository
class MemberDeliveryAddressPersistenceAdapter implements MemberDeliveryAddressLoadPort, MemberDeliveryAddressSavePort {

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
    public long countByMemberId(MemberId memberId) {
        Long count = queryFactory.select(memberDeliveryAddressJpaEntity.count())
            .from(memberDeliveryAddressJpaEntity)
            .where(memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()))
            .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId) {
        MemberDeliveryAddressJpaEntity entity = queryFactory
            .selectFrom(memberDeliveryAddressJpaEntity)
            .where(
                memberDeliveryAddressJpaEntity.memberId.eq(memberId.value()),
                memberDeliveryAddressJpaEntity.defaultAddress.isTrue()
            )
            .fetchOne();
        return Optional.ofNullable(entity).map(MemberDeliveryAddressMapper::toDomain);
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

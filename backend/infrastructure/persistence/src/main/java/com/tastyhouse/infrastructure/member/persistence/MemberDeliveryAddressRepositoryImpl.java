package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.vo.MemberId;

@Repository
public class MemberDeliveryAddressRepositoryImpl implements MemberDeliveryAddressRepository {
    private final MemberDeliveryAddressJpaRepository memberDeliveryAddressJpaRepository;

    public MemberDeliveryAddressRepositoryImpl(MemberDeliveryAddressJpaRepository memberDeliveryAddressJpaRepository) {
        this.memberDeliveryAddressJpaRepository = memberDeliveryAddressJpaRepository;
    }

    @Override
    public Optional<MemberDeliveryAddress> findById(Long addressId) {
        return memberDeliveryAddressJpaRepository.findById(addressId).map(MemberDeliveryAddressMapper::toDomain);
    }

    @Override
    public List<MemberDeliveryAddress> findByMemberId(MemberId memberId) {
        return memberDeliveryAddressJpaRepository.findByMemberIdOrderByIdAsc(memberId.value())
            .stream()
            .map(MemberDeliveryAddressMapper::toDomain)
            .toList();
    }

    @Override
    public long countByMemberId(MemberId memberId) {
        return memberDeliveryAddressJpaRepository.countByMemberId(memberId.value());
    }

    @Override
    public Optional<MemberDeliveryAddress> findDefaultByMemberId(MemberId memberId) {
        return memberDeliveryAddressJpaRepository.findByMemberIdAndDefaultAddressTrue(memberId.value())
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

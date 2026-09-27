package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressState;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressStatePort;

@Repository
public class MemberDeliveryAddressStatePortImpl implements MemberDeliveryAddressStatePort {
    private final MemberDeliveryAddressJpaRepository memberDeliveryAddressJpaRepository;

    public MemberDeliveryAddressStatePortImpl(MemberDeliveryAddressJpaRepository memberDeliveryAddressJpaRepository) {
        this.memberDeliveryAddressJpaRepository = memberDeliveryAddressJpaRepository;
    }

    @Override
    public Optional<MemberDeliveryAddressState> findById(Long addressId) {
        return memberDeliveryAddressJpaRepository.findById(addressId).map(MemberDeliveryAddressMapper::toState);
    }

    @Override
    public List<MemberDeliveryAddressState> findByMemberId(Long memberId) {
        return memberDeliveryAddressJpaRepository.findByMemberIdOrderByIdAsc(memberId)
            .stream()
            .map(MemberDeliveryAddressMapper::toState)
            .toList();
    }

    @Override
    public long countByMemberId(Long memberId) {
        return memberDeliveryAddressJpaRepository.countByMemberId(memberId);
    }

    @Override
    public Optional<MemberDeliveryAddressState> findDefaultByMemberId(Long memberId) {
        return memberDeliveryAddressJpaRepository.findByMemberIdAndDefaultAddressTrue(memberId)
            .map(MemberDeliveryAddressMapper::toState);
    }

    @Override
    public MemberDeliveryAddressState save(MemberDeliveryAddressState state) {
        if (state.id() == null) {
            MemberDeliveryAddressJpaEntity saved = memberDeliveryAddressJpaRepository.save(
                MemberDeliveryAddressMapper.toEntity(state)
            );
            return MemberDeliveryAddressMapper.toState(saved);
        }

        MemberDeliveryAddressJpaEntity entity = memberDeliveryAddressJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배달 주소입니다: " + state.id()));
        MemberDeliveryAddressMapper.applyChanges(entity, state);
        return MemberDeliveryAddressMapper.toState(entity);
    }

    @Override
    public void deleteById(Long addressId) {
        memberDeliveryAddressJpaRepository.deleteById(addressId);
    }
}

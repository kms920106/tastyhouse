package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 회원 배달 주소록 write 어댑터.
 *
 * <p>표현용 목록 조회(행정동명 조인)는 같은 모듈의 {@code member/query/MemberDeliveryAddressQueryDao}가
 * 담당하고, 여기서는 도메인 서비스의 불변식 검증에 필요한 로드·카운트·저장·삭제만 다룬다.
 */
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
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

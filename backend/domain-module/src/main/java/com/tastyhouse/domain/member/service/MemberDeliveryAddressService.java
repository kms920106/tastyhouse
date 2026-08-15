package com.tastyhouse.domain.member.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;

/**
 * 회원 배달 주소록 컬렉션 불변식(도메인 서비스).
 *
 * <p>세 규칙이 여기 있는 이유는 전부 <b>회원의 주소록 전체를 읽어야 판정</b>되는 크로스 애그리거트
 * 규칙이기 때문이다 — 주소 한 건만 봐서는 알 수 없다.
 *
 * <ul>
 *   <li><b>기본 배송지 유일성</b> — 새 기본을 지정하면 기존 기본을 해제한다.</li>
 *   <li><b>회원당 {@value #MAX_ADDRESS_COUNT}건 한도</b>.</li>
 *   <li><b>소유권 검증</b> — 수정·삭제·기본지정 전부. 주소 id만으로 접근하는 경로라
 *       검증을 빠뜨리면 남의 주소를 조작할 수 있다.</li>
 * </ul>
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code MemberDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 소비 모듈의 command
 * 서비스가 선언한다.
 *
 * <p>도메인 모델이 프레임워크-프리 POJO라 더티체킹이 없으므로, 상태를 바꾼 뒤에는 반드시 <b>명시적
 * {@code save}</b>를 호출한다.
 */
public class MemberDeliveryAddressService {

    /** 회원당 배달 주소 등록 한도. */
    private static final int MAX_ADDRESS_COUNT = 10;

    /** 행정동 매칭 시 주소 문자열을 시/도 · 시/군/구 · 행정동명으로 쪼개기 위한 최소 토큰 수. */
    private static final int ADDRESS_TOKEN_MIN_COUNT = 3;

    private final MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    private final AdminDongRepository adminDongRepository;

    public MemberDeliveryAddressService(
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        AdminDongRepository adminDongRepository
    ) {
        this.memberDeliveryAddressRepository = memberDeliveryAddressRepository;
        this.adminDongRepository = adminDongRepository;
    }

    /**
     * 배달 주소를 등록하고 생성된 식별자를 반환한다.
     *
     * <p>한도 검증 → 행정동 매칭 → (기본 지정이면) 기존 기본 해제 → 저장 순서로 진행한다.
     * 좌표 필수 검증은 {@link MemberDeliveryAddress#of}가 강제한다.
     */
    public Long create(
        MemberId memberId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean isDefault
    ) {
        if (memberDeliveryAddressRepository.countByMemberId(memberId) >= MAX_ADDRESS_COUNT) {
            throw new BusinessException(ErrorCode.MEMBER_DELIVERY_ADDRESS_LIMIT_EXCEEDED);
        }

        AdminDongId adminDongId = matchAdminDongId(roadAddress, lotAddress);
        if (isDefault) {
            unmarkExistingDefault(memberId);
        }

        MemberDeliveryAddress address = MemberDeliveryAddress.of(
            memberId,
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            adminDongId,
            latitude,
            longitude,
            isDefault
        );
        return memberDeliveryAddressRepository.save(address).getId();
    }

    /**
     * 배달 주소를 수정한다. 주소 문자열이 바뀌면 행정동도 다시 매칭한다.
     *
     * <p>기본 배송지 여부는 여기서 바꾸지 않는다 — 유일성 불변식을 다루는 전용 경로
     * ({@link #changeDefault})가 따로 있고, 두 경로가 같은 상태를 건드리면 규칙이 두 벌이 된다.
     */
    public void update(
        MemberId memberId,
        Long addressId,
        String alias,
        String roadAddress,
        String lotAddress,
        String detailAddress,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);

        address.update(
            alias,
            roadAddress,
            lotAddress,
            detailAddress,
            matchAdminDongId(roadAddress, lotAddress),
            latitude,
            longitude
        );
        memberDeliveryAddressRepository.save(address);
    }

    /** 배달 주소를 삭제한다. 기본 배송지를 지워도 다른 주소가 자동 승격되지는 않는다. */
    public void delete(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);
        memberDeliveryAddressRepository.deleteById(address.getId());
    }

    /**
     * 주어진 주소를 기본 배송지로 지정한다. 기존 기본 배송지가 있으면 먼저 해제해 <b>회원당 1건</b>
     * 유일성을 유지한다.
     */
    public void changeDefault(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);

        unmarkExistingDefault(memberId);

        address.markAsDefault();
        memberDeliveryAddressRepository.save(address);
    }

    /**
     * 주소를 로드하고 소유권을 검증한다. 주소 id만으로 접근하는 API라 이 검증이 유일한 방어선이다.
     */
    private MemberDeliveryAddress loadOwnedAddress(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = memberDeliveryAddressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));
        if (!address.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }
        return address;
    }

    /**
     * 회원의 기존 기본 배송지가 있으면 해제하고 명시적으로 저장한다. 더티체킹이 없으므로 save가 없으면
     * 해제가 유실되어 기본 배송지가 2건이 된다.
     */
    private void unmarkExistingDefault(MemberId memberId) {
        memberDeliveryAddressRepository.findDefaultByMemberId(memberId).ifPresent(existing -> {
            existing.unmarkDefault();
            memberDeliveryAddressRepository.save(existing);
        });
    }

    /**
     * 주소 문자열의 행정동명으로 {@code ADMIN_DONG}을 매칭한다.
     *
     * <p><b>매칭 실패는 예외가 아니라 {@code null}이다.</b> 행정동은 지역별 배달팁을 붙이기 위한 파생
     * 값이며, 매칭에 실패하면 그 가게의 지역별 팁이 적용되지 않을 뿐 주소 저장 자체는 성공해야 한다 —
     * 여기서 거절하면 행정동 마스터에 없는 신도시·도서 지역 회원이 주소를 아예 등록하지 못한다.
     * (좌표와는 성격이 다르다. 좌표는 없으면 거리별 팁이 0원이 되는 매출 누수라 {@code of}가 거부한다.)
     *
     * <p>도로명 주소를 우선 시도하고, 도로명 주소에 행정동명이 없어 실패하면 지번 주소로 재시도한다 —
     * 도로명 주소는 "법정동"이 아니라 도로명을 담아 행정동명이 나타나지 않는 경우가 흔하기 때문이다.
     */
    private AdminDongId matchAdminDongId(String roadAddress, String lotAddress) {
        return findAdminDongByAddress(roadAddress)
            .or(() -> findAdminDongByAddress(lotAddress))
            .map(AdminDong::getId)
            .map(AdminDongId::of)
            .orElse(null);
    }

    /**
     * 주소 문자열을 공백으로 쪼개 앞 3토큰(시/도 · 시/군/구 · 행정동명)으로 행정동을 찾는다.
     * 토큰이 3개 미만이면 매칭을 시도하지 않는다.
     */
    private Optional<AdminDong> findAdminDongByAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        String[] tokens = address.trim().split("\\s+");
        if (tokens.length < ADDRESS_TOKEN_MIN_COUNT) {
            return Optional.empty();
        }
        return adminDongRepository.findByDongNameMatch(tokens[0], tokens[1], tokens[2]);
    }
}

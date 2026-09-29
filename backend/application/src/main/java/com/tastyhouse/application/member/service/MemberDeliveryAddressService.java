package com.tastyhouse.application.member.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressPersistencePort;
import com.tastyhouse.application.region.port.out.write.AdminDongPersistencePort;

public class MemberDeliveryAddressService {
    private static final int MAX_ADDRESS_COUNT = 10;

    private static final int ADDRESS_TOKEN_MIN_COUNT = 3;

    private final MemberDeliveryAddressPersistencePort memberDeliveryAddressPersistencePort;
    private final AdminDongPersistencePort adminDongPersistencePort;

    public MemberDeliveryAddressService(
        MemberDeliveryAddressPersistencePort memberDeliveryAddressPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort
    ) {
        this.memberDeliveryAddressPersistencePort = memberDeliveryAddressPersistencePort;
        this.adminDongPersistencePort = adminDongPersistencePort;
    }

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
        if (memberDeliveryAddressPersistencePort.countByMemberId(memberId) >= MAX_ADDRESS_COUNT) {
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
        return memberDeliveryAddressPersistencePort.save(address).getId();
    }

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
        memberDeliveryAddressPersistencePort.save(address);
    }

    public void delete(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);
        memberDeliveryAddressPersistencePort.deleteById(address.getId());
    }

    public void changeDefault(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);

        unmarkExistingDefault(memberId);

        address.markAsDefault();
        memberDeliveryAddressPersistencePort.save(address);
    }

    public MemberDeliveryAddress findOwnedAddress(MemberId memberId, Long addressId) {
        return loadOwnedAddress(memberId, addressId);
    }

    private MemberDeliveryAddress loadOwnedAddress(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = memberDeliveryAddressPersistencePort.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));
        if (!address.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }
        return address;
    }

    private void unmarkExistingDefault(MemberId memberId) {
        memberDeliveryAddressPersistencePort.findDefaultByMemberId(memberId).ifPresent(existing -> {
            existing.unmarkDefault();
            memberDeliveryAddressPersistencePort.save(existing);
        });
    }

    private AdminDongId matchAdminDongId(String roadAddress, String lotAddress) {
        return findAdminDongByAddress(roadAddress)
            .or(() -> findAdminDongByAddress(lotAddress))
            .map(AdminDong::getId)
            .map(AdminDongId::of)
            .orElse(null);
    }

    private Optional<AdminDong> findAdminDongByAddress(String address) {
        if (address == null || address.isBlank()) {
            return Optional.empty();
        }

        String[] tokens = address.trim().split("\\s+");
        if (tokens.length < ADDRESS_TOKEN_MIN_COUNT) {
            return Optional.empty();
        }
        return adminDongPersistencePort.findByDongNameMatch(tokens[0], tokens[1], tokens[2]);
    }
}

package com.tastyhouse.application.member.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberDeliveryAddressSavePort;
import com.tastyhouse.application.region.port.out.write.AdminDongLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MemberDeliveryAddressService {

    private static final int MAX_ADDRESS_COUNT = 10;

    private static final int ADDRESS_TOKEN_MIN_COUNT = 3;

    private final MemberDeliveryAddressLoadPort memberDeliveryAddressLoadPort;
    private final MemberDeliveryAddressSavePort memberDeliveryAddressSavePort;
    private final AdminDongLoadPort adminDongLoadPort;

    public MemberDeliveryAddressService(
        MemberDeliveryAddressLoadPort memberDeliveryAddressLoadPort,
        MemberDeliveryAddressSavePort memberDeliveryAddressSavePort,
        AdminDongLoadPort adminDongLoadPort
    ) {
        this.memberDeliveryAddressLoadPort = memberDeliveryAddressLoadPort;
        this.memberDeliveryAddressSavePort = memberDeliveryAddressSavePort;
        this.adminDongLoadPort = adminDongLoadPort;
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
        if (memberDeliveryAddressLoadPort.countByMemberId(memberId) >= MAX_ADDRESS_COUNT) {
            throw new ApplicationException(WebErrorCode.MEMBER_DELIVERY_ADDRESS_LIMIT_EXCEEDED);
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
        return memberDeliveryAddressSavePort.save(address).getId();
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
        memberDeliveryAddressSavePort.save(address);
    }

    public void delete(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);
        memberDeliveryAddressSavePort.deleteById(address.getId());
    }

    public void changeDefault(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = loadOwnedAddress(memberId, addressId);

        unmarkExistingDefault(memberId);

        address.markAsDefault();
        memberDeliveryAddressSavePort.save(address);
    }

    public MemberDeliveryAddress findOwnedAddress(MemberId memberId, Long addressId) {
        return loadOwnedAddress(memberId, addressId);
    }

    private MemberDeliveryAddress loadOwnedAddress(MemberId memberId, Long addressId) {
        MemberDeliveryAddress address = memberDeliveryAddressLoadPort.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException(WebErrorCode.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));
        if (!address.isOwnedBy(memberId)) {
            throw new ApplicationException(WebErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }
        return address;
    }

    private void unmarkExistingDefault(MemberId memberId) {
        memberDeliveryAddressLoadPort.findDefaultByMemberId(memberId).ifPresent(existing -> {
            existing.unmarkDefault();
            memberDeliveryAddressSavePort.save(existing);
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
        return adminDongLoadPort.findByDongNameMatch(tokens[0], tokens[1], tokens[2]);
    }
}

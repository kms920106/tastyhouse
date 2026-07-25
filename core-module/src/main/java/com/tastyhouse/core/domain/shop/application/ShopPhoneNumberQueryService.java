package com.tastyhouse.core.domain.shop.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopPhoneNumber;
import com.tastyhouse.core.domain.shop.domain.repository.ShopPhoneNumberRepository;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhoneNumberResult;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShopPhoneNumberQueryService {

    private final ShopPhoneNumberRepository shopPhoneNumberRepository;

    public List<ShopPhoneNumberResult> findPhoneNumbers(Long shopId) {
        return shopPhoneNumberRepository.findByShopId(shopId).stream()
            .map(this::toShopPhoneNumberResult)
            .toList();
    }

    private ShopPhoneNumberResult toShopPhoneNumberResult(ShopPhoneNumber phoneNumber) {
        return ShopPhoneNumberResult.from(
            phoneNumber.getId(),
            phoneNumber.getShopId(),
            phoneNumber.getPhoneNumber(),
            phoneNumber.isPrimary(),
            phoneNumber.isVirtual()
        );
    }
}

package com.tastyhouse.domain.shop.service;

import java.util.List;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.repository.ShopPhoneNumberRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 전화번호 목록 불변식(도메인 서비스).
 *
 * <p>가게 전화번호는 목록({@code ShopPhoneNumber} 다건)과 가게 대표 전화번호({@code Shop.phoneNumber})가
 * <b>항상 일치</b>해야 한다. 대표번호가 바뀌거나 삭제되면 가게 애그리거트의 전화번호도 같은 트랜잭션에서
 * 함께 갱신되어야 하며(그렇지 않으면 목록과 상세의 번호가 어긋난다), "대표는 정확히 한 건"이라는
 * 불변식도 유지되어야 한다. {@code ShopPhoneNumber}와 {@code Shop} 두 애그리거트 타입을 함께
 * load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로 도메인 계층에 둔다.
 *
 * <p>정책: 가게당 최대 {@value #MAX_PHONE_NUMBER_COUNT}건, 첫 등록분이 자동으로 대표가 되며, 대표를
 * 삭제하면 남은 번호 중 첫 건이 대표를 승계한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 *
 * <p><b>변경이력 기록도 이 서비스가 소유한다</b>(운영 분류 {@code PHONE_NUMBER}·
 * {@code REPRESENTATIVE_PHONE}). 이미 전화번호 목록과 대상 애그리거트를 로드해 둔 상태라 추가 조회 없이
 * 변경 전 값을 얻을 수 있다. 변경 주체({@link ShopChangeActor})는 도메인이 인증을 모르므로 마지막
 * 파라미터로 명시 전달받는다.
 */
public class ShopPhoneNumberRegistryService {

    private static final int MAX_PHONE_NUMBER_COUNT = 10;

    private final ShopPhoneNumberRepository shopPhoneNumberRepository;
    private final ShopRepository shopRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopPhoneNumberRegistryService(
        ShopPhoneNumberRepository shopPhoneNumberRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopPhoneNumberRepository = shopPhoneNumberRepository;
        this.shopRepository = shopRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    /**
     * 전화번호를 추가한다. 가게의 첫 번호면 대표로 지정되고 가게 대표 전화번호도 함께 갱신된다.
     *
     * @return 생성된 전화번호 식별자
     */
    public Long addPhoneNumber(Long shopId, String phoneNumber, boolean virtual, ShopChangeActor actor) {
        List<ShopPhoneNumber> existingPhoneNumbers = shopPhoneNumberRepository.findByShopId(shopId);
        if (existingPhoneNumbers.size() >= MAX_PHONE_NUMBER_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_PHONE_NUMBER_LIMIT_EXCEEDED);
        }

        boolean primary = existingPhoneNumbers.isEmpty();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(
            ShopPhoneNumber.of(ShopId.of(shopId), phoneNumber, primary, virtual)
        );

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.PHONE_NUMBER,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describePhoneNumber(saved)
        );

        if (primary) {
            // 첫 등록분은 자동으로 대표가 되므로 대표번호 변경도 별도 이력으로 남긴다 — 두 분류가
            // 화면에서 따로 조회되므로 한쪽만 남기면 대표번호 변경 시점을 추적할 수 없다.
            syncShopPhoneNumber(shopId, saved.getPhoneNumber());
            shopChangeHistoryRecorder.record(
                saved.getShopId(),
                ShopChangeType.REPRESENTATIVE_PHONE,
                ShopChangeActionType.UPDATE,
                actor,
                describeRepresentativePhone(null),
                describeRepresentativePhone(saved.getPhoneNumber())
            );
        }

        return saved.getId();
    }

    /**
     * 전화번호를 삭제한다. 대표번호를 삭제하면 남은 번호 중 첫 건이 대표를 승계하고 가게 대표
     * 전화번호도 함께 갱신된다.
     */
    public void deletePhoneNumber(Long id, ShopChangeActor actor) {
        ShopPhoneNumber phoneNumber = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));
        shopPhoneNumberRepository.deleteById(id);

        shopChangeHistoryRecorder.record(
            phoneNumber.getShopId(),
            ShopChangeType.PHONE_NUMBER,
            ShopChangeActionType.DELETE,
            actor,
            describePhoneNumber(phoneNumber),
            null
        );

        if (!phoneNumber.isPrimary()) {
            return;
        }

        List<ShopPhoneNumber> remainingPhoneNumbers = shopPhoneNumberRepository.findByShopId(phoneNumber.getShopId().value());
        if (remainingPhoneNumbers.isEmpty()) {
            // 남은 번호가 없으면 승계 대상이 없다. 가게 대표 전화번호는 기존 동작대로 그대로 둔다.
            return;
        }

        ShopPhoneNumber newPrimary = remainingPhoneNumbers.getFirst();
        newPrimary.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(newPrimary);
        syncShopPhoneNumber(phoneNumber.getShopId().value(), saved.getPhoneNumber());

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.REPRESENTATIVE_PHONE,
            ShopChangeActionType.UPDATE,
            actor,
            describeRepresentativePhone(phoneNumber.getPhoneNumber()),
            describeRepresentativePhone(saved.getPhoneNumber())
        );
    }

    /**
     * 대표번호를 지정한다. 기존 대표는 해제되고 가게 대표 전화번호도 함께 갱신된다.
     */
    public void designatePrimary(Long id, ShopChangeActor actor) {
        ShopPhoneNumber target = shopPhoneNumberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_PHONE_NUMBER_NOT_FOUND));

        List<ShopPhoneNumber> phoneNumbers = shopPhoneNumberRepository.findByShopId(target.getShopId().value());
        String previousPrimaryPhoneNumber = null;
        for (ShopPhoneNumber phoneNumber : phoneNumbers) {
            if (phoneNumber.isPrimary() && !phoneNumber.getId().equals(target.getId())) {
                previousPrimaryPhoneNumber = phoneNumber.getPhoneNumber();
                phoneNumber.unmarkPrimary();
                shopPhoneNumberRepository.save(phoneNumber);
            }
        }

        target.markPrimary();
        ShopPhoneNumber saved = shopPhoneNumberRepository.save(target);
        syncShopPhoneNumber(saved.getShopId().value(), saved.getPhoneNumber());

        shopChangeHistoryRecorder.record(
            saved.getShopId(),
            ShopChangeType.REPRESENTATIVE_PHONE,
            ShopChangeActionType.UPDATE,
            actor,
            describeRepresentativePhone(previousPrimaryPhoneNumber),
            describeRepresentativePhone(saved.getPhoneNumber())
        );
    }

    /**
     * 전화번호 1행을 한 줄로 요약한다(예: {@code "010-1234-5678"}, 가상번호면 {@code "010-... (가상번호)"}).
     */
    private String describePhoneNumber(ShopPhoneNumber phoneNumber) {
        if (phoneNumber.isVirtual()) {
            return phoneNumber.getPhoneNumber() + " (가상번호)";
        }
        return phoneNumber.getPhoneNumber();
    }

    /**
     * 대표번호를 한 줄로 요약한다(예: {@code "대표번호: 010-1234-5678"}). 대표가 없던 상태는 "미설정"으로
     * 표기한다 — 빈 문자열은 "값이 없다"와 "이력에 값이 안 담겼다"를 구분하지 못한다.
     */
    private String describeRepresentativePhone(String phoneNumber) {
        return "대표번호: " + (phoneNumber == null ? ShopChangeValueFormatter.unset() : phoneNumber);
    }

    /**
     * 가게 애그리거트의 대표 전화번호를 목록의 대표번호와 일치시킨다.
     */
    private void syncShopPhoneNumber(Long shopId, String phoneNumber) {
        ShopId targetShopId = ShopId.of(shopId);
        Shop shop = shopRepository.findById(targetShopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        shop.changePhoneNumber(phoneNumber);
        shopRepository.save(shop);
    }
}

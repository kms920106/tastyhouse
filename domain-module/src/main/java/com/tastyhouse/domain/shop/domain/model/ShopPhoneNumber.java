package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 가게 전화번호(다건) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopPhoneNumberJpaEntity} + {@code ShopPhoneNumberMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ShopPhoneNumberRepository#save}를
 * 호출해야 한다.
 */
public class ShopPhoneNumber {

    private static final List<String> VIRTUAL_NUMBER_PREFIXES = List.of(
        "02", "15", "16", "18",
        "051", "053", "032", "062", "042", "052", "033",
        "031", "043", "041", "044", "063", "061", "054", "055", "064", "070", "010"
    );
    private static final int VIRTUAL_NUMBER_MIN_LENGTH = 8;
    private static final int VIRTUAL_NUMBER_MAX_LENGTH = 13;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId; // 가게 ID (SHOP.id 참조)
    private final String phoneNumber; // 전화번호
    private boolean primary; // 대표 여부 (상태전이로 재대입됨)
    private final boolean virtual; // 가상번호 여부
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopPhoneNumber(
        Long id,
        ShopId shopId,
        String phoneNumber,
        boolean primary,
        boolean virtual,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.phoneNumber = phoneNumber;
        this.primary = primary;
        this.virtual = virtual;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 가게 전화번호를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     * 가상번호로 등록 요청이면 발급 조건(지정 국번, 8~13자리)을 검증한다.
     */
    public static ShopPhoneNumber of(ShopId shopId, String phoneNumber, boolean primary, boolean virtual) {
        if (virtual && !isValidVirtualNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.SHOP_VIRTUAL_NUMBER_INVALID);
        }
        return new ShopPhoneNumber(null, shopId, phoneNumber, primary, virtual, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopPhoneNumber reconstitute(
        Long id,
        ShopId shopId,
        String phoneNumber,
        boolean primary,
        boolean virtual,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopPhoneNumber(id, shopId, phoneNumber, primary, virtual, createdAt, updatedAt);
    }

    /**
     * 대표 전화번호로 지정한다.
     */
    public void markPrimary() {
        this.primary = true;
    }

    /**
     * 대표 전화번호 지정을 해제한다.
     */
    public void unmarkPrimary() {
        this.primary = false;
    }

    /**
     * 가상번호 발급 조건(지정 국번으로 시작 && 숫자만 8~13자리)을 충족하는지 검증한다.
     */
    public static boolean isValidVirtualNumber(String raw) {
        if (raw == null) {
            return false;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() < VIRTUAL_NUMBER_MIN_LENGTH || digits.length() > VIRTUAL_NUMBER_MAX_LENGTH) {
            return false;
        }
        return VIRTUAL_NUMBER_PREFIXES.stream().anyMatch(digits::startsWith);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public boolean isPrimary() {
        return this.primary;
    }

    public boolean isVirtual() {
        return this.virtual;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}

package com.tastyhouse.domain.ceo.domain.model;

import com.tastyhouse.domain.ceo.domain.vo.CeoId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

/**
 * 점주 계정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code CeoJpaEntity} + {@code CeoMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code CeoRepository#save}를
 * 호출해야 한다.
 */
public class Ceo {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final String username;
    private final String password;
    private final String name;
    private final String businessRegistrationNumber;
    private final PhoneNumber phoneNumber;
    private final String email;
    private final CeoStatus status;

    private Ceo(
        Long id,
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumber phoneNumber,
        String email,
        CeoStatus status
    ) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.status = status;
    }

    /**
     * 신규 점주 계정을 생성한다. password는 이미 인코딩된 값이어야 한다.
     * 사업자등록번호·휴대폰번호·이메일은 시드 시점에는 알 수 없으므로 null로 시작한다.
     */
    public static Ceo create(String username, String encodedPassword, String name) {
        return new Ceo(null, username, encodedPassword, name, null, null, null, CeoStatus.ACTIVE);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static Ceo reconstitute(
        Long id,
        String username,
        String password,
        String name,
        String businessRegistrationNumber,
        PhoneNumber phoneNumber,
        String email,
        CeoStatus status
    ) {
        return new Ceo(id, username, password, name, businessRegistrationNumber, phoneNumber, email, status);
    }

    public CeoId getCeoId() {
        return CeoId.of(this.id);
    }

    public boolean isActive() {
        return this.status == CeoStatus.ACTIVE;
    }

    public Long getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getName() {
        return this.name;
    }

    public String getBusinessRegistrationNumber() {
        return this.businessRegistrationNumber;
    }

    public PhoneNumber getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getEmail() {
        return this.email;
    }

    public CeoStatus getStatus() {
        return this.status;
    }
}

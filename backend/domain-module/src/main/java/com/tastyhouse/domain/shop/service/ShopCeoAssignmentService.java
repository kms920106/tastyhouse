package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 담당 점주 배정·해제 불변식(도메인 서비스).
 *
 * <p>이 서비스가 존재하는 이유는 배정·해제가 단순한 컬럼 갱신이 아니라 <b>개인정보처리시스템 접근권한의
 * 부여·말소</b>이기 때문이다. {@code SHOP.ceo_id} 갱신과 이력 기록이 반드시 같은 트랜잭션에서 원자적으로
 * 일어나야 하고, 재배정은 "말소 후 부여" 2행으로 남아야 하며, 무의미한 재배정은 이력을 오염시키므로
 * 거부해야 한다 — 이 규칙들은 액터(admin API)가 하나여도 도메인 불변식이므로 도메인 계층에 둔다.
 *
 * <p>상태 규칙:
 * <table border="1">
 *   <caption>배정·해제 상태 전이</caption>
 *   <tr><th>상황</th><th>결과</th></tr>
 *   <tr><td>미배정 → 배정</td><td>{@code GRANT} 1행 + {@code SHOP.ceo_id} 갱신</td></tr>
 *   <tr><td>A 배정 → B 재배정</td><td>{@code REVOKE}(A) + {@code GRANT}(B) 2행, 같은 트랜잭션</td></tr>
 *   <tr><td>A 배정 → A 재배정</td><td>409 {@code SHOP_CEO_ALREADY_ASSIGNED}</td></tr>
 *   <tr><td>배정 → 해제</td><td>{@code REVOKE} 1행 + {@code SHOP.ceo_id = NULL}</td></tr>
 *   <tr><td>미배정 → 해제</td><td>409 {@code SHOP_CEO_NOT_ASSIGNED}</td></tr>
 * </table>
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * admin-api의 command 서비스가 선언한다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopCeoAssignmentService {

    private final ShopRepository shopRepository;
    private final CeoRepository ceoRepository;
    private final ShopCeoAssignmentRecorder shopCeoAssignmentRecorder;

    public ShopCeoAssignmentService(
        ShopRepository shopRepository,
        CeoRepository ceoRepository,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        this.shopRepository = shopRepository;
        this.ceoRepository = ceoRepository;
        this.shopCeoAssignmentRecorder = shopCeoAssignmentRecorder;
    }

    /**
     * 가게에 담당 점주를 배정한다.
     *
     * <p>이미 다른 점주가 배정돼 있으면 기존 점주의 권한을 먼저 말소하고({@code REVOKE}) 새 점주에게
     * 부여한다({@code GRANT}) — 두 행이 같은 트랜잭션에 남아야 "언제부터 언제까지 누가 접근할 수
     * 있었는가"가 끊기지 않는다.
     *
     * <p>같은 점주를 다시 배정하는 것은 409로 거부한다. 실제로 권한 상태가 달라지지 않았는데
     * {@code REVOKE}+{@code GRANT} 2행을 남기면, 그 순간 권한이 끊겼던 것처럼 읽히는 거짓 이력이 된다.
     */
    public void assign(ShopId shopId, CeoId ceoId, Long actorAdminId) {
        Shop shop = loadShop(shopId);
        validateCeoExists(ceoId);

        CeoId currentCeoId = shop.getCeoId();
        if (ceoId.equals(currentCeoId)) {
            throw new BusinessException(ErrorCode.SHOP_CEO_ALREADY_ASSIGNED);
        }

        shop.assignCeo(ceoId);
        shopRepository.save(shop);

        if (currentCeoId != null) {
            shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
        }
        shopCeoAssignmentRecorder.recordGrant(shopId, ceoId, actorAdminId);
    }

    /**
     * 가게의 담당 점주 배정을 해제한다.
     *
     * <p>해제 이후 그 점주의 해당 가게 관리 호출은 전부 403이 된다 — 담당 점주 미배정 가게는 어떤
     * 점주도 관리할 수 없다({@code docs/domain/shop.md}).
     */
    public void revoke(ShopId shopId, Long actorAdminId) {
        Shop shop = loadShop(shopId);

        CeoId currentCeoId = shop.getCeoId();
        if (currentCeoId == null) {
            throw new BusinessException(ErrorCode.SHOP_CEO_NOT_ASSIGNED);
        }

        shop.assignCeo(null);
        shopRepository.save(shop);

        shopCeoAssignmentRecorder.recordRevoke(shopId, currentCeoId, actorAdminId);
    }

    private Shop loadShop(ShopId shopId) {
        return shopRepository.findById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    /**
     * 배정 대상 점주가 실재하는지 확인한다. 존재하지 않는 점주를 배정하면 그 가게는 아무도 관리할 수
     * 없는 상태가 되고, 이력에는 존재하지 않는 계정에 권한을 부여한 행이 남는다.
     */
    private void validateCeoExists(CeoId ceoId) {
        if (ceoRepository.findById(ceoId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.CEO_NOT_FOUND);
        }
    }
}

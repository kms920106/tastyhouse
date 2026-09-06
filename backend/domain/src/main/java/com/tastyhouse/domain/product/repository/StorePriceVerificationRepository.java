package com.tastyhouse.domain.product.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.StorePriceVerification;
import com.tastyhouse.domain.product.model.StorePriceVerificationItem;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.product.vo.StorePriceVerificationId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 매장 가격 인증 요청 write 포트. 표현 목적 조회(관리자 검수 목록)는
 * {@code StorePriceVerificationQueryDao}가 담당한다.
 *
 * <p>항목({@code Item})을 별도 포트로 분리하지 않고 함께 두는 이유는 항목이 요청 애그리거트에
 * <b>종속된 구성요소</b>라 독립적인 생애주기를 갖지 않기 때문이다 — 요청 없이 항목만 조회·저장하는
 * 경로가 존재하지 않는다.
 *
 * <p>{@code existsByShopIdAndStatusIn}은 "검수 중이면 재요청 불가" 불변식 검증용이므로 화면용 집계가
 * 아니라 이 포트에 있다.
 */
public interface StorePriceVerificationRepository {

    StorePriceVerification save(StorePriceVerification verification);

    Optional<StorePriceVerification> findById(StorePriceVerificationId id);

    /** 가게의 가장 최근 인증 요청(생성 역순 첫 건). 점주 화면의 현재 상태 표시에 쓴다. */
    Optional<StorePriceVerification> findLatestByShopId(ShopId shopId);

    /** 검수 중(대기·진행) 요청이 있는지 — 재요청 차단 판정. */
    boolean existsByShopIdAndStatusIn(ShopId shopId, List<StorePriceVerificationStatus> statuses);

    void saveItem(StorePriceVerificationItem item);

    List<StorePriceVerificationItem> findAllItemsByVerificationId(StorePriceVerificationId verificationId);
}

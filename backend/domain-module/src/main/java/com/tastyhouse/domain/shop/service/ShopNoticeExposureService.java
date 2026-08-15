package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주 공지 앱 노출 불변식 — "가게당 노출 공지는 최대 1건".
 *
 * <p>노출 토글은 "이 공지를 켜면서 동시에 기존 노출 공지를 끈다"는 <b>집합 불변식</b>이라 단일 애그리거트
 * 연산이 아니다. api 모듈 CommandService가 이를 직접 하면 write 포트 조회·저장을 조합하게 되어 CQRS
 * 경계가 흐려지므로, 이 도메인 서비스가 소유한다.
 *
 * <p>DB 유니크 제약으로는 표현할 수 없다(MySQL 부분 유니크 인덱스 미지원). 동시 요청은 낙관적 락 없이
 * 마지막 쓰기 승리로 둔다 — 같은 점주가 자기 가게 공지 두 건을 동시에 켜는 시나리오는 실질적으로
 * 발생하지 않는다.
 *
 * <p>도메인 모델은 순수 POJO라 더티 체킹이 없으므로 상태 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ShopNoticeExposureService {

    private final ShopNoticeRepository shopNoticeRepository;

    public ShopNoticeExposureService(ShopNoticeRepository shopNoticeRepository) {
        this.shopNoticeRepository = shopNoticeRepository;
    }

    /**
     * 대상 공지를 앱에 노출한다. 같은 가게에 이미 노출 중인 다른 공지가 있으면 함께 내린다.
     *
     * <p>대상은 <b>이미 영속된</b> 공지여야 한다. 식별자가 없으면 "기존 노출 공지가 대상 자신인가"를
     * 판정할 수 없고, {@code null}과의 비교가 조용히 false가 되어 방금 만든 공지를 켜면서 남의 공지를
     * 내리는 것처럼 보이는 오동작이 된다. 신규 등록 경로는 {@code save} 이후에 호출한다.
     */
    public void expose(ShopId shopId, ShopNotice target) {
        if (target.getId() == null) {
            throw new IllegalArgumentException("영속되지 않은 공지는 노출할 수 없습니다.");
        }

        shopNoticeRepository.findExposedByShopId(shopId)
            .filter(current -> !current.getId().equals(target.getId()))
            .ifPresent(current -> {
                current.unexpose();
                shopNoticeRepository.save(current);
            });
        target.expose();
        shopNoticeRepository.save(target);
    }

    /**
     * 대상 공지를 앱에서 내린다.
     */
    public void unexpose(ShopNotice target) {
        target.unexpose();
        shopNoticeRepository.save(target);
    }
}

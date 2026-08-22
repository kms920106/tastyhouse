package com.tastyhouse.domain.shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.OriginSourceType;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopOriginInfo;
import com.tastyhouse.domain.shop.repository.ShopOriginInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 가게 원산지 표시 정보의 조회·upsert(도메인 서비스).
 *
 * <p>원산지는 가게당 1건이라 없으면 생성, 있으면 갱신하는 upsert 시맨틱을 가진다. 입력 방식별 필수
 * 필드·형식과 상호 배타 정리는 애그리거트({@link ShopOriginInfo})가 소유하고, 이 서비스는 가게 존재
 * 검증과 <b>변경이력 기록</b>을 담당한다.
 *
 * <p><b>변경이력을 여기서 남기는 이유</b>는 {@link ShopConvenienceInfoService}와 같다 — upsert라 변경 전
 * 값을 보려면 저장 전에 기존 행을 읽어야 하는데, 이 서비스는 upsert를 수행하려고 이미 그 행을 읽는
 * 유일한 지점이다. ceo-api의 {@code CommandService}는 CQRS 교차 주입 금지로 QueryDao를 주입할 수 없어
 * 변경 전 값을 구조적으로 볼 수 없다.
 *
 * <p><b>금칙어 검수를 하지 않는다.</b> 원산지 본문은 마케팅 문구가 아니라 법령이 요구하는 사실 표시이고
 * ("돼지고기: 국내산", "쇠고기: 미국산"), 검수로 저장을 막으면 법정 표시 의무를 이행할 수 없게 된다.
 * 찾아오는길 안내가 검수 대상인 것과 대비된다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ShopDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는 소비 모듈의 command
 * 서비스가 선언한다. 변경 주체({@link ShopChangeActor})는 도메인이 인증을 모르므로 마지막 파라미터로
 * 명시 전달받는다.
 */
public class ShopOriginInfoService {

    private final ShopOriginInfoRepository shopOriginInfoRepository;
    private final ShopRepository shopRepository;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopOriginInfoService(
        ShopOriginInfoRepository shopOriginInfoRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopOriginInfoRepository = shopOriginInfoRepository;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
        this.shopRepository = shopRepository;
    }

    /**
     * 가게의 원산지 정보를 조회한다. 미설정이면 {@code Optional.empty()}이며, 그 상태를 어떻게 보여줄지는
     * 소비 측이 결정한다 — 점주 화면은 빈 폼을, 손님 화면은 영역 숨김을 택한다.
     */
    public Optional<ShopOriginInfo> findByShopId(Long shopId) {
        return shopOriginInfoRepository.findByShopId(shopId);
    }

    /**
     * 원산지 정보를 upsert 한다(PUT 전체 교체).
     *
     * <p>한 화면에서 입력 방식과 본문을 통째로 저장하는 replace-all 성격이므로, 필드별로 이력을 쪼개지
     * 않고 <b>저장 1회당 1행</b>만 남긴다({@code UPDATE}).
     */
    public void upsertOriginInfo(
        Long shopId,
        OriginSourceType sourceType,
        String content,
        String url,
        ShopChangeActor actor
    ) {
        validateShopExists(shopId);

        // 변경 전 요약을 update 호출 전에 확정한다 — 같은 인스턴스를 제자리에서 갱신하므로
        // 나중에 읽으면 이미 변경 후 값이다.
        ShopOriginInfo existing = shopOriginInfoRepository.findByShopId(shopId).orElse(null);
        String previousValue = describeOriginInfo(existing);

        ShopOriginInfo shopOriginInfo;
        if (existing == null) {
            shopOriginInfo = ShopOriginInfo.of(ShopId.of(shopId), sourceType, content, url);
        } else {
            existing.update(sourceType, content, url);
            shopOriginInfo = existing;
        }

        shopOriginInfoRepository.save(shopOriginInfo);

        shopChangeHistoryRecorder.record(
            ShopId.of(shopId),
            ShopChangeType.ORIGIN_INFO,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeOriginInfo(shopOriginInfo)
        );
    }

    /**
     * 가게 존재를 확인한다. 기존 {@code findById}를 쓰고 존재 여부 전용 포트 메서드를 새로 열지 않는다 —
     * 포트에 메서드를 추가하면 손수 작성된 테스트 스텁 전부가 함께 깨진다.
     */
    private void validateShopExists(Long shopId) {
        shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private String describeOriginInfo(ShopOriginInfo originInfo) {
        if (originInfo == null) {
            return ShopChangeValueFormatter.snapshot(List.of());
        }

        List<String> lines = new ArrayList<>(2);
        lines.add("입력방식: " + originInfo.getSourceType().getDescription());
        if (originInfo.getSourceType() == OriginSourceType.DIRECT) {
            lines.add("원산지: " + valueOrUnset(originInfo.getContent()));
        } else {
            lines.add("URL: " + valueOrUnset(originInfo.getUrl()));
        }
        return ShopChangeValueFormatter.snapshot(lines);
    }

    private String valueOrUnset(String value) {
        return value == null || value.isBlank() ? ShopChangeValueFormatter.unset() : value;
    }
}

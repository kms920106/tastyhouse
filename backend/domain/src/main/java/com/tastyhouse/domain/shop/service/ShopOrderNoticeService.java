package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.repository.ShopOrderNoticeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 주문안내(메뉴판 최상단 안내 문구) 도메인 서비스.
 *
 * <p>{@code @Service} 없는 순수 POJO이며 {@code ShopDomainConfig}가 {@code @Bean}으로 등록한다.
 *
 * <p><b>왜 애그리거트가 아니라 도메인 서비스가 필요한가</b>: 주문안내는 "가게당 1건"이라는 집합
 * 불변식을 갖는다. 등록·수정 요청은 단일 엔드포인트({@code PUT})로 들어오지만 실제 처리는 기존 행
 * 유무에 따라 insert/update로 갈라지므로, 애그리거트 하나만으로는 표현할 수 없는 <b>전체교체(upsert)
 * 연산</b>이다. 이 분기를 각 api 모듈의 command 서비스에 두면 ceo·admin이 같은 규칙을 두 벌 갖게 되고,
 * 한쪽만 고쳐지는 결함이 생긴다.
 *
 * <p><b>승인 절차가 없다는 점이 메뉴모음컷·사장님 추천과의 결정적 차이다.</b> 그 둘은 등록 시
 * {@code PENDING} 행을 만들고 관리자 승인 뒤에야 손님에게 보이지만, 주문안내는 {@link #upsert}가
 * 반환하는 순간 이미 노출 대상이다 — 출처 PDF가 주문안내를 "편집 가능 항목"으로, 메뉴모음컷·사장님
 * 추천만 "승인 필요"로 분류하기 때문이다. 그래서 이 서비스에는 승인/반려 메서드가 없고, 대신
 * {@link #hide(ShopId, String)}/{@link #unhide(ShopId)} 사후 조치 경로가 있다.
 *
 * <p>본문 길이 검증을 이 서비스가 소유하는 이유는 ceo({@code PUT})만 본문을 쓰지만, 검증 없이
 * 저장하면 {@code VARCHAR(500)} 초과가 DB 제약 위반(500)으로 터져 점주에게 "무엇이 잘못됐는지"
 * 전달되지 않기 때문이다. Bean Validation({@code @Size})으로도 잡히지만, 그것은 presentation
 * 계약이라 다른 경로(배치·관리자 대행 입력)가 추가되면 우회된다.
 */
public class ShopOrderNoticeService {

    /**
     * 주문안내 본문 최대 길이. {@code SHOP_ORDER_NOTICE.content}의 {@code VARCHAR(500)}과 일치시킨다 —
     * 어긋나면 검증을 통과한 값이 DB에서 잘리거나 제약 위반으로 터진다.
     */
    private static final int MAX_CONTENT_LENGTH = 500;

    private final ShopOrderNoticeRepository shopOrderNoticeRepository;

    public ShopOrderNoticeService(ShopOrderNoticeRepository shopOrderNoticeRepository) {
        this.shopOrderNoticeRepository = shopOrderNoticeRepository;
    }

    /**
     * 가게의 주문안내를 전체교체한다(없으면 등록, 있으면 본문 갱신).
     *
     * <p>{@code SHOP_ORDER_NOTICE.shop_id}에 유니크 제약이 있어 "가게당 1건"은 DB가 최종 보장한다.
     * 이 메서드의 선행 조회는 그 제약을 대신하는 것이 아니라, <b>중복 등록 시도를 500(제약 위반)이
     * 아니라 정상 수정으로 처리</b>하기 위한 것이다.
     *
     * <p>기존 행의 게시중단 상태는 유지된다 — 사유는 {@code ShopOrderNotice#updateContent} 참조.
     *
     * @return 저장된 주문안내 (신규였다면 식별자가 채워진 상태)
     */
    public ShopOrderNotice upsert(ShopId shopId, String content) {
        String validated = validateContent(content);

        return shopOrderNoticeRepository.findByShopId(shopId)
            .map(existing -> {
                existing.updateContent(validated);
                return shopOrderNoticeRepository.save(existing);
            })
            .orElseGet(() -> shopOrderNoticeRepository.save(ShopOrderNotice.of(shopId, validated)));
    }

    /**
     * 관리자가 규정 위반 주문안내를 게시중단한다.
     *
     * <p>이미 게시중단된 문구에 다시 호출되면 사유만 갱신된다 — 관리자가 사유를 정정하는 것을 막을
     * 이유가 없고, {@code ShopNotice}처럼 {@code ALREADY_HIDDEN} 예외를 두면 사유 수정 경로가 따로
     * 필요해진다. 주문안내는 가게당 1건이라 대상이 유일하므로 이 관용이 모호함을 만들지 않는다.
     *
     * @throws ResourceNotFoundException 주문안내가 없는 가게인 경우
     */
    public ShopOrderNotice hide(ShopId shopId, String reason) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.hide(reason);
        return shopOrderNoticeRepository.save(notice);
    }

    /**
     * 게시중단을 해제해 다시 손님에게 노출한다. 해제 후 문구는 점주가 마지막으로 저장한 그대로다.
     *
     * @throws ResourceNotFoundException 주문안내가 없는 가게인 경우
     */
    public ShopOrderNotice unhide(ShopId shopId) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.unhide();
        return shopOrderNoticeRepository.save(notice);
    }

    /**
     * 본문을 검증하고 앞뒤 공백을 제거한 값을 돌려준다.
     *
     * <p><b>trim 후에 길이를 센다.</b> 프론트 textarea는 개행·공백을 그대로 보내므로, trim 전 길이로
     * 재면 화면상 500자 이내인 문구가 거부될 수 있다.
     *
     * @throws BusinessException 본문이 비었거나({@code SHOP_ORDER_NOTICE_CONTENT_REQUIRED})
     *                           500자를 넘는 경우({@code SHOP_ORDER_NOTICE_CONTENT_TOO_LONG})
     */
    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_NOTICE_CONTENT_REQUIRED);
        }

        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_NOTICE_CONTENT_TOO_LONG);
        }
        return trimmed;
    }

    /**
     * 게시중단 조작의 대상 주문안내를 로드한다. 미설정 가게에 게시중단을 시도하는 것은 대상이 없는
     * 조작이므로 404다.
     *
     * <p>{@code SHOP_NOT_FOUND}를 재사용하지 않고 {@code SHOP_ORDER_NOTICE_NOT_FOUND}를 쓴다 —
     * 가게는 실재하는데 "가게를 찾을 수 없습니다"가 나가면 관리자가 원인을 엉뚱한 곳에서 찾는다.
     * 스펙 표에 없는 상수지만, 없는 대상에 게시중단을 시도한 상황을 그대로 가리키는 코드가 없으면
     * 로그와 응답이 서로 다른 이야기를 하게 된다.
     */
    private ShopOrderNotice loadByShopId(ShopId shopId) {
        return shopOrderNoticeRepository.findByShopId(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_ORDER_NOTICE_NOT_FOUND));
    }
}

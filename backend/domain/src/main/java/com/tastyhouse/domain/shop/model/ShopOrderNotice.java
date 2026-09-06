package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;

/**
 * 주문안내(메뉴판 최상단 안내 문구) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopOrderNoticeJpaEntity} + {@code ShopOrderNoticeMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopOrderNoticeRepository#save}를 호출해야 한다.
 *
 * <p><b>{@code ShopNotice}(점주 공지)와 별개 애그리거트인 이유</b>: 점주 공지는 가게정보 화면의 공지이고
 * 주문안내는 메뉴판 최상단 문구다. 자리가 다르고 노출 규칙도 다르다 — 공지는 여러 건을 등록해 그중
 * 1건만 앱에 반영하는 {@code exposed} 토글을 갖지만, 주문안내는 <b>가게당 항상 1건</b>이라 토글할
 * 대상이 없다. 그래서 상태 필드가 {@code hidden} 하나뿐이다.
 *
 * <p><b>승인 절차가 없다.</b> 메뉴모음컷·사장님 추천과 달리 주문안내는 점주가 저장하면 즉시 손님에게
 * 보인다 — 출처 PDF가 주문안내를 "편집 가능 항목"으로, 메뉴모음컷·사장님 추천만 "승인 필요"로 분류하기
 * 때문이다. 승인 대기 상태가 없으므로 {@code PENDING}/{@code APPROVED} 같은 상태 열거형도 없고, 대신
 * 규정 위반은 관리자가 사후에 {@link #hide(String)}로 내린다({@code ShopNotice.hidden}과 동형).
 *
 * <p><b>금지어 자동 판정을 하지 않는 이유</b>: 전화주문 유도·계좌이체 유도 같은 등록 금지 기준은 문구
 * 전체의 맥락으로 판단해야 하고, 오탐이 점주 영업을 막는 비용이 수동 검수 비용보다 크다. PDF도 이를
 * 자동 차단이 아니라 "수정 요청 및 삭제 조치"라는 사후 조치로 규정한다.
 */
public class ShopOrderNotice {

    private final ShopOrderNoticeId id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private String content;
    private boolean hidden; // 관리자 게시중단
    private String hiddenReason; // hidden = true 일 때만 값 존재
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopOrderNotice(
        ShopOrderNoticeId id,
        ShopId shopId,
        String content,
        boolean hidden,
        String hiddenReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.content = content;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 주문안내를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>본문 길이 검증은 이 팩토리가 아니라 {@code ShopOrderNoticeService}가 소유한다 — 등록(신규)과
     * 수정(기존 행 갱신) 두 경로가 같은 규칙을 쓰는데, 팩토리에만 두면 수정 경로가 검증을 우회한다.
     */
    public static ShopOrderNotice of(ShopId shopId, String content) {
        return new ShopOrderNotice(null, shopId, content, false, null, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopOrderNotice reconstitute(
        ShopOrderNoticeId id,
        ShopId shopId,
        String content,
        boolean hidden,
        String hiddenReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopOrderNotice(id, shopId, content, hidden, hiddenReason, createdAt, updatedAt);
    }

    /**
     * 본문을 교체한다. 길이 검증을 거친 값만 넘어오도록 {@code ShopOrderNoticeService}를 경유한다.
     *
     * <p>게시중단 상태는 건드리지 않는다 — 점주가 문구를 고쳤다는 사실만으로 관리자 조치가 풀리면
     * 한 글자 수정으로 게시중단을 무력화할 수 있다. 해제는 관리자만 할 수 있다({@link #unhide()}).
     */
    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 관리자가 규정 위반으로 게시중단한다. 사유를 함께 남긴다 — 조치 근거 없이 내려가면 점주가 무엇을
     * 고쳐야 하는지 알 수 없고, 사유는 점주 조회(C-1) 응답으로 그대로 내려간다.
     */
    public void hide(String reason) {
        this.hidden = true;
        this.hiddenReason = reason;
    }

    /**
     * 게시중단을 해제한다. 사유도 함께 비운다 — 게시중인 문구에 남은 과거 사유는 점주 화면에서
     * 아직 조치 중인 것처럼 읽힌다(이력은 {@code SHOP_CHANGE_HISTORY}에 남으므로 유실이 아니다).
     */
    public void unhide() {
        this.hidden = false;
        this.hiddenReason = null;
    }

    public ShopOrderNoticeId getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public String getHiddenReason() {
        return this.hiddenReason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}

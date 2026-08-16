package com.tastyhouse.domain.ceo.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;

/**
 * 자주 쓰는 문구 순수 도메인 모델 — 점주가 사장님 답변 작성 시 골라 넣을 문구를 미리 등록해 둔 것.
 *
 * <p><b>귀속은 가게가 아니라 점주 계정({@code ceoId})이다.</b> 한 점주가 여러 가게를 맡아도 문구를
 * 공유한다 — 문구는 사람이 쓰는 말버릇에 가깝지 가게 설정이 아니기 때문이다. 그래서 이 애그리거트는
 * review가 아니라 ceo 컨텍스트가 소유한다({@code ShopId}를 갖지 않는다).
 *
 * <p><b>{@code name}은 nullable이며 표시명을 저장하지 않는다.</b> 이름을 비우면 화면이 내용 앞부분을
 * 대신 보여주는데, 그 파생값을 여기에 저장하면 내용을 수정할 때 어긋난다. 파생은 표현 계층
 * ({@code CeoReplyPhraseQueryService}의 private 매퍼)이 조회 시점에 계산한다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code CeoReplyPhraseJpaEntity} + {@code CeoReplyPhraseMapper}가 담당하며, 도메인이 프레임워크-프리
 * 이므로 변경 후 저장은 더티 체킹이 아니라 명시적 {@code save} 호출이다.
 */
public class CeoReplyPhrase {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final CeoId ceoId;
    private String name; // 점주가 입력한 문구 이름. 미입력 시 null
    private String content;
    private final int sort;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private CeoReplyPhrase(
        Long id,
        CeoId ceoId,
        String name,
        String content,
        int sort,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.ceoId = ceoId;
        this.name = name;
        this.content = content;
        this.sort = sort;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 문구를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>{@code sort}는 클라이언트가 보내지 않고 등록 시점의 보유 건수로 서버가 채운다
     * ({@code CeoReplyPhraseService}).
     */
    public static CeoReplyPhrase of(CeoId ceoId, String name, String content, int sort) {
        return new CeoReplyPhrase(null, ceoId, name, content, sort, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static CeoReplyPhrase reconstitute(
        Long id,
        CeoId ceoId,
        String name,
        String content,
        int sort,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new CeoReplyPhrase(id, ceoId, name, content, sort, createdAt, updatedAt);
    }

    /**
     * 문구의 이름과 내용을 수정한다. 소유 점주({@code ceoId})와 정렬 순서({@code sort})는 바뀌지
     * 않는다 — 수정은 내용 교정이지 재배치가 아니다.
     */
    public void updateContent(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public int getSort() {
        return this.sort;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}

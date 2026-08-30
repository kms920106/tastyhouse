package com.tastyhouse.infrastructure.ceo.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 자주 쓰는 문구 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code CeoReplyPhrase}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code CeoReplyPhraseMapper}가 수행한다.
 *
 * <p>{@code content}는 {@code TEXT}가 아니라 {@code VARCHAR(1000)}이다 — 상한 1,000자가 확정돼 있어
 * DB가 직접 보증한다. {@code schema.sql}의 길이와 일치시킨다.
 *
 * <p>{@code ceo_id}는 크로스-애그리거트 FK라 raw {@code Long}이다({@code @Convert} 미사용) — VO 매핑은
 * QueryDSL이 {@code NumberPath<Long>} 대신 VO path를 만들게 해 query DAO 조인·투영을 깨뜨린다.
 */
@Entity
@Table(name = "CEO_REPLY_PHRASE")
public class CeoReplyPhraseJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "name", length = 50)
    private String name; // 미입력 시 NULL — 화면이 내용 앞부분을 대신 표시한다

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "sort", nullable = false)
    private int sort;

    protected CeoReplyPhraseJpaEntity() {
    }

    private CeoReplyPhraseJpaEntity(Long ceoId, String name, String content, int sort) {
        this.ceoId = ceoId;
        this.name = name;
        this.content = content;
        this.sort = sort;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code CeoReplyPhraseMapper#toEntity}에서만 호출한다.
     */
    static CeoReplyPhraseJpaEntity create(Long ceoId, String name, String content, int sort) {
        return new CeoReplyPhraseJpaEntity(ceoId, name, content, sort);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·
     * 소유 점주·정렬 순서는 수정 대상이 아니므로 건드리지 않는다.
     */
    void applyChanges(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public Long getCeoId() {
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
}

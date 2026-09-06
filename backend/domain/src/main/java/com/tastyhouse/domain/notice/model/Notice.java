package com.tastyhouse.domain.notice.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.notice.vo.NoticeId;

/**
 * 공지사항 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code NoticeJpaEntity} + {@code NoticeMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code NoticeRepository#save}를
 * 호출해야 한다.
 */
public class Notice {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private String title; // 공지사항 제목
    private String content; // 공지사항 본문 내용
    private boolean visible; // 노출 여부 (true: 노출)
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Notice(
        Long id,
        String title,
        String content,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 공지사항을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Notice of(String title, String content, boolean visible) {
        return new Notice(null, title, content, visible, false, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Notice reconstitute(
        Long id,
        String title,
        String content,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Notice(id, title, content, visible, deleted, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public NoticeId getNoticeId() {
        return NoticeId.of(this.id);
    }

    public void update(String title, String content, boolean visible) {
        this.title = title;
        this.content = content;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }
}

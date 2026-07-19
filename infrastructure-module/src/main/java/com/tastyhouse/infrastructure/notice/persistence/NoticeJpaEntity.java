package com.tastyhouse.infrastructure.notice.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 공지사항 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Notice}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code NoticeMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "NOTICE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 공지사항 제목

    @Column(name = "content", nullable = false, length = 1000)
    private String content; // 공지사항 본문 내용

    @Column(name = "is_visible", nullable = false)
    private boolean visible; // 노출 여부 (true: 노출)

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted; // 삭제 여부 (true: 삭제됨, Soft Delete)

    private NoticeJpaEntity(String title, String content, boolean visible, boolean deleted) {
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.deleted = deleted;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code NoticeMapper#toEntity}에서만 호출한다.
     */
    static NoticeJpaEntity create(String title, String content, boolean visible, boolean deleted) {
        return new NoticeJpaEntity(title, content, visible, deleted);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(String title, String content, boolean visible, boolean deleted) {
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.deleted = deleted;
    }
}

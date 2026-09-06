package com.tastyhouse.domain.faq.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;

/**
 * FAQ 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code FaqJpaEntity} + {@code FaqMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code FaqRepository#save}를
 * 호출해야 한다.
 */
public class Faq {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private FaqCategoryId faqCategoryId;
    private String question;
    private String answer;
    private Integer sort;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Faq(
        Long id,
        FaqCategoryId faqCategoryId,
        String question,
        String answer,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 FAQ를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static Faq of(FaqCategoryId faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        return new Faq(null, faqCategoryId, question, answer, sort, visible, false, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Faq reconstitute(
        Long id,
        FaqCategoryId faqCategoryId,
        String question,
        String answer,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Faq(id, faqCategoryId, question, answer, sort, visible, deleted, createdAt, updatedAt);
    }

    public FaqId getFaqId() {
        return FaqId.of(this.id);
    }

    public void update(FaqCategoryId faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public FaqCategoryId getFaqCategoryId() {
        return this.faqCategoryId;
    }

    public String getQuestion() {
        return this.question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public Integer getSort() {
        return this.sort;
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
}

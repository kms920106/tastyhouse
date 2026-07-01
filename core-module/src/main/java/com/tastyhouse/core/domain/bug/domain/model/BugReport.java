package com.tastyhouse.core.domain.bug.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(
    name = "BUG_REPORT",
    indexes = {
        @Index(name = "idx_bug_report_member_id", columnList = "member_id")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BugReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "device", nullable = false, length = 100)
    private String device;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    private BugReport(Long memberId, String device, String title, String content) {
        this.memberId = memberId;
        this.device = device;
        this.title = title;
        this.content = content;
    }

    public static BugReport create(Long memberId, String device, String title, String content) {
        return new BugReport(memberId, device, title, content);
    }
}

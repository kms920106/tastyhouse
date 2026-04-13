package com.tastyhouse.core.entity.report;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private String device; // 단말기 (iPhone 18, Galaxy 24SE 등)

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 제목

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    private BugReport(
        Long memberId,
        String device,
        String title,
        String content
    ) {
        this.memberId = memberId;
        this.device = device;
        this.title = title;
        this.content = content;
    }

    public static BugReport of(
        Long memberId,
        String device,
        String title,
        String content
    ) {
        return new BugReport(
            memberId,
            device,
            title,
            content
        );
    }
}

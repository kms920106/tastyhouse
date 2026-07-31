package com.tastyhouse.domain.bug.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BugReportImageTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientBugReportImage() {
        BugReportImage image = BugReportImage.of(1L, 10L, 0);

        assertThat(image.getId()).isNull();
        assertThat(image.getBugReportId()).isEqualTo(1L);
        assertThat(image.getImageFileId()).isEqualTo(10L);
        assertThat(image.getSort()).isEqualTo(0);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        BugReportImage image = BugReportImage.reconstitute(5L, 1L, 10L, 0);

        assertThat(image.getId()).isEqualTo(5L);
        assertThat(image.getBugReportId()).isEqualTo(1L);
        assertThat(image.getImageFileId()).isEqualTo(10L);
        assertThat(image.getSort()).isEqualTo(0);
    }
}

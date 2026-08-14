package com.tastyhouse.domain.ceo.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.model.CeoLoginFailureReason;
import com.tastyhouse.domain.ceo.model.CeoLoginHistory;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;
import com.tastyhouse.domain.ceo.vo.CeoId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 점주 로그인 이력 기록기 단위 테스트. Spring 컨텍스트 없이 fake 포트만으로 검증한다.
 */
class CeoLoginHistoryRecorderTest {

    private static final int USER_AGENT_MAX_LENGTH = 500;

    @Test
    @DisplayName("recordSuccess는 SUCCESS 이력 1행을 남기고 실패 사유는 비운다")
    void recordSuccess_savesSuccessRowWithoutFailureReason() {
        FakeCeoLoginHistoryRepository repository = new FakeCeoLoginHistoryRepository();
        CeoLoginHistoryRecorder recorder = new CeoLoginHistoryRecorder(repository);

        recorder.recordSuccess(CeoId.of(7L), "121.130.11.24", "Mozilla/5.0");

        assertThat(repository.saved).hasSize(1);
        CeoLoginHistory history = repository.saved.getFirst();
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(7L));
        assertThat(history.getResult()).isEqualTo(CeoLoginResult.SUCCESS);
        assertThat(history.getFailureReason()).isNull();
        assertThat(history.getIpAddress()).isEqualTo("121.130.11.24");
        assertThat(history.getUserAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("recordFailure는 FAILURE 이력 1행을 사유와 함께 남긴다")
    void recordFailure_savesFailureRowWithReason() {
        FakeCeoLoginHistoryRepository repository = new FakeCeoLoginHistoryRepository();
        CeoLoginHistoryRecorder recorder = new CeoLoginHistoryRecorder(repository);

        recorder.recordFailure(CeoId.of(7L), CeoLoginFailureReason.BAD_CREDENTIALS, "10.0.0.1", "curl/8.4.0");

        assertThat(repository.saved).hasSize(1);
        CeoLoginHistory history = repository.saved.getFirst();
        assertThat(history.getResult()).isEqualTo(CeoLoginResult.FAILURE);
        assertThat(history.getFailureReason()).isEqualTo(CeoLoginFailureReason.BAD_CREDENTIALS);
    }

    @Test
    @DisplayName("500자를 넘는 User-Agent는 500자로 절단해 저장한다")
    void record_truncatesUserAgentToColumnLength() {
        FakeCeoLoginHistoryRepository repository = new FakeCeoLoginHistoryRepository();
        CeoLoginHistoryRecorder recorder = new CeoLoginHistoryRecorder(repository);
        String longUserAgent = "a".repeat(USER_AGENT_MAX_LENGTH + 120);

        recorder.recordSuccess(CeoId.of(7L), "10.0.0.1", longUserAgent);

        String saved = repository.saved.getFirst().getUserAgent();
        assertThat(saved).hasSize(USER_AGENT_MAX_LENGTH);
        assertThat(saved).isEqualTo(longUserAgent.substring(0, USER_AGENT_MAX_LENGTH));
    }

    @Test
    @DisplayName("500자 이하 User-Agent는 그대로 저장하고, null도 그대로 둔다")
    void record_keepsShortOrNullUserAgentAsIs() {
        FakeCeoLoginHistoryRepository repository = new FakeCeoLoginHistoryRepository();
        CeoLoginHistoryRecorder recorder = new CeoLoginHistoryRecorder(repository);
        String exactLengthUserAgent = "b".repeat(USER_AGENT_MAX_LENGTH);

        recorder.recordSuccess(CeoId.of(7L), "10.0.0.1", exactLengthUserAgent);
        recorder.recordSuccess(CeoId.of(7L), "10.0.0.1", null);

        assertThat(repository.saved.get(0).getUserAgent()).isEqualTo(exactLengthUserAgent);
        assertThat(repository.saved.get(1).getUserAgent()).isNull();
    }

    /**
     * 로그인 이력 write 포트 fake — domain-module에는 Mockito 의존이 없어 손으로 만든다.
     */
    private static class FakeCeoLoginHistoryRepository implements CeoLoginHistoryRepository {

        private final List<CeoLoginHistory> saved = new ArrayList<>();

        @Override
        public CeoLoginHistory save(CeoLoginHistory ceoLoginHistory) {
            saved.add(ceoLoginHistory);
            return ceoLoginHistory;
        }
    }
}

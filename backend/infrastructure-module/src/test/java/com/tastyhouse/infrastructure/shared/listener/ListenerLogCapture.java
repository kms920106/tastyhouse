package com.tastyhouse.infrastructure.shared.listener;

import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

/**
 * 로그만 남기는 도메인 이벤트 리스너를 검증하기 위한 로그 캡처 유틸.
 *
 * <p>인프라 리스너 9개 중 6개는 협력자 없이 {@code log.info(...)}만 수행한다. 이런 리스너에서
 * "무엇을 하는지"는 곧 "무엇을 기록하는지"이므로, 로그를 관측하지 않으면 <b>핸들러 본문을 통째로
 * 지워도 통과하는</b> 공허한 테스트만 남는다. 그래서 Logback {@link ListAppender}를 대상 로거에 직접
 * 붙여, 이벤트의 어떤 값이 기록에 반영되는지까지 봉인한다.
 *
 * <p>{@code @SpringBootTest} 없이 동작한다 — Logback은 SLF4J 바인딩이라 로거 조작에 스프링 컨텍스트가
 * 필요 없고, 이 모듈의 리스너 테스트는 전부 순수 단위 테스트다(스프링 배선·AFTER_COMMIT 발화 자체는
 * 프레임워크 몫이라 검증 대상이 아니다).
 *
 * <p>사용 후에는 반드시 {@link #detach()}를 호출한다(JUnit {@code @AfterEach}). 루트가 아니라 대상
 * 클래스의 로거에 붙이므로 테스트 간 간섭은 없지만, 떼지 않으면 같은 로거를 쓰는 다른 테스트가
 * 실행될 때 이벤트가 계속 쌓인다.
 */
public final class ListenerLogCapture {

    private final Logger logger;
    private final ListAppender<ILoggingEvent> appender;

    private ListenerLogCapture(Logger logger, ListAppender<ILoggingEvent> appender) {
        this.logger = logger;
        this.appender = appender;
    }

    /**
     * 지정한 클래스의 로거에 캡처 appender를 붙인다.
     *
     * <p>대상 로거의 레벨을 {@code INFO}로 명시해, 실행 환경의 로그 설정(운영 전환으로 레벨을 올린
     * 경우 등)에 따라 테스트가 흔들리지 않게 한다.
     */
    public static ListenerLogCapture attachTo(Class<?> target) {
        Logger logger = (Logger) LoggerFactory.getLogger(target);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
        return new ListenerLogCapture(logger, appender);
    }

    public void detach() {
        logger.detachAppender(appender);
        appender.stop();
    }

    public List<ILoggingEvent> events() {
        return List.copyOf(appender.list);
    }

    /**
     * 캡처된 로그가 정확히 한 건일 때 그 메시지를 파라미터까지 치환해 반환한다.
     *
     * <p>{@code getFormattedMessage()}를 쓰는 이유는, 이벤트의 어떤 <b>값</b>이 기록에 반영되는지까지
     * 확인하기 위해서다 — 포맷 문자열만 보면 파라미터를 잘못 넘겨도 드러나지 않는다.
     */
    public String singleFormattedMessage() {
        List<ILoggingEvent> events = events();
        if (events.size() != 1) {
            throw new AssertionError("로그가 정확히 1건이어야 합니다. 실제: " + events.size() + "건");
        }
        return events.getFirst().getFormattedMessage();
    }
}

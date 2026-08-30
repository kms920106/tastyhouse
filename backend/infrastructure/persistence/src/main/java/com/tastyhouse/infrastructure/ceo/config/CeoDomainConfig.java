package com.tastyhouse.infrastructure.ceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.ceo.port.ReplyPhraseTextValidator;
import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;
import com.tastyhouse.domain.ceo.repository.CeoReplyPhraseRepository;
import com.tastyhouse.domain.ceo.service.CeoLoginHistoryRecorder;
import com.tastyhouse.domain.ceo.service.CeoReplyPhraseService;

/**
 * ceo 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class CeoDomainConfig {

    /**
     * 점주 로그인 이력 기록 — 개인정보처리시스템 접속기록. ceo-api의
     * {@code CeoLoginHistoryCommandService}가 트랜잭션 경계를 열고 호출한다.
     */
    @Bean
    public CeoLoginHistoryRecorder ceoLoginHistoryRecorder(
        CeoLoginHistoryRepository ceoLoginHistoryRepository
    ) {
        return new CeoLoginHistoryRecorder(ceoLoginHistoryRepository);
    }

    /**
     * 자주 쓰는 문구 등록·수정·삭제 불변식 — 5개 상한(앱 강제)·소유권·금칙어 검수. ceo-api의
     * {@code CeoReplyPhraseCommandService}가 트랜잭션 경계를 열고 호출한다.
     *
     * <p>금칙어 검수는 shop 컨텍스트가 소유한 무상태 정책을 재사용하되, 컨텍스트 경계를 넘지 않도록
     * 출력 포트 {@link ReplyPhraseTextValidator}로 주입한다 — 그 구현
     * ({@code ReplyPhraseProhibitedWordValidatorAdapter})이 {@code ProhibitedWordValidator}에 위임하므로
     * 규칙은 복제되지 않는다.
     */
    @Bean
    public CeoReplyPhraseService ceoReplyPhraseService(
        CeoReplyPhraseRepository ceoReplyPhraseRepository,
        ReplyPhraseTextValidator replyPhraseTextValidator
    ) {
        return new CeoReplyPhraseService(ceoReplyPhraseRepository, replyPhraseTextValidator);
    }
}

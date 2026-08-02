package com.tastyhouse.domain.shared.exception;

/**
 * 낙관적 락 충돌을 나타내는 프레임워크-프리 예외.
 *
 * <p>infrastructure-module의 write 어댑터가 Spring/Hibernate의
 * {@code ObjectOptimisticLockingFailureException}을 catch해 이 예외로 번역·rethrow한다.
 * core-module의 재시도 판별 로직(예: {@code ReservationCommandService})은
 * spring-orm 예외 대신 이 예외를 참조한다.
 *
 * <p>목적: core-module이 {@code spring-orm}에 의존하지 않도록 하여
 * 최종 정리 단계에서 해당 의존을 제거할 수 있게 한다.
 */
public class OptimisticLockConflictException extends RuntimeException {

    public OptimisticLockConflictException(String message) {
        super(message);
    }

    public OptimisticLockConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}

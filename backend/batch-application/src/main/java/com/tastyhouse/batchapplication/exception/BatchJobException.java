package com.tastyhouse.batchapplication.exception;

/**
 * 배치 잡(크롤링·집계 등) 실행 실패를 나타내는 예외.
 *
 * <p>batch-module은 HTTP 경계가 없어 응답 계약이 존재하지 않으므로, 도메인의
 * {@code BusinessException}/{@code ErrorCode} 체계(상태코드·응답 code를 담는 구조)를 쓰지 않는다.
 * 대신 raw {@code RuntimeException} 대신 이 타입을 던져, 배치 실패를 호출부·로그에서 식별할 수 있게 한다.
 *
 * <p>스케줄러는 이 예외를 잡아 로그로 남기고 다음 주기에 재실행하므로(잡 단위 격리) 예외가 밖으로
 * 전파되지 않는다.
 */
public class BatchJobException extends RuntimeException {

    public BatchJobException(String message, Throwable cause) {
        super(message, cause);
    }
}

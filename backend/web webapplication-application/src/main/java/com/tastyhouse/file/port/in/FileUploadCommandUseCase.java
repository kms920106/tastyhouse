package com.tastyhouse..file.port.in;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 유스케이스(인바운드 포트).
 *
 * <p>{@code MultipartFile}을 파라미터로 받는 것은 업로드 경계 타입이라 허용되는 형태다
 * (Command 필드로 싣는 것만 금지 — {@code commandRecordsShouldNotHoldMultipartFile}).
 * 업로드 결과는 파일 식별자 하나이므로 별도 Command·Response record를 두지 않는다.
 */
public interface FileUploadCommandUseCase {

    Long upload(MultipartFile file);
}

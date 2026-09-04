package com.tastyhouse.ceoapplication.file.port.in;

import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드 유스케이스(인바운드 포트).
 *
 * <p>업로드 규칙 본체(허용 확장자·용량 한도·저장 경로·이벤트 발행)는 프레임워크-프리 도메인 서비스
 * {@code FileUploadService}가 소유하고, 이 포트의 구현체는 {@code MultipartFile} 어댑팅과
 * 트랜잭션 경계 선언만 담당한다. 과거에는 api-common-module의 {@code FileService} 한 벌이 그 역할을
 * 겸했는데, 표현 모듈이 {@code @Transactional} 유스케이스를 갖는 데다 application 계층이 그것을
 * 주입받아 application→표현 역방향 의존이 생겼다.
 *
 * <p>{@code MultipartFile}을 파라미터로 받는 것은 업로드 경계 타입이라 허용되는 형태다
 * (Command <b>필드</b>로 싣는 것만 금지 — {@code commandRecordsShouldNotHoldMultipartFile}).
 * 업로드 결과가 파일 식별자 하나뿐이라 별도 Command·Response record를 두지 않는다.
 */
public interface FileUploadOwnerCommandUseCase {

    Long upload(MultipartFile file);
}

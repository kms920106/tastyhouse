package com.tastyhouse.domain.file.repository;

import java.util.Optional;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.vo.UploadedFileId;

/**
 * 업로드 파일 write 포트.
 *
 * <p>업로드 경로가 필요로 하는 저장·단건 로드만 노출한다. 표현 목적 조회(응답에 실을 파일 URL)는
 * 이 포트를 거치지 않는다 — 각 도메인 query DAO가 {@code uploaded_file}을 join해 경로를 가져오고
 * infrastructure-module의 {@code FileUrlResolver}가 표시용 URL로 바꾼 상태로 Result에 담는다.
 *
 * <p>과거에는 응답 URL 변환의 재료를 얻으려고 {@code findFilePath}(단건)·{@code findFilePaths}(배치)를
 * 여기 두었으나, 그 둘은 화면에 뿌릴 값을 얻기 위한 조회여서 "write 포트 잔류 판정 기준"(불변식
 * 검증·상태 전이에 필요한 조회만 write 포트에 남긴다)에 맞지 않았고, 조회 전환 후 호출부가 0이 되어
 * 제거했다.
 */
public interface UploadedFileRepository {

    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(UploadedFileId id);
}

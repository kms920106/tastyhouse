package com.tastyhouse.domain.file.domain.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.tastyhouse.domain.file.domain.model.UploadedFile;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;

public interface UploadedFileRepository {

    UploadedFile save(UploadedFile uploadedFile);

    Optional<UploadedFile> findById(UploadedFileId id);

    /**
     * 저장된 파일 경로만 조회한다. 응답 URL 변환의 재료로 전 모듈이 사용하며, 표현용 Result DTO가
     * 아니라 값(경로 문자열)을 반환하므로 query DAO가 아닌 write 포트에 둔다(공통 지침 "write 포트
     * 잔류 판정 기준").
     */
    default Optional<String> findFilePath(UploadedFileId id) {
        if (id == null) {
            return Optional.empty();
        }
        return findById(id).map(UploadedFile::getFilePath);
    }

    /**
     * 여러 파일의 경로를 식별자별로 한 번에 조회한다. 목록 응답 조립처럼 파일 식별자가 N개인 경로에서
     * {@link #findFilePath}를 반복 호출하면 N번 쿼리가 나가므로(N+1), 그 자리를 이 배치 조회로 대체한다.
     *
     * <p>입력이 비어 있으면 빈 맵을 돌려주며, 존재하지 않는 식별자는 결과 맵에 키가 없다(호출부는
     * {@code get}이 {@code null}인 경우를 단건 조회의 {@code Optional.empty()}와 같게 다룬다).
     */
    Map<UploadedFileId, String> findFilePaths(Collection<UploadedFileId> ids);
}

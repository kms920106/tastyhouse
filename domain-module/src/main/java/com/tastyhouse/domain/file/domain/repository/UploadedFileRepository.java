package com.tastyhouse.domain.file.domain.repository;

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
}

package com.tastyhouse.infrastructure.file.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.model.UploadedFile;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.vo.UploadedFileId;

/**
 * 업로드 파일 write 어댑터.
 *
 * <p>QueryDSL 없이 순수 pass-through이며, update 경로가 없어 {@code save}는 insert 전용이다.
 * 표현 목적 파일 조회(응답 URL)는 이 어댑터를 거치지 않는다 — 각 도메인 query DAO가
 * {@code uploaded_file}을 join하고 {@code FileUrlResolver}가 URL로 변환한다.
 */
@Repository
public class UploadedFileRepositoryImpl implements UploadedFileRepository {

    private final UploadedFileJpaRepository uploadedFileJpaRepository;

    public UploadedFileRepositoryImpl(UploadedFileJpaRepository uploadedFileJpaRepository) {
        this.uploadedFileJpaRepository = uploadedFileJpaRepository;
    }

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        UploadedFileJpaEntity saved = uploadedFileJpaRepository.save(UploadedFileMapper.toEntity(uploadedFile));
        return UploadedFileMapper.toDomain(saved);
    }

    @Override
    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileJpaRepository.findById(id.value()).map(UploadedFileMapper::toDomain);
    }
}

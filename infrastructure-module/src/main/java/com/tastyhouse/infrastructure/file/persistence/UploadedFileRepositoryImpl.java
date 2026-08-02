package com.tastyhouse.infrastructure.file.persistence;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.file.domain.model.UploadedFile;
import com.tastyhouse.domain.file.domain.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;

@Repository
@RequiredArgsConstructor
public class UploadedFileRepositoryImpl implements UploadedFileRepository {

    private final UploadedFileJpaRepository uploadedFileJpaRepository;

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        UploadedFileJpaEntity saved = uploadedFileJpaRepository.save(UploadedFileMapper.toEntity(uploadedFile));
        return UploadedFileMapper.toDomain(saved);
    }

    @Override
    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileJpaRepository.findById(id.value()).map(UploadedFileMapper::toDomain);
    }

    /**
     * 식별자 목록을 {@code IN} 한 번으로 조회해 경로 맵을 만든다(단건 조회 반복으로 인한 N+1 제거).
     * 경로가 없는 행은 맵에 넣지 않아, 호출부에서 단건 조회의 {@code Optional.empty()}와 같게 다뤄진다.
     */
    @Override
    public Map<UploadedFileId, String> findFilePaths(Collection<UploadedFileId> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        List<Long> rawIds = ids.stream()
            .filter(Objects::nonNull)
            .map(UploadedFileId::value)
            .distinct()
            .toList();

        if (rawIds.isEmpty()) {
            return Map.of();
        }

        Map<UploadedFileId, String> filePathById = new LinkedHashMap<>();
        uploadedFileJpaRepository.findAllById(rawIds).forEach(entity -> {
            if (entity.getId() != null && entity.getFilePath() != null) {
                filePathById.put(UploadedFileId.of(entity.getId()), entity.getFilePath());
            }
        });

        return filePathById;
    }
}

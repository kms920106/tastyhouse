package com.tastyhouse.core.domain.file.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.file.domain.model.UploadedFile;
import com.tastyhouse.core.domain.file.domain.model.UploadedFileId;
import com.tastyhouse.core.domain.file.domain.repository.UploadedFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;

@Repository
@RequiredArgsConstructor
public class UploadedFileRepositoryImpl implements UploadedFileRepository {

    private final UploadedFileJpaRepository uploadedFileJpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public UploadedFile save(UploadedFile file) {
        return uploadedFileJpaRepository.save(file);
    }

    @Override
    public Optional<UploadedFile> findById(Long id) {
        return uploadedFileJpaRepository.findById(id);
    }

    @Override
    public Optional<UploadedFile> findById(UploadedFileId id) {
        return uploadedFileJpaRepository.findById(id.value());
    }

    @Override
    public List<UploadedFile> findByIds(List<Long> ids) {
        return queryFactory
            .selectFrom(uploadedFile)
            .where(uploadedFile.id.in(ids))
            .orderBy(uploadedFile.createdAt.desc())
            .fetch();
    }
}

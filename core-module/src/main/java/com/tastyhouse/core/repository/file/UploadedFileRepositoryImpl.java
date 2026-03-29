package com.tastyhouse.core.repository.file;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.file.QUploadedFile;
import com.tastyhouse.core.entity.file.UploadedFile;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UploadedFileRepositoryImpl implements UploadedFileRepository {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public UploadedFile save(UploadedFile uploadedFile) {
        entityManager.persist(uploadedFile);
        return uploadedFile;
    }

    @Override
    public Optional<UploadedFile> findById(Long id) {
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        UploadedFile result = queryFactory
            .selectFrom(uploadedFile)
            .where(uploadedFile.id.eq(id))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<UploadedFile> findByIds(List<Long> ids) {
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        return queryFactory
            .selectFrom(uploadedFile)
            .where(uploadedFile.id.in(ids))
            .orderBy(uploadedFile.createdAt.desc())
            .fetch();
    }
}

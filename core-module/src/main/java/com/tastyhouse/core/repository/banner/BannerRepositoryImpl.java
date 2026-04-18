package com.tastyhouse.core.repository.banner;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.banner.BannerType;
import com.tastyhouse.core.entity.banner.QBanner;
import com.tastyhouse.core.entity.banner.dto.BannerListItemDto;
import com.tastyhouse.core.entity.file.QUploadedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BannerListItemDto> findAllByType(BannerType type, Pageable pageable) {
        QBanner banner = QBanner.banner;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        JPAQuery<BannerListItemDto> query = queryFactory
            .select(Projections.constructor(BannerListItemDto.class,
                banner.id,
                banner.title,
                uploadedFile.filePath,
                banner.linkUrl
            ))
            .from(banner)
            .join(uploadedFile).on(uploadedFile.id.eq(banner.uploadedFileId))
            .where(
                banner.type.eq(type),
                banner.active.isTrue(),
                banner.startDate.loe(LocalDateTime.now()),
                banner.endDate.goe(LocalDateTime.now())
            )
            .orderBy(banner.sort.asc());

        long total = query.fetch().size();

        List<BannerListItemDto> banners = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return new PageImpl<>(banners, pageable, total);
    }
}

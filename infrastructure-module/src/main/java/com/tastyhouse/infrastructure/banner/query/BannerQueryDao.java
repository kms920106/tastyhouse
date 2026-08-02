package com.tastyhouse.infrastructure.banner.query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.banner.domain.model.BannerType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.banner.persistence.QBannerJpaEntity.bannerJpaEntity;
import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;

/**
 * 배너 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code BannerRepository})와 역할이 겹치지 않는다. 소비 모듈(web-api/admin-api)의
 * {@code BannerQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>이미지 파일 조인({@code QUploadedFileJpaEntity})은 같은 모듈 내 참조라 Q타입을 직접 쓴다.
 * 조인으로 얻은 저장 경로는 {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다 —
 * {@code @QueryProjection}은 record 생성자로 직접 투영하므로 변환을 투영식에 끼울 수 없어, fetch
 * 직후 재조립한다. 변환 자체는 순수 문자열 연산이라 추가 쿼리가 발생하지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 메서드명에는 admin 마커를
 * 붙이지 않고 순수 동작명을 쓴다({@code findAllBanners}/{@code findDetailById}는 비노출·기간만료를
 * 포함한 관리 조회, {@code findVisibleBannersByType}은 현재 노출 중인 배너만).
 */
@Repository
public class BannerQueryDao {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public BannerQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 회원 노출용 배너 목록 조회 — 유형이 일치하고 노출(visible=true) 상태이며 현재 시각이 노출 기간
     * 안에 있는 배너만 조회한다. 이미지가 필수라 파일을 inner join 한다.
     */
    public PageResult<BannerListItemResult> findVisibleBannersByType(BannerType type, PageQuery pageQuery) {
        LocalDateTime now = LocalDateTime.now();

        Long total = queryFactory
            .select(bannerJpaEntity.id.count())
            .from(bannerJpaEntity)
            .where(
                bannerJpaEntity.type.eq(type),
                bannerJpaEntity.deleted.isFalse(),
                bannerJpaEntity.visible.isTrue(),
                bannerJpaEntity.startDate.loe(now),
                bannerJpaEntity.endDate.goe(now)
            )
            .fetchOne();

        List<BannerListItemResult> banners = queryFactory
            .select(new QBannerListItemResult(
                bannerJpaEntity.id,
                bannerJpaEntity.title,
                uploadedFileJpaEntity.filePath,
                bannerJpaEntity.linkUrl
            ))
            .from(bannerJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerImageFileId()))
            .where(
                bannerJpaEntity.type.eq(type),
                bannerJpaEntity.deleted.isFalse(),
                bannerJpaEntity.visible.isTrue(),
                bannerJpaEntity.startDate.loe(now),
                bannerJpaEntity.endDate.goe(now)
            )
            .orderBy(bannerJpaEntity.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 관리 목록 조회 — 비노출·노출기간 만료 배너를 포함하며 type/title 부분일치·visible 필터를
     * 적용한다. 이미지가 없을 수 있어 파일을 left join 한다.
     */
    public PageResult<BannerManagementListItemResult> findAllBanners(BannerSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(bannerJpaEntity.id.count())
            .from(bannerJpaEntity)
            .where(
                bannerJpaEntity.deleted.isFalse(),
                typeEq(condition.type()),
                titleContains(condition.title()),
                visibleEq(condition.visible())
            )
            .fetchOne();

        List<BannerManagementListItemResult> banners = queryFactory
            .select(new QBannerManagementListItemResult(
                bannerJpaEntity.id,
                bannerJpaEntity.type,
                bannerJpaEntity.title,
                uploadedFileJpaEntity.id,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath,
                bannerJpaEntity.linkUrl,
                bannerJpaEntity.startDate,
                bannerJpaEntity.endDate,
                bannerJpaEntity.sort,
                bannerJpaEntity.visible
            ))
            .from(bannerJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerImageFileId()))
            .where(
                bannerJpaEntity.deleted.isFalse(),
                typeEq(condition.type()),
                titleContains(condition.title()),
                visibleEq(condition.visible())
            )
            .orderBy(bannerJpaEntity.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 관리 상세 조회 — 비노출·노출기간 만료 배너도 조회된다. 이미지가 없을 수 있어 파일을 left join 한다.
     */
    public Optional<BannerDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        BannerDetailResult detail = queryFactory
            .select(new QBannerDetailResult(
                bannerJpaEntity.id,
                bannerJpaEntity.type,
                bannerJpaEntity.title,
                uploadedFileJpaEntity.id,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath,
                bannerJpaEntity.linkUrl,
                bannerJpaEntity.startDate,
                bannerJpaEntity.endDate,
                bannerJpaEntity.sort,
                bannerJpaEntity.visible,
                bannerJpaEntity.createdAt,
                bannerJpaEntity.updatedAt
            ))
            .from(bannerJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerImageFileId()))
            .where(bannerJpaEntity.id.eq(id), bannerJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail).map(this::withResolvedImageUrl);
    }

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. 아래 세 메서드는 {@code @QueryProjection}이
     * 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
    private BannerListItemResult withResolvedImageUrl(BannerListItemResult row) {
        return new BannerListItemResult(
            row.id(),
            row.title(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.linkUrl()
        );
    }

    private BannerManagementListItemResult withResolvedImageUrl(BannerManagementListItemResult row) {
        return new BannerManagementListItemResult(
            row.id(),
            row.type(),
            row.title(),
            row.imageFileId(),
            row.imageFileName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.linkUrl(),
            row.startDate(),
            row.endDate(),
            row.sort(),
            row.visible()
        );
    }

    private BannerDetailResult withResolvedImageUrl(BannerDetailResult row) {
        return new BannerDetailResult(
            row.id(),
            row.type(),
            row.title(),
            row.imageFileId(),
            row.imageFileName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.linkUrl(),
            row.startDate(),
            row.endDate(),
            row.sort(),
            row.visible(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    /**
     * {@code @Convert} VO 컬럼인 {@code BANNER.image_file_id}를 raw {@code Long}으로 비교하기 위한 path.
     */
    private NumberPath<Long> bannerImageFileId() {
        return Expressions.numberPath(Long.class, bannerJpaEntity, "imageFileId");
    }

    private BooleanExpression typeEq(BannerType type) {
        return type != null ? bannerJpaEntity.type.eq(type) : null;
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? bannerJpaEntity.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? bannerJpaEntity.visible.eq(visible) : null;
    }
}

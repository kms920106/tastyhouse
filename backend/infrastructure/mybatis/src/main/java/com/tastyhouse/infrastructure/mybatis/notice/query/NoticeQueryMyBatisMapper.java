package com.tastyhouse.infrastructure.mybatis.notice.query;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tastyhouse.application.notice.port.out.NoticeDetailResult;
import com.tastyhouse.application.notice.port.out.NoticeListItemResult;
import com.tastyhouse.application.notice.port.out.NoticeManagementListItemResult;

@Mapper
interface NoticeQueryMyBatisMapper {

    long countVisible();

    List<NoticeListItemResult> selectVisible(@Param("offset") long offset, @Param("limit") int limit);

    long countManagement(
        @Param("titlePattern") String titlePattern,
        @Param("contentPattern") String contentPattern,
        @Param("visible") Boolean visible
    );

    List<NoticeManagementListItemResult> selectManagement(
        @Param("titlePattern") String titlePattern,
        @Param("contentPattern") String contentPattern,
        @Param("visible") Boolean visible,
        @Param("offset") long offset,
        @Param("limit") int limit
    );

    Optional<NoticeDetailResult> selectDetailById(Long id);
}

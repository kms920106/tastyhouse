package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

@Mapper
interface NoticeMyBatisMapper {

    Optional<NoticeRow> selectActiveById(long id);

    void insert(NoticeWriteRow row);

    int update(NoticeWriteRow row);
}

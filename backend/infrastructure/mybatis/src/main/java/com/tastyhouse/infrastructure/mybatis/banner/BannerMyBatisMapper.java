package com.tastyhouse.infrastructure.mybatis.banner;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

@Mapper
interface BannerMyBatisMapper {

    Optional<BannerRow> selectActiveById(long id);

    void insert(BannerWriteRow row);

    int update(BannerWriteRow row);
}

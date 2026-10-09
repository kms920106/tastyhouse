package com.tastyhouse.infrastructure.mybatis;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@MapperScan(basePackageClasses = MyBatisModuleConfig.class, annotationClass = Mapper.class)
class MyBatisModuleConfig {
}

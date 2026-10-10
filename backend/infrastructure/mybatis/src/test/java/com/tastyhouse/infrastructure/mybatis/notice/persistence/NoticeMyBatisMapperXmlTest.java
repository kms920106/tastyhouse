package com.tastyhouse.infrastructure.mybatis.notice.persistence;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeMyBatisMapperXmlTest {

    private static final String RESOURCE = "mapper/notice/NoticeMyBatisMapper.xml";

    @Test
    @DisplayName("XML이 DB 없이 파싱되고 resultMap 생성자 매핑이 대상 타입과 맞는다")
    void parsesWithMatchingConstructors() throws Exception {
        Configuration configuration = parse();

        assertThat(configuration.getResultMapNames()).isNotEmpty();
        assertThat(configuration.getIncompleteResultMaps()).isEmpty();
        assertThat(configuration.getIncompleteStatements()).isEmpty();
    }

    @Test
    @DisplayName("namespace가 매퍼 인터페이스로 묶이고 매퍼 메서드마다 SQL이 하나씩 있다")
    void everyMapperMethodHasStatement() throws Exception {
        Configuration configuration = parse();

        assertThat(configuration.hasMapper(NoticeMyBatisMapper.class)).isTrue();
        Arrays.stream(NoticeMyBatisMapper.class.getDeclaredMethods())
            .map(Method::getName)
            .forEach(name -> assertThat(configuration.hasStatement(NoticeMyBatisMapper.class.getName() + "." + name))
                .as(name)
                .isTrue());
    }

    @Test
    @DisplayName("insert·update의 모든 바인딩 이름이 NoticeWriteRow 프로퍼티로 해석된다")
    void writeStatementsBindToWriteRowProperties() throws Exception {
        Configuration configuration = parse();
        NoticeWriteRow row = new NoticeWriteRow(
            7L, "제목", "본문", true, false, LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 2, 1, 0, 0));
        MetaObject meta = configuration.newMetaObject(row);

        for (String method : new String[] {"insert", "update"}) {
            BoundSql sql = configuration.getMappedStatement(NoticeMyBatisMapper.class.getName() + "." + method).getBoundSql(row);
            assertThat(sql.getParameterMappings()).as(method).isNotEmpty();
            sql.getParameterMappings().stream()
                .map(ParameterMapping::getProperty)
                .forEach(property -> assertThat(meta.hasGetter(property)).as(method + "." + property).isTrue());
        }
    }

    @Test
    @DisplayName("update는 created_at을 바꾸지 않고 삭제 여부와 무관하게 id로만 갱신한다")
    void updateKeepsCreatedAtAndIgnoresDeletedFlagInWhere() throws Exception {
        NoticeWriteRow row = new NoticeWriteRow(7L, "t", "c", true, false, null, null);

        String sql = parse().getMappedStatement(NoticeMyBatisMapper.class.getName() + ".update").getBoundSql(row).getSql();

        assertThat(sql).doesNotContain("created_at");
        assertThat(sql.substring(sql.indexOf("WHERE"))).doesNotContain("is_deleted");
    }

    private static Configuration parse() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream xml = Resources.getResourceAsStream(RESOURCE)) {
            new XMLMapperBuilder(xml, configuration, RESOURCE, configuration.getSqlFragments()).parse();
        }
        return configuration;
    }
}

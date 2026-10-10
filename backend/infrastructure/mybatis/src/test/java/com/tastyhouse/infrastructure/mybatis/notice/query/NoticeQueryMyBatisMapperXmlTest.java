package com.tastyhouse.infrastructure.mybatis.notice.query;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeQueryMyBatisMapperXmlTest {

    private static final String RESOURCE = "mapper/notice/NoticeQueryMyBatisMapper.xml";

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

        assertThat(configuration.hasMapper(NoticeQueryMyBatisMapper.class)).isTrue();
        Arrays.stream(NoticeQueryMyBatisMapper.class.getDeclaredMethods())
            .map(Method::getName)
            .forEach(name -> assertThat(configuration.hasStatement(NoticeQueryMyBatisMapper.class.getName() + "." + name))
                .as(name)
                .isTrue());
    }

    @Test
    @DisplayName("관리 검색 조건이 모두 비면 LIKE·노출 조건 없이 렌더링된다")
    void rendersManagementWithoutOptionalConditions() throws Exception {
        BoundSql sql = parse().getMappedStatement(statement("selectManagement"))
            .getBoundSql(managementParams(null, null, null));

        assertThat(sql.getSql()).doesNotContain("LIKE").doesNotContain("is_visible =").contains("LIMIT ? OFFSET ?");
        assertThat(propertiesOf(sql)).containsExactly("limit", "offset");
    }

    @Test
    @DisplayName("관리 검색 조건이 모두 있으면 ESCAPE '!' LIKE 두 개와 노출 조건이 붙는다")
    void rendersManagementWithAllConditions() throws Exception {
        BoundSql sql = parse().getMappedStatement(statement("selectManagement"))
            .getBoundSql(managementParams("%a%", "%b%", true));

        assertThat(sql.getSql())
            .contains("LOWER(title) LIKE ? ESCAPE '!'")
            .contains("LOWER(content) LIKE ? ESCAPE '!'")
            .contains("is_visible = ?")
            .contains("LIMIT ? OFFSET ?");
        assertThat(propertiesOf(sql)).containsExactly("titlePattern", "contentPattern", "visible", "limit", "offset");
    }

    @Test
    @DisplayName("관리 건수 조회는 목록과 같은 조건 조각을 쓴다")
    void countManagementSharesCondition() throws Exception {
        BoundSql sql = parse().getMappedStatement(statement("countManagement"))
            .getBoundSql(managementParams("%a%", "%b%", false));

        assertThat(propertiesOf(sql)).containsExactly("titlePattern", "contentPattern", "visible");
    }

    @Test
    @DisplayName("공개 목록은 삭제·비노출을 거르고 id 역순으로 페이징한다")
    void rendersVisibleList() throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", 0L);
        params.put("limit", 10);

        BoundSql sql = parse().getMappedStatement(statement("selectVisible")).getBoundSql(params);

        assertThat(sql.getSql())
            .contains("is_deleted = false")
            .contains("is_visible = true")
            .contains("ORDER BY id DESC")
            .contains("LIMIT ? OFFSET ?");
        assertThat(propertiesOf(sql)).containsExactly("limit", "offset");
    }

    private static String statement(String method) {
        return NoticeQueryMyBatisMapper.class.getName() + "." + method;
    }

    private static Map<String, Object> managementParams(String titlePattern, String contentPattern, Boolean visible) {
        Map<String, Object> params = new HashMap<>();
        params.put("titlePattern", titlePattern);
        params.put("contentPattern", contentPattern);
        params.put("visible", visible);
        params.put("offset", 20L);
        params.put("limit", 10);
        return params;
    }

    private static List<String> propertiesOf(BoundSql sql) {
        return sql.getParameterMappings().stream().map(ParameterMapping::getProperty).toList();
    }

    private static Configuration parse() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream xml = Resources.getResourceAsStream(RESOURCE)) {
            new XMLMapperBuilder(xml, configuration, RESOURCE, configuration.getSqlFragments()).parse();
        }
        return configuration;
    }
}

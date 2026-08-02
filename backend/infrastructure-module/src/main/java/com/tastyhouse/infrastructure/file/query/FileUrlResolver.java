package com.tastyhouse.infrastructure.file.query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.file.port.FileStoragePort;

/**
 * 저장 경로({@code filePath}) → 표시용 URL 변환의 단일 실행 지점(read 측 어셈블러).
 *
 * <p>과거에는 각 api 모듈 {@code FileService}의 {@code getUrlByPath}를 조회 Service마다 호출해
 * 응답을 조립했다. 그 결과 같은 변환이 60여 곳에 흩어졌고, 모듈별 {@code FileService}가 서로 다르게
 * 드리프트했다. 변환을 read 어댑터(query DAO) 안으로 들여오면 Result가 이미 URL을 담은 채 나오므로
 * api 모듈에서는 변환 호출 자체가 사라진다.
 *
 * <p>이 클래스는 {@code file} 도메인의 read model이 아니다 — 파일은 자체 조회 화면이 없고, 각 도메인
 * query DAO가 {@code uploaded_file}을 join해 흡수한다. 여기 있는 것은 그 DAO들이 공유하는 변환기
 * 하나뿐이며, 그래서 {@code FileQueryDao}가 아니라 resolver라는 이름을 쓴다.
 *
 * <p>변환 규칙 자체는 도메인 출력 포트 {@link FileStoragePort}가 소유한다(S3는 baseUrl 연결,
 * Firebase는 경로 인코딩 + {@code ?alt=media}). 스토리지 구현을 infra가 알지 않도록 포트만 주입받으며,
 * 이는 헥사고날에서 driven 어댑터가 도메인 포트를 사용하는 정상 형태다.
 *
 * <p><b>캐싱하지 않는다.</b> {@link FileStoragePort#getFileUrl(String)}은 네트워크·SDK·DB 접근이 없는
 * 순수 문자열 변환이라 행 단위로 반복 호출해도 비용이 사실상 없다. 캐싱은 값비싼 연산에 쓰는 수단이며,
 * 여기 도입하면 baseUrl 설정 변경 시 무효화 책임만 새로 생긴다.
 */
@Component
public class FileUrlResolver {

    private final FileStoragePort fileStoragePort;

    public FileUrlResolver(FileStoragePort fileStoragePort) {
        this.fileStoragePort = fileStoragePort;
    }

    /**
     * 저장 경로를 표시용 URL로 바꾼다. 경로가 없으면(파일 미첨부, left join 미스) {@code null}.
     */
    public String resolve(String filePath) {
        if (filePath == null) {
            return null;
        }
        return fileStoragePort.getFileUrl(filePath);
    }

    /**
     * 경로 맵을 URL 맵으로 한 번에 바꾼다. 목록 조회에서 파일을 별도 배치 조회한 경우에 쓰며,
     * 입력 순서를 보존한다. 경로가 {@code null}인 항목은 결과에서 제외한다.
     */
    public Map<Long, String> resolveAll(Map<Long, String> filePathById) {
        if (filePathById == null || filePathById.isEmpty()) {
            return Map.of();
        }

        Map<Long, String> urlById = new LinkedHashMap<>();
        filePathById.forEach((id, filePath) -> {
            String url = resolve(filePath);
            if (url != null) {
                urlById.put(id, url);
            }
        });
        return urlById;
    }

    /**
     * 경로 컬렉션을 URL 리스트로 바꾼다. 변환할 수 없는 항목({@code null} 경로)은 제외하므로
     * 결과 크기가 입력보다 작을 수 있다.
     */
    public List<String> resolveAll(Collection<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return List.of();
        }
        return filePaths.stream()
            .map(this::resolve)
            .filter(Objects::nonNull)
            .toList();
    }
}

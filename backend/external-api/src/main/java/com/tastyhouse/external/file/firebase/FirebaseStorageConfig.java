package com.tastyhouse.external.file.firebase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "file.provider", havingValue = "firebase")
public class FirebaseStorageConfig {

    private final FirebaseStorageProperties properties;

    public FirebaseStorageConfig(FirebaseStorageProperties properties) {
        this.properties = properties;
    }

    /**
     * 서비스 계정 키는 파일 "경로"가 아니라 configtree 로 주입된 JSON "내용"({@code firebase.service-account}
     * 프로퍼티)을 그대로 읽는다. 경로 기반 로딩({@code file:} 상대경로 + ResourceLoader)은 상대경로가 JVM 작업
     * 디렉터리 기준으로 해석되어 실행 위치(gradle -p 실행·java -jar CWD·systemd WorkingDirectory)마다 성패가
     * 갈렸다. 내용 주입 방식은 CWD 와 완전히 무관하며, Kubernetes/Docker secret 마운트 패턴과도 코드가 동일하다.
     * 시크릿 디렉터리 규약은 {@code config/application-file.yml}의 configtree import 주석을 참조한다.
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        String serviceAccountJson = properties.serviceAccountJson();
        if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
            throw new IllegalStateException(
                "Firebase 서비스 계정 키가 로드되지 않았습니다. SECRETS_DIR 환경변수가 가리키는 디렉터리"
                    + "(기본값: /etc/tastyhouse/secrets)에 firebase/service-account 파일이 있는지 확인하세요.");
        }

        try (InputStream serviceAccountStream =
                 new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .setStorageBucket(properties.storageBucket())
                    .build();

            return FirebaseApp.initializeApp(options);
        }
    }
}

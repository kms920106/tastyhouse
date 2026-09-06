package com.tastyhouse.external.firebase;

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

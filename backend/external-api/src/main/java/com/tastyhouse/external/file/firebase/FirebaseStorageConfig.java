package com.tastyhouse.external.file.firebase;

import java.io.IOException;
import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@ConditionalOnProperty(name = "file.provider", havingValue = "firebase")
public class FirebaseStorageConfig {

    private final FirebaseStorageProperties properties;
    private final ResourceLoader resourceLoader;

    public FirebaseStorageConfig(FirebaseStorageProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    /**
     * 서비스 계정 키는 {@link ResourceLoader}로 읽는다. {@code FileInputStream}은 경로를 JVM 작업 디렉터리
     * 기준으로만 해석해 실행 위치(IDE 구성·jar 실행 경로·컨테이너 WORKDIR)에 종속되고, fat jar 내부 리소스는
     * 아예 읽지 못한다. {@code classpath:}·{@code file:} 접두어를 모두 처리하는 Resource 추상화를 쓴다.
     */
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        Resource serviceAccount = resourceLoader.getResource(properties.serviceAccountPath());

        try (InputStream serviceAccountStream = serviceAccount.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .setStorageBucket(properties.storageBucket())
                    .build();

            return FirebaseApp.initializeApp(options);
        }
    }
}

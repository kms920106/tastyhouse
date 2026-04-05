package com.tastyhouse.external.file.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.tastyhouse.external.file.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(name = "file.storage.type", havingValue = "firebase")
@RequiredArgsConstructor
public class FirebaseStorageConfig {

    private final FileStorageProperties properties;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FileInputStream serviceAccount = new FileInputStream(properties.getFirebaseServiceAccountPath());

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(properties.getFirebaseStorageBucket())
                .build();

        return FirebaseApp.initializeApp(options);
    }
}

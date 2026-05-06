package memme.memoryme.global.config;

import lombok.RequiredArgsConstructor;
import memme.memoryme.upload.config.S3StorageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {
    private final S3StorageProperties properties;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(region())
                .credentialsProvider(credentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(region())
                .credentialsProvider(credentialsProvider())
                .build();
    }

    private Region region() {
        return Region.of(properties.getS3().getRegion());
    }

    private StaticCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        properties.getCredentials().getAccessKey(),
                        properties.getCredentials().getSecretKey()
                )
        );
    }
}

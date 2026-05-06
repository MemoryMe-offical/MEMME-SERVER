package memme.memoryme.upload.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "cloud.aws")
public class S3StorageProperties {
    private final S3 s3 = new S3();
    private final Credentials credentials = new Credentials();

    @Getter
    @Setter
    public static class S3 {
        private String bucket;
        private String region = "ap-northeast-2";
        private String basePrefix = "memme";
        private long readUrlExpireMinutes = 10;
    }

    @Getter
    @Setter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    public String normalizedBasePrefix() {
        if (s3.basePrefix == null || s3.basePrefix.isBlank()) {
            return "memme";
        }
        return s3.basePrefix.trim().replaceAll("^/+", "").replaceAll("/+$", "");
    }
}

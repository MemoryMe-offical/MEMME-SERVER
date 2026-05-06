package memme.memoryme.upload.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.exception.CommonErrorCode;
import memme.memoryme.global.util.jwt.CurrentUserProvider;
import memme.memoryme.upload.api.dto.FileUploadResponse;
import memme.memoryme.upload.api.dto.ImageUploadResponse;
import memme.memoryme.upload.api.dto.VideoUploadResponse;
import memme.memoryme.upload.config.S3StorageProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Primary
@RequiredArgsConstructor
public class S3UploadService implements UploadService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3StorageProperties properties;
    private final CurrentUserProvider currentUserProvider;
    private final S3ObjectUrlBuilder objectUrlBuilder;

    @Override
    public ImageUploadResponse uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.size() > 10) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }

        List<StoredS3File> storedFiles = files.stream()
                .map(file -> upload(file, "images", "image/"))
                .toList();

        return new ImageUploadResponse(
                storedFiles.stream().map(StoredS3File::url).toList(),
                storedFiles.stream().map(StoredS3File::key).toList()
        );
    }

    @Override
    public VideoUploadResponse uploadVideo(MultipartFile file) {
        StoredS3File storedFile = upload(file, "videos", "video/");
        return new VideoUploadResponse(
                storedFile.url(),
                storedFile.key(),
                null,
                null,
                storedFile.size()
        );
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        StoredS3File storedFile = upload(file, "files", null);
        return new FileUploadResponse(
                storedFile.uid(),
                storedFile.originalName(),
                storedFile.url(),
                storedFile.key(),
                storedFile.mimeType(),
                storedFile.size()
        );
    }

    @Override
    public String createReadUrl(String key) {
        validateReadableKey(key);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.getS3().getBucket())
                .key(key)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.getS3().getReadUrlExpireMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    private StoredS3File upload(MultipartFile file, String directory, String requiredContentTypePrefix) {
        validateFile(file, requiredContentTypePrefix);

        UUID userUid = currentUserProvider.getUid();
        UUID attachmentUid = UUID.randomUUID();
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extension(originalName);
        String key = buildKey(userUid, directory, attachmentUid, extension);
        String mimeType = file.getContentType() == null || file.getContentType().isBlank()
                ? "application/octet-stream"
                : file.getContentType();

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getS3().getBucket())
                    .key(key)
                    .contentType(mimeType)
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            return new StoredS3File(
                    attachmentUid,
                    originalName,
                    objectUrlBuilder.build(key),
                    key,
                    mimeType,
                    file.getSize()
            );
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateFile(MultipartFile file, String requiredContentTypePrefix) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        if (requiredContentTypePrefix == null) {
            return;
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith(requiredContentTypePrefix)) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
    }

    private String buildKey(UUID userUid, String directory, UUID attachmentUid, String extension) {
        return properties.normalizedBasePrefix()
                + "/users/" + userUid
                + "/" + directory
                + "/" + attachmentUid
                + extension;
    }

    private void validateReadableKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        UUID userUid = currentUserProvider.getUid();
        String normalizedKey = key.trim();
        String expectedPrefix = properties.normalizedBasePrefix() + "/users/" + userUid + "/";
        if (!normalizedKey.startsWith(expectedPrefix) || normalizedKey.contains("..")) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }

    private String sanitizeOriginalName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "file";
        }

        String sanitized = originalName.replaceAll("[\\r\\n\\t]", "_").trim();
        int slash = Math.max(sanitized.lastIndexOf('/'), sanitized.lastIndexOf('\\'));

        return slash >= 0 ? sanitized.substring(slash + 1) : sanitized;
    }

    private String extension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        String extension = originalName.substring(originalName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        if (!extension.matches("\\.[a-z0-9]{1,20}")) {
            return "";
        }
        return extension;
    }

    private record StoredS3File(UUID uid, String originalName, String url, String key, String mimeType, Long size) {
    }
}

package memme.memoryme.upload.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.global.exception.CommonErrorCode;
import memme.memoryme.upload.api.dto.FileUploadResponse;
import memme.memoryme.upload.api.dto.ImageUploadResponse;
import memme.memoryme.upload.api.dto.VideoUploadResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@Profile("local-upload")
@RequiredArgsConstructor
public class LocalUploadService implements UploadService {
    private static final Path UPLOAD_ROOT = Path.of("uploads");

    @Override
    public ImageUploadResponse uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.size() > 10) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        return new ImageUploadResponse(
                files.stream()
                        .map(file -> store(file, "images"))
                        .toList(),
                List.of()
        );
    }

    @Override
    public VideoUploadResponse uploadVideo(MultipartFile file) {
        StoredFile storedFile = storeFile(file, "videos");
        return new VideoUploadResponse(storedFile.url(), null, null, null, storedFile.size());
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) {
        StoredFile storedFile = storeFile(file, "files");
        return new FileUploadResponse(
                storedFile.uid(),
                storedFile.originalName(),
                storedFile.url(),
                null,
                storedFile.mimeType(),
                storedFile.size()
        );
    }

    private String store(MultipartFile file, String directory) {
        return storeFile(file, directory).url();
    }

    private StoredFile storeFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }

        try {
            UUID uid = UUID.randomUUID();
            String originalName = file.getOriginalFilename();
            String extension = extension(originalName);
            String storedName = uid + extension;
            Path targetDir = UPLOAD_ROOT.resolve(directory);
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(storedName).normalize();
            file.transferTo(target);

            return new StoredFile(
                    uid,
                    originalName,
                    "/uploads/" + directory + "/" + storedName,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String createReadUrl(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(CommonErrorCode.BAD_REQUEST);
        }
        return key;
    }

    private String extension(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            return "";
        }
        String extension = originalName.substring(originalName.lastIndexOf('.'));
        return extension.length() > 20 ? "" : extension;
    }

    private record StoredFile(UUID uid, String originalName, String url, String mimeType, Long size) {
    }
}

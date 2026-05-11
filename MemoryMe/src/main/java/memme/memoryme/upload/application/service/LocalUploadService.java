package memme.memoryme.upload.application.service;

import lombok.RequiredArgsConstructor;
import memme.memoryme.global.exception.BusinessException;
import memme.memoryme.upload.api.dto.FileUploadResponse;
import memme.memoryme.upload.api.dto.ImageUploadResponse;
import memme.memoryme.upload.api.dto.UploadObjectDto;
import memme.memoryme.upload.api.dto.UploadObjectListResponse;
import memme.memoryme.upload.api.dto.VideoUploadResponse;
import memme.memoryme.upload.exception.UploadErrorCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
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
            throw new BusinessException(UploadErrorCode.INVALID_UPLOAD_REQUEST);
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

    @Override
    public UploadObjectListResponse getUploadedImages() {
        return listUploadedObjects("images");
    }

    @Override
    public UploadObjectListResponse getUploadedVideos() {
        return listUploadedObjects("videos");
    }

    @Override
    public UploadObjectListResponse getUploadedFiles() {
        return listUploadedObjects("files");
    }

    private String store(MultipartFile file, String directory) {
        return storeFile(file, directory).url();
    }

    private StoredFile storeFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(UploadErrorCode.INVALID_UPLOAD_REQUEST);
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
            throw new BusinessException(UploadErrorCode.UPLOAD_FAILED);
        }
    }

    @Override
    public String createReadUrl(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(UploadErrorCode.INVALID_OBJECT_KEY);
        }
        return key;
    }

    @Override
    public void deleteObject(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(UploadErrorCode.INVALID_OBJECT_KEY);
        }
        try {
            Path target = UPLOAD_ROOT.resolve(key.replaceFirst("^/uploads/", "")).normalize();
            Path root = UPLOAD_ROOT.toAbsolutePath().normalize();
            Path absoluteTarget = target.toAbsolutePath().normalize();
            if (!absoluteTarget.startsWith(root)) {
                throw new BusinessException(UploadErrorCode.OBJECT_ACCESS_DENIED);
            }
            Files.deleteIfExists(absoluteTarget);
        } catch (IOException e) {
            throw new BusinessException(UploadErrorCode.OBJECT_DELETE_FAILED);
        }
    }

    private UploadObjectListResponse listUploadedObjects(String directory) {
        Path targetDir = UPLOAD_ROOT.resolve(directory).normalize();
        if (!Files.exists(targetDir)) {
            return new UploadObjectListResponse(List.of(), 0);
        }
        try (var paths = Files.list(targetDir)) {
            List<UploadObjectDto> objects = paths
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(this::lastModified).reversed())
                    .map(path -> {
                        String url = "/uploads/" + directory + "/" + path.getFileName();
                        return new UploadObjectDto(url, url, size(path), lastModified(path));
                    })
                    .toList();
            return new UploadObjectListResponse(objects, objects.size());
        } catch (IOException e) {
            throw new BusinessException(UploadErrorCode.OBJECT_LIST_FAILED);
        }
    }

    private Long size(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return null;
        }
    }

    private Instant lastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
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

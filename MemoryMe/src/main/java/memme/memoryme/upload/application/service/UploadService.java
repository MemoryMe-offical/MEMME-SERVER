package memme.memoryme.upload.application.service;

import memme.memoryme.upload.api.dto.FileUploadResponse;
import memme.memoryme.upload.api.dto.ImageUploadResponse;
import memme.memoryme.upload.api.dto.UploadObjectListResponse;
import memme.memoryme.upload.api.dto.VideoUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {
    ImageUploadResponse uploadImages(List<MultipartFile> files);
    VideoUploadResponse uploadVideo(MultipartFile file);
    FileUploadResponse uploadFile(MultipartFile file);
    UploadObjectListResponse getUploadedImages();
    UploadObjectListResponse getUploadedVideos();
    UploadObjectListResponse getUploadedFiles();
    String createReadUrl(String key);
    void deleteObject(String key);
}

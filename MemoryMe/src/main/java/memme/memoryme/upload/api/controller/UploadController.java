package memme.memoryme.upload.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import memme.memoryme.global.util.response.ResponseWrapper;
import memme.memoryme.upload.api.dto.FileUploadResponse;
import memme.memoryme.upload.api.dto.ImageUploadResponse;
import memme.memoryme.upload.api.dto.UploadObjectListResponse;
import memme.memoryme.upload.api.dto.UploadObjectUrlResponse;
import memme.memoryme.upload.api.dto.VideoUploadResponse;
import memme.memoryme.upload.application.service.UploadService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@Tag(name = "Upload API", description = "이미지·영상·파일 업로드 API")
@RestController
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;

    @Operation(summary = "이미지 업로드")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<ImageUploadResponse>> uploadImages(@RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "이미지 업로드 성공", uploadService.uploadImages(files)));
    }

    @Operation(summary = "영상 업로드")
    @PostMapping(value = "/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<VideoUploadResponse>> uploadVideo(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "영상 업로드 성공", uploadService.uploadVideo(file)));
    }

    @Operation(summary = "파일 업로드")
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseWrapper<FileUploadResponse>> uploadFile(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "파일 업로드 성공", uploadService.uploadFile(file)));
    }

    @Operation(summary = "업로드 이미지 객체 목록 조회")
    @GetMapping("/images")
    public ResponseEntity<ResponseWrapper<UploadObjectListResponse>> getUploadedImages() {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "업로드 이미지 목록 조회 성공", uploadService.getUploadedImages()));
    }

    @Operation(summary = "업로드 영상 객체 목록 조회")
    @GetMapping("/videos")
    public ResponseEntity<ResponseWrapper<UploadObjectListResponse>> getUploadedVideos() {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "업로드 영상 목록 조회 성공", uploadService.getUploadedVideos()));
    }

    @Operation(summary = "업로드 파일 객체 목록 조회")
    @GetMapping("/files")
    public ResponseEntity<ResponseWrapper<UploadObjectListResponse>> getUploadedFiles() {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "업로드 파일 목록 조회 성공", uploadService.getUploadedFiles()));
    }

    @Operation(summary = "S3 객체 접근 URL 리다이렉트")
    @GetMapping("/object")
    public ResponseEntity<Void> redirectObject(@RequestParam("key") String key) {
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, URI.create(uploadService.createReadUrl(key)).toString())
                .build();
    }

    @Operation(summary = "S3 객체 접근 URL 조회")
    @GetMapping("/object-url")
    public ResponseEntity<ResponseWrapper<UploadObjectUrlResponse>> getObjectUrl(@RequestParam("key") String key) {
        return ResponseEntity.ok(ResponseWrapper.ok(200, "업로드 객체 접근 URL 조회 성공",
                new UploadObjectUrlResponse(uploadService.createReadUrl(key))));
    }
}

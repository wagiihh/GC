package ghoneimcaptures.gc.Service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class S3Service {
    private final S3Client s3Client;
    private final ExecutorService executorService;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        this.executorService = Executors.newFixedThreadPool(10); // Thread pool for async uploads
    }

    public void uploadFile(String bucketName, String key, File file) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
    }

    /**
     * Asynchronous file upload for better performance
     */
    public CompletableFuture<Void> uploadFileAsync(String bucketName, String key, File file) {
        return CompletableFuture.runAsync(() -> {
            uploadFile(bucketName, key, file);
        }, executorService);
    }

    /**
     * Upload large files using multipart upload for better performance
     */
    public CompletableFuture<Void> uploadLargeFileAsync(String bucketName, String key, File file) {
        return CompletableFuture.runAsync(() -> {
            try {
                // For files larger than 100MB, use multipart upload
                if (file.length() > 100 * 1024 * 1024) {
                    uploadMultipartFile(bucketName, key, file);
                } else {
                    uploadFile(bucketName, key, file);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
            }
        }, executorService);
    }

    /**
     * Multipart upload for large files
     */
    private void uploadMultipartFile(String bucketName, String key, File file) {
        try {
            CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
            String uploadId = createResponse.uploadId();

            // Upload parts (simplified - in production, you'd want to handle this more robustly)
            UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(1)
                    .build();

            s3Client.uploadPart(uploadPartRequest, RequestBody.fromFile(file));

            // Complete multipart upload
            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                    .parts(CompletedPart.builder()
                            .partNumber(1)
                            .eTag(s3Client.uploadPart(uploadPartRequest, RequestBody.fromFile(file)).eTag())
                            .build())
                    .build();

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build();

            s3Client.completeMultipartUpload(completeRequest);

        } catch (Exception e) {
            throw new RuntimeException("Multipart upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Download file from S3 to local file system
     */
    public void downloadFile(String bucketName, String key, File destinationFile) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try (InputStream inputStream = s3Client.getObject(getObjectRequest);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * Get file as InputStream for streaming
     */
    public InputStream downloadFileAsStream(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(getObjectRequest);
    }

    /**
     * Generate a pre-signed URL for secure download (expires after specified duration)
     */
    public String generateDownloadUrl(String bucketName, String key, Duration expirationTime) {
        try (S3Presigner presigner = S3Presigner.create()) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expirationTime)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        }
    }

    /**
     * Generate a pre-signed URL with default 1 hour expiration
     */
    public String generateDownloadUrl(String bucketName, String key) {
        return generateDownloadUrl(bucketName, key, Duration.ofHours(1));
    }

    /**
     * Delete file from S3
     */
    public void deleteFile(String bucketName, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}

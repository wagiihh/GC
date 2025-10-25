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
        this.executorService = Executors.newFixedThreadPool(20); // Increased thread pool for better parallel processing
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
                // For files larger than 50MB, use multipart upload (lowered threshold)
                if (file.length() > 50 * 1024 * 1024) {
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
     * Optimized multipart upload for large files with proper chunking
     */
    private void uploadMultipartFile(String bucketName, String key, File file) {
        String uploadId = null;
        try {
            // Create multipart upload
            CreateMultipartUploadRequest createRequest = CreateMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            CreateMultipartUploadResponse createResponse = s3Client.createMultipartUpload(createRequest);
            uploadId = createResponse.uploadId();

            // Calculate optimal part size (minimum 5MB, maximum 5GB per part)
            long fileSize = file.length();
            long partSize = Math.max(5 * 1024 * 1024, fileSize / 10000); // At least 5MB per part
            partSize = Math.min(partSize, 5L * 1024 * 1024 * 1024); // Max 5GB per part
            
            // For files under 100MB, use single part
            if (fileSize < 100 * 1024 * 1024) {
                partSize = fileSize;
            }

            java.util.List<CompletedPart> completedParts = new java.util.ArrayList<>();
            int partNumber = 1;
            long position = 0;

            try (java.io.RandomAccessFile randomAccessFile = new java.io.RandomAccessFile(file, "r")) {
                while (position < fileSize) {
                    long currentPartSize = Math.min(partSize, fileSize - position);
                    
                    // Read file chunk
                    byte[] buffer = new byte[(int) currentPartSize];
                    randomAccessFile.seek(position);
                    randomAccessFile.readFully(buffer);

                    // Upload part
                    UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .uploadId(uploadId)
                            .partNumber(partNumber)
                            .build();

                    UploadPartResponse uploadPartResponse = s3Client.uploadPart(
                            uploadPartRequest, 
                            RequestBody.fromBytes(buffer)
                    );

                    completedParts.add(CompletedPart.builder()
                            .partNumber(partNumber)
                            .eTag(uploadPartResponse.eTag())
                            .build());

                    position += currentPartSize;
                    partNumber++;
                }
            }

            // Complete multipart upload
            CompletedMultipartUpload completedUpload = CompletedMultipartUpload.builder()
                    .parts(completedParts)
                    .build();

            CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .uploadId(uploadId)
                    .multipartUpload(completedUpload)
                    .build();

            s3Client.completeMultipartUpload(completeRequest);

        } catch (Exception e) {
            // Abort multipart upload on failure
            if (uploadId != null) {
                try {
                    AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .uploadId(uploadId)
                            .build();
                    s3Client.abortMultipartUpload(abortRequest);
                } catch (Exception abortException) {
                    System.err.println("Failed to abort multipart upload: " + abortException.getMessage());
                }
            }
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

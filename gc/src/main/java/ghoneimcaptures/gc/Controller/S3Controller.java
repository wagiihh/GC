package ghoneimcaptures.gc.Controller;

import ghoneimcaptures.gc.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/s3")
@CrossOrigin(origins = "*")
public class S3Controller {

    @Autowired
    private S3Service s3Service;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    /**
     * Upload file to S3
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Generate unique file name
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            String s3Key = folder + "/" + uniqueFileName;

            // Convert MultipartFile to File
            File tempFile = File.createTempFile("upload_", fileExtension);
            file.transferTo(tempFile);

            // Upload to S3
            s3Service.uploadFile(bucketName, s3Key, tempFile);

            // Clean up temp file
            tempFile.delete();

            response.put("status", "success");
            response.put("message", "File uploaded successfully");
            response.put("fileKey", s3Key);
            response.put("fileName", uniqueFileName);
            response.put("originalName", originalFilename);
            response.put("fileSize", String.valueOf(file.getSize()));

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("status", "error");
            response.put("message", "Failed to upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Download file from S3
     */
    @GetMapping("/download/{folder}/{fileName}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String folder,
            @PathVariable String fileName) {
        
        try {
            String s3Key = folder + "/" + fileName;
            File tempFile = File.createTempFile("download_", fileName);
            
            // Download from S3
            s3Service.downloadFile(bucketName, s3Key, tempFile);
            
            // Read file content
            byte[] fileContent = Files.readAllBytes(tempFile.toPath());
            
            // Clean up temp file
            tempFile.delete();

            // Determine content type
            String contentType = Files.probeContentType(tempFile.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(fileContent.length);

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get file as stream
     */
    @GetMapping("/stream/{folder}/{fileName}")
    public ResponseEntity<InputStream> streamFile(
            @PathVariable String folder,
            @PathVariable String fileName) {
        
        try {
            String s3Key = folder + "/" + fileName;
            InputStream inputStream = s3Service.downloadFileAsStream(bucketName, s3Key);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("inline", fileName);

            return new ResponseEntity<>(inputStream, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Generate download URL
     */
    @GetMapping("/download-url/{folder}/{fileName}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @PathVariable String folder,
            @PathVariable String fileName,
            @RequestParam(value = "expiration", required = false, defaultValue = "3600") long expirationSeconds) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String s3Key = folder + "/" + fileName;
            Duration expiration = Duration.ofSeconds(expirationSeconds);
            String downloadUrl = s3Service.generateDownloadUrl(bucketName, s3Key, expiration);

            response.put("status", "success");
            response.put("downloadUrl", downloadUrl);
            response.put("expirationSeconds", String.valueOf(expirationSeconds));
            response.put("fileName", fileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to generate download URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete file from S3
     */
    @DeleteMapping("/delete/{folder}/{fileName}")
    public ResponseEntity<Map<String, String>> deleteFile(
            @PathVariable String folder,
            @PathVariable String fileName) {
        
        Map<String, String> response = new HashMap<>();
        
        try {
            String s3Key = folder + "/" + fileName;
            s3Service.deleteFile(bucketName, s3Key);

            response.put("status", "success");
            response.put("message", "File deleted successfully");
            response.put("fileKey", s3Key);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "S3 Controller");
        response.put("bucket", bucketName);
        return ResponseEntity.ok(response);
    }
}

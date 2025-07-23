package exercise.Exercise.services;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  private final S3Presigner presigner;

  @Value("${aws.s3.bucket}")
  private String bucket;

  public String generatePresignedUploadUrl(Long exerciseId, String fileName, String folder, Duration ttl) {
    String key = folder + "/" + exerciseId + "_" + fileName;

    PutObjectRequest objectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(guessContentType(fileName))
        // .acl(ObjectCannedACL.PUBLIC_READ)
        .build();

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(ttl)
        .putObjectRequest(objectRequest)
        .build();

    PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
    return presigned.url().toString();
  }

  private String guessContentType(String fileName) {
    return switch (fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()) {
      case "mp4", "m4v" -> "video/mp4";
      case "mov" -> "video/quicktime";
      case "webm" -> "video/webm";
      case "jpg", "jpeg" -> "image/jpeg";
      case "png" -> "image/png";
      default -> "application/octet-stream";
    };
  }

  public boolean objectExists(String objectUrl) {
    URI uri = URI.create(objectUrl);
    String bucket = uri.getHost().split("\\.")[0];
    String key = uri.getPath().substring(1);
    try (S3Client s3 = S3Client.builder().build()) {
      s3.headObject(HeadObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    } catch (S3Exception e) {
      throw e;
    }
  }

  public String uploadObject(Long exerciseId, MultipartFile file, String folder) throws IOException {
    String key = folder + "/" + exerciseId + "_" + file.getOriginalFilename();

    s3Client.putObject(
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(file.getContentType())
            .build(),
        RequestBody.fromBytes(file.getBytes()));

    return "https://" + bucket + ".s3.eu-central-1.amazonaws.com/" + key;
  }

  public void deleteObject(String objectUrl) throws IOException {
    String bucketUrl = "https://" + bucket + ".s3.eu-central-1.amazonaws.com/";
    String key = objectUrl.replace(bucketUrl, "");

    s3Client.deleteObject(
        DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build());
  }
}

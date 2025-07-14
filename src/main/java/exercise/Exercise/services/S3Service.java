package exercise.Exercise.services;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import exercise.Exercise.dtos.CreateExerciseDTO;
import exercise.Exercise.entities.Exercise;
import exercise.Exercise.repositories.ExerciseRepository;
import exercise.User.dtos.UserDTO;
import exercise.User.entities.User;
import exercise.User.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucket;

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

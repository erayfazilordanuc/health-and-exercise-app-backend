package exercise.Symptoms.dtos;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import exercise.Common.entities.TimeStamps;
import exercise.User.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SymptomsDTO {

    private Long id;
    private Integer pulse; // bpm
    private Integer steps; // steps count
    private Integer sleep; // sleep duration in minutes

    private String sleepSession; // JSON string olabilir (örneğin REM/DEEP uyku vs.)

    private Long ownerId;

    private Timestamp createdAt;
    private Timestamp updatedAt;
}

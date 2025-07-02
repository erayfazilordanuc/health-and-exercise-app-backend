package exercise.Message.dtos;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

  private Long id;

  @NotNull
  private String message;

  @NotNull
  private String sender;

  @NotNull
  private String receiver;

  @NotNull
  private Long roomId;

  private Timestamp createdAt;
}
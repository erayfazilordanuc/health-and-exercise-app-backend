package exercise.Common.email.entities;

import com.google.firebase.database.annotations.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {

  private String recipient;
  private String msgBody;
  private String subject;
  @Nullable
  private String attachment;
}

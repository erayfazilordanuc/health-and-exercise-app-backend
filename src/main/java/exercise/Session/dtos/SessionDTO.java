package exercise.Session.dtos;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDTO {
        private Long id;
        private Long userId;

        private String locale;
        private String source;

        private Timestamp startedAt;
        private Timestamp endedAt;
        private Long activeMs;
        private Integer heartbeatCount;
}
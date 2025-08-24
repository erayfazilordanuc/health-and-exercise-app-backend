package exercise.Session.dtos;

import java.sql.Timestamp;
import java.util.UUID;

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

        private UUID sessionId;

        private Timestamp startedAt;

        private Timestamp endedAt;

        private Long activeMs;

        private Timestamp lastHeartbeatAt;

        private Integer heartbeatCount;

        private String reason; // "logout" | "close"

        private String source; // "MOBILE" vs.

        private Timestamp createdAt;

        private Timestamp updatedAt;
}
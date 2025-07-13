package exercise.Notification.entities;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import exercise.User.entities.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "fcm_tokens")
public class FCMToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(length = 50)
    private String platform; // örn: "android" ya da "ios"

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public FCMToken(FCMTokenDTO tokenDTO) {
        this.id = Objects.isNull(tokenDTO.getId()) ? tokenDTO.getId() : null;
        this.userId = tokenDTO.getUserId(); // Instead of userId, user can be a column
        this.token = tokenDTO.getToken();
        this.platform = tokenDTO.getPlatform();
        this.createdAt = Objects.isNull(tokenDTO.getCreatedAt()) ? tokenDTO.getCreatedAt() : null;
    }
}

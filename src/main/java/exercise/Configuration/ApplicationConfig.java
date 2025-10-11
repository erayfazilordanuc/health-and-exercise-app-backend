package exercise.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder, en yaygın ve güvenli seçeneklerden biridir.
        return new BCryptPasswordEncoder();
    }

    // Buraya uygulamanın geneli için gerekli başka bean'ler de ekleyebilirsiniz.
}
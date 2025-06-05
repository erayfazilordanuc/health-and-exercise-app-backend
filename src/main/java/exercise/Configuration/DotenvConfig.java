package exercise.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
  @Bean
  public Dotenv dotenv() {
    // Eğer prod-da başka bir env dosyası demek istersen filename() parametresini
    // değiştir
    return Dotenv.configure()
        .filename("stage.env")
        .ignoreIfMissing() // Bulamazsa yine de çalışsın
        .load();
  }
}

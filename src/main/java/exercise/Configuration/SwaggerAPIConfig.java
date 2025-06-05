package exercise.Configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerAPIConfig {

        String schemeName = "bearerAuth";
        String bearerFormat = "JWT";
        String scheme = "bearer";

        @Bean
        public OpenAPI caseOpenAPI() {
                return new OpenAPI()
                                .servers(List.of(new Server().url("http://localhost:8080"),
                                                new Server().url("https://eray.ordanuc.com")))
                                .addSecurityItem(new SecurityRequirement()
                                                .addList(schemeName))
                                .components(new Components()
                                                .addSecuritySchemes(
                                                                schemeName, new SecurityScheme()
                                                                                .name(schemeName)
                                                                                .type(SecurityScheme.Type.HTTP)
                                                                                .bearerFormat(bearerFormat)
                                                                                .in(SecurityScheme.In.HEADER)
                                                                                .scheme(scheme)))
                                .info(new Info()
                                                .title("Exercise Service")
                                                .description("Claim Event Information")
                                                .version("1.0"));
        }
}

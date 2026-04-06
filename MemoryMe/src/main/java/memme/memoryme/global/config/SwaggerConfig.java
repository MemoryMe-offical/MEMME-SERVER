package memme.memoryme.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${server.port}")
    private String port;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .info(apiInfo())
                .addServersItem(new Server()
                        .url("https://memme.o-r.kr")
                        .description("Production server"))
                .addServersItem(new Server()
                        .url("http://localhost:" + port)
                        .description("local server"));
    }

    private Info apiInfo() {
        return new Info()
                .title("멤메")
                .description("멤메 API Docs")
                .version("1.0.0");
    }
}
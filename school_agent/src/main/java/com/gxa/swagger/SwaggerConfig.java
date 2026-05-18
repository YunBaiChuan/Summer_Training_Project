package com.gxa.swagger;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /*
    配置swagger基本信息
     */
    @Bean
    public OpenAPI swaggerOpenApi() {
        return new OpenAPI()
                .info(new Info().title("校园二手交易智能平台")
                        .description("校园二手交易智能平台")
                        .version("v1.0"))
                .externalDocs(new ExternalDocumentation()
                        .description("")
                        .url(""));
    }

}

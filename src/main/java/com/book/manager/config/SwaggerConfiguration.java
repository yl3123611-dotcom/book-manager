package com.book.manager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 配置 - Spring Boot 3 推荐 springdoc-openapi
 */
@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("【图书管理后台 Swagger UI】")
                        .description("图书管理后台接口")
                        .version("1.0")
                        .contact(new Contact()
                                .name("尘心(Jason)")
                                .url("http://www.diqiyuzhou.tk")
                                .email("amazingjava@163.com")));
    }
}

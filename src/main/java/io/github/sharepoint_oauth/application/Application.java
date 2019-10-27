package io.github.sharepoint_oauth.application;

import io.github.sharepoint_oauth.jersey.RestConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    @Bean
    public RestConfig restConfig() {
        return new RestConfig();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}


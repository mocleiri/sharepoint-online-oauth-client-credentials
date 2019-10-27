package io.github.sharepoint_oauth.application;

import io.github.sharepoint_oauth.jersey.RestConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

@SpringBootApplication()
@ComponentScan("io.github.sharepoint_oauth")
public class Application {

//    @Bean
//    public RestConfig restConfig() {
//        return new RestConfig();
//    }

    public static void main(String[] args) {
        SpringApplication.run(new Class[] {Application.class, RestConfig.class}, args);
    }

}


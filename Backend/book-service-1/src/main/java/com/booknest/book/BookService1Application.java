package com.booknest.book;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Path;
import java.nio.file.Paths;

// Main entry point for the Book Microservice
@SpringBootApplication
public class BookService1Application {

    @Value("${app.upload.dir:uploads/books/}")
    private String uploadDir;

    public static void main(String[] args) {
        SpringApplication.run(BookService1Application.class, args);
    }

    // Configure static resource handling for book cover images
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                Path absoluteUploadPath = Paths.get(uploadDir).toAbsolutePath();
                registry.addResourceHandler("/uploads/books/**")
                        .addResourceLocations("file:" + absoluteUploadPath.toString().replace("\\", "/") + "/");
            }
        };
    }
}
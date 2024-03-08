package com.mawen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2023/9/18
 */
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.mawen.repository")
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}

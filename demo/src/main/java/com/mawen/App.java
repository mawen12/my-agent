package com.mawen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2023/9/18
 */
@SpringBootApplication
@EnableElasticsearchRepositories(basePackages = "com.mawen.repository")
public class App {

    // -javaagent:/opt/agent/agent-dep.jar
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    // -XX:StartFlightRecording:filename=recording.jfr,duration=10s
//    public static void main(String[] args) {
//        SpringApplication application = new SpringApplication(App.class);
//        application.setApplicationStartup(new BufferingApplicationStartup(2048));
//        application.run(args);
//    }
}

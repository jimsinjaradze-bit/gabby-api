package io.cutehat.gabby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GabbyApplication {

    public static void main(String[] args) {
        SpringApplication.run(GabbyApplication.class, args);
    }

}

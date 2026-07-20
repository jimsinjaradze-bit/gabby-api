package io.cutehat.gabby;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GabbyApplication {

    public static void main(String[] args) {
        // Tomcat negotiates permessage-deflate itself at the JSR-356 container level,
        // bypassing Spring's HandshakeHandler.filterRequestedExtensions entirely. This
        // is the only property that actually removes it from the server's installed
        // extensions, which is what causes WebKit (all iOS browsers) to close with 1002.
        System.setProperty("org.apache.tomcat.websocket.DISABLE_BUILTIN_EXTENSIONS", "true");
        SpringApplication.run(GabbyApplication.class, args);
    }

}

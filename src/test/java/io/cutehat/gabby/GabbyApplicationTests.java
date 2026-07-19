package io.cutehat.gabby;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Real server needed: the websocket container bean requires a ServerContainer, absent in the mock environment
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GabbyApplicationTests {

    @Test
    void contextLoads() {
    }

}

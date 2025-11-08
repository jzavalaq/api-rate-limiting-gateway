package com.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic scaffold test to verify the application loads without errors.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class ScaffoldTest {

    @Test
    void contextLoads() {
        // If this test passes, the Spring context loads successfully
    }
}

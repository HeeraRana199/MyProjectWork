package com.cts;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Default Spring Boot context-load test. Disabled because it requires a real
 * MySQL connection (the production datasource). The pure-unit tests under
 * com.cts.service / com.cts.util cover the business logic without needing
 * a running database.
 */
@Disabled("Requires live MySQL; skipped for unit-test runs.")
@SpringBootTest
class Demo1ApplicationTests {

    @Test
    void contextLoads() {
    }

}

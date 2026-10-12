package com.movietracker.watchlog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WatchlogServiceApplication Context Loads Test")
class WatchlogServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}

package com.indhive;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = IndhiveApplication.class)
@ActiveProfiles("test")
public class IndhiveApplicationTests {

    @Test
    void contextLoads() {
        // Este test simplemente verifica que el contexto arranca correctamente.
    }
}

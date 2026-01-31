package nttdata.personal.julius.api;

import nttdata.personal.julius.api.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class PersonalJuliusApiTransactionApplicationTests {

    @Test
    void contextLoads() {
    }
}

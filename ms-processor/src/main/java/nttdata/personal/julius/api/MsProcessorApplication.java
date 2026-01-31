package nttdata.personal.julius.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableFeignClients
@EnableCaching
public class MsProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsProcessorApplication.class, args);
    }
}

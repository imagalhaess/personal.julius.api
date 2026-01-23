package nttdata.personal.julius.api;

import nttdata.personal.julius.api.infrastructure.messaging.kafka.events.TransactionCreatedEvent;
import nttdata.personal.julius.api.infrastructure.messaging.kafka.producer.TransactionEventProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Personal Julius API - Aplicação de Gerenciamento de Finanças Pessoais
 * 
 * Desenvolvido como projeto de validação de conhecimentos adquiridos
 * no programa Java NTTData BECA.
 * 
 * @author Isabela M
 * @version 0.0.1-SNAPSHOT
 */
@SpringBootApplication
public class PersonalJuliusApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PersonalJuliusApiApplication.class, args);
    }

    @Bean
    CommandLineRunner testKafka(TransactionEventProducer producer) {
        return args -> {
            var testEvent = new TransactionCreatedEvent(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new BigDecimal("100.00"),
                    "BRL",
                    "EXPENSE",
                    "FOOD"
            );
            producer.send(testEvent);
            System.out.println("MENSAGEM DE TESTE ENVIADA AO KAFKA!");
        };
    }
}

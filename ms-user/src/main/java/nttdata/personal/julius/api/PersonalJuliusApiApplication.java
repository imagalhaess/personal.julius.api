package nttdata.personal.julius.api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

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
@EnableKafka
public class PersonalJuliusApiApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(),
                                                             entry.getValue()));

        SpringApplication.run(PersonalJuliusApiApplication.class, args);
    }

}
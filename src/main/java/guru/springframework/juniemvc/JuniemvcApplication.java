package guru.springframework.juniemvc;

import guru.springframework.juniemvc.config.ShipmentApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ShipmentApiProperties.class)
public class JuniemvcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JuniemvcApplication.class, args);
    }

}

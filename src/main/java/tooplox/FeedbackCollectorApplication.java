package tooplox;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
class DemoApplication {

    static void main(String[] args) {
        run(DemoApplication.class, args);
    }
}

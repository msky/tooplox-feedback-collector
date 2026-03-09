package tooplox.shared.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class ClockProvider {

    @Bean
    public Clock getClock() {
        return Clock.systemUTC();
    }
}

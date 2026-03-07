package tooplox.feedbackcollector.utils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

    public static LocalDateTime randomFutureDate(Clock clock) {
        Random randomDays = ThreadLocalRandom.current();
        return LocalDateTime.now(clock).plusDays(randomDays.nextInt(365) + 1);
    }
}

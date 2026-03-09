package tooplox.feedbackcollector.utils;

import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;

import java.time.Clock;
import java.time.LocalDateTime;

import static tooplox.feedbackcollector.utils.TestUtils.randomFutureDate;

public class CreateInboxCommandBuilder {
    Clock clock = Clock.systemUTC();
    LocalDateTime expiringOn = randomFutureDate(clock);
    boolean allowsAnonymousFeedback = true;
    String topic = "sample topic";

    public CreateInboxCommand build() {
        return new CreateInboxCommand(expiringOn, allowsAnonymousFeedback, topic);
    }

    public static CreateInboxCommandBuilder sampleCreateInboxCommand(Clock clock) {
        return new CreateInboxCommandBuilder().withClock(clock);
    }

    public CreateInboxCommandBuilder expiringOn(LocalDateTime expiringOn) {
        this.expiringOn = expiringOn;
        return this;
    }

    public CreateInboxCommandBuilder allowingAnonymousFeedback(boolean allowsAnonymousFeedback) {
        this.allowsAnonymousFeedback = allowsAnonymousFeedback;
        return this;
    }

    private CreateInboxCommandBuilder withClock(Clock clock) {
        this.clock = clock;
        return this;
    }


    public CreateInboxCommandBuilder withoutExpirationDate() {
        this.expiringOn = null;
        return this;
    }

    public CreateInboxCommandBuilder withTopic(String topic) {
        this.topic = topic;
        return this;
    }
}

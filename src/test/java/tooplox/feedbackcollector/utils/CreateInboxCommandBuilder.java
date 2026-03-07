package tooplox.feedbackcollector.utils;

import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;

import java.time.Clock;
import java.time.LocalDateTime;

import static tooplox.feedbackcollector.utils.TestUtils.randomFutureDate;

public class CreateInboxCommandBuilder {
    Clock clock = Clock.systemUTC();
    String ownerUserName = "testUserName";
    LocalDateTime expiringOn = randomFutureDate(clock);
    boolean allowsAnonymousFeedback = true;

    public CreateInboxCommand build() {
        return new CreateInboxCommand(ownerUserName, expiringOn, allowsAnonymousFeedback);
    }

    public static CreateInboxCommandBuilder sampleCreateInboxCommand(Clock clock) {
        return new CreateInboxCommandBuilder().withClock(clock);
    }

    public CreateInboxCommandBuilder ownedBy(String ownerUserName) {
        this.ownerUserName = ownerUserName;
        return this;
    }

    public CreateInboxCommandBuilder expiringOn(LocalDateTime expiringOn) {
        this.expiringOn = expiringOn;
        return this;
    }

    public CreateInboxCommandBuilder allowingAnonymousFeedback(boolean allowsAnonymousFeedback) {
        this.allowsAnonymousFeedback = allowsAnonymousFeedback;
        return this;
    }

    public CreateInboxCommandBuilder withoutOwner() {
        this.ownerUserName = null;
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
}

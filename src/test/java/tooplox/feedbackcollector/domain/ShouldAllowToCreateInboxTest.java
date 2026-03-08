package tooplox.feedbackcollector.domain;

import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.IncorrectExpirationDate;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.MissingOwner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ShouldAllowToCreateInboxTest extends BaseFeedbackCollectorTest {
    @Test
    void shouldAllowToCreateInbox() {
        // given
        userIsAuthenticated("Bob");
        val createInboxCommand = sampleCreateInboxCommand()
                .expiringOn(randomFutureDate())
                .allowingAnonymousFeedback(true)
                .build();

        // when
        val result = createInbox(createInboxCommand);


        // then
        inboxIdWasGenerated(result);
    }

    @Test
    void shouldAllowToCreateMultipleInboxes() {
        // given
        userIsAuthenticated("Bob");

        val commands = multipleCreateInboxCommandFromSameOwner();

        // when
        val results = commands.stream().map(this::createInbox).toList();

        // then
        results.forEach(this::inboxIdWasGenerated);
    }

    @Test
    void shouldFailWhenCreatingInboxWithExpirationDateInThePast() {
        // given
        userIsAuthenticated("Bob");

        val command = sampleCreateInboxCommand()
                .expiringOn(someDateInPast())
                .build();

        // when
        val result = createInbox(command);

        // then
        failedBecauseOf(result, IncorrectExpirationDate.class);
    }

    @Test
    void shouldFailWhenCreatingInboxWithoutExpirationDate() {
        // given
        userIsAuthenticated("Bob");
        val command = sampleCreateInboxCommand()
                .withoutExpirationDate()
                .build();

        // when
        val result = createInbox(command);

        // then
        failedBecauseOf(result, IncorrectExpirationDate.class);
    }

    @Test
    void shouldFailWhenCreatingInboxWithoutOwner() {
        // when
        val resultWithoutOwner = createInbox(sampleCreateInboxCommand()
                .build());

        // then
        failedBecauseOf(resultWithoutOwner, MissingOwner.class);
    }

    private List<CreateInboxCommand> multipleCreateInboxCommandFromSameOwner() {
        return IntStream.range(0, 5).mapToObj(i -> sampleCreateInboxCommand()
                .build()).toList();
    }

    private void inboxIdWasGenerated(Either<CreateInboxFailure, CreateInboxResultDto> result) {
        assertThat(result)
                .hasRightValueSatisfying(value -> assertNotNull(value.inboxId()));
    }

    private LocalDateTime someDateInPast() {
        return LocalDateTime.now(clock).minusDays(1);
    }
}

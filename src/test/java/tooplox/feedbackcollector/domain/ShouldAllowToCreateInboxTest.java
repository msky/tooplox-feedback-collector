package tooplox.feedbackcollector.domain;

import io.vavr.control.Either;
import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.IncorrectExpirationDate;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.MissingOwner;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.TooLongOwnerUserName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ShouldAllowToCreateInboxTest extends BaseFeedbackCollectorTest {
    @Test
    void shouldAllowToCreateInbox() {
        // given
        val createInboxCommand = sampleCreateInboxCommand()
                .ownedBy("Bob")
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
        val commands = multipleCreateInboxCommandFromSameOwner();

        // when
        val results = commands.stream().map(this::createInbox).toList();

        // then
        results.forEach(this::inboxIdWasGenerated);
    }

    @Test
    void shouldFailWhenCreatingInboxWithExpirationDateInThePast() {
        // given
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
                .withoutOwner()
                .build());

        // then
        failedBecauseOf(resultWithoutOwner, MissingOwner.class);

        // when
        val resultWithEmptyOwner = createInbox(sampleCreateInboxCommand()
                .ownedBy("")
                .build());

        // then
        failedBecauseOf(resultWithEmptyOwner, MissingOwner.class);
    }

    @Test
    void shouldFailWhenCreatingInboxWithTooLongOwnerName() {
        // given
        val tooLongOwnerName = "a".repeat(MAX_OWNER_USER_NAME_LENGTH + 1);

        // when
        val result = createInbox(sampleCreateInboxCommand()
                .ownedBy(tooLongOwnerName)
                .build());

        // then
        failedBecauseOf(result, TooLongOwnerUserName.class);

    }

    private List<CreateInboxCommand> multipleCreateInboxCommandFromSameOwner() {
        return IntStream.range(0, 5).mapToObj(i -> sampleCreateInboxCommand()
                .ownedBy("Bob").build()).toList();
    }

    private void inboxIdWasGenerated(Either<CreateInboxFailure, CreateInboxResultDto> result) {
        assertThat(result)
                .hasRightValueSatisfying(value -> assertNotNull(value.inboxId()));
    }

    private LocalDateTime someDateInPast() {
        return LocalDateTime.now(clock).minusDays(1);
    }
}

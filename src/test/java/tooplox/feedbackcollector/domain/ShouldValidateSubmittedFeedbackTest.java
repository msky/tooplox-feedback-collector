package tooplox.feedbackcollector.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.*;
import tooplox.shared.domain.InboxId;

import static tooplox.feedbackcollector.utils.SubmitFeedbackCommandBuilder.sampleSubmitFeedbackCommand;
import static tooplox.feedbackcollector.utils.TestUtils.someRandomDateTime;

public class ShouldValidateSubmittedFeedbackTest extends BaseFeedbackCollectorTest {

    @Test
    void shouldAllowToSubmitFeedback() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val submitFeedbackResult = submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        // then
        succeeded(submitFeedbackResult);
    }

    @Test
    void shouldFailWhenSubmittingFeedbackToExpiredInbox() {
        // given
        userIsAuthenticated("Bob");
        val now = someRandomDateTime();
        val inboxExpirationTime = now.plusDays(5);
        timeIs(now);
        val inboxId = createInbox(sampleCreateInboxCommand().expiringOn(inboxExpirationTime).build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        timeIs(inboxExpirationTime.plusSeconds(1));
        val result = submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        // then
        failedBecauseOf(result, InboxExpired.class);
    }

    @Test
    void shouldFailWhenSubmittingFeedbackForNonExistingInbox() {
        // given
        userIsAuthenticated("Bob");
        InboxId nonExistingInbox = InboxId.generate();

        userIsAuthenticated("Alice");

        // when
        val result = submitFeedback(sampleSubmitFeedbackCommand().toInbox(nonExistingInbox).build());

        // then
        failedBecauseOf(result, InboxNotFound.class);
    }

    @Test
    void shouldFailWhenSubmittingFeedbackWithoutInbox() {
        // when
        userIsAuthenticated("Alice");
        val result = submitFeedback(sampleSubmitFeedbackCommand().withoutInbox().build());

        // then
        failedBecauseOf(result, InboxNotFound.class);
    }

    @Test
    void shouldFailWhenSubmittingFeedbackWithoutBody() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val submitWithoutContentResult = submitFeedback(sampleSubmitFeedbackCommand()
                .toInbox(inboxId)
                .withoutContent()
                .build());

        // then
        failedBecauseOf(submitWithoutContentResult, NoContent.class);

        // when
        val submitWithWhitespaceContentResult = submitFeedback(sampleSubmitFeedbackCommand()
                .toInbox(inboxId)
                .withContent("   ")
                .build());

        // then
        failedBecauseOf(submitWithWhitespaceContentResult, NoContent.class);

        // when
        val submitWithNewLineContentResult = submitFeedback(sampleSubmitFeedbackCommand()
                .toInbox(inboxId)
                .withContent("\n")
                .build());

        // then
        failedBecauseOf(submitWithNewLineContentResult, NoContent.class);
    }

    @Test
    void shouldFailWhenSubmittingAnonymousFeedbackToInboxThatDoesntAllowIt() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand()
                .allowingAnonymousFeedback(false)
                .build()).get().inboxId();

        thereIsNoAuthenticatedUser();

        // when
        val result = submitFeedback(sampleSubmitFeedbackCommand()
                .toInbox(inboxId)
                .build());

        // then
        failedBecauseOf(result, AnonymousFeedbackNotAllowed.class);
    }

    @Test
    void shouldFailWhenSubmittingFeedbackWithTooLongBody() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val result = submitFeedback(sampleSubmitFeedbackCommand()
                .toInbox(inboxId)
                .withContent("a".repeat(MAX_FEEDBACK_CONTENT_LENGTH + 1))
                .build());

        // then
        failedBecauseOf(result, ContentTooLarge.class);
    }

    @Test
    void shouldFailWhenSubmittingFeedbackToOwnInbox() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        // when
        val result = submitFeedback(sampleSubmitFeedbackCommand().
                toInbox(inboxId)
                .build());

        // then
        failedBecauseOf(result, SubmittingFeedbackToYourself.class);
    }
}

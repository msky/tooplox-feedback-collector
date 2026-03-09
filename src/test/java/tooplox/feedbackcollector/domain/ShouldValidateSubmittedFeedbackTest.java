package tooplox.feedbackcollector.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure.*;
import tooplox.shared.domain.InboxId;

import static tooplox.feedbackcollector.utils.SendMessageCommandBuilder.sampleSendMessageCommand;
import static tooplox.feedbackcollector.utils.TestUtils.someRandomDateTime;

public class ShouldValidateSubmittedFeedbackTest extends BaseFeedbackCollectorTest {

    @Test
    void shouldAllowToSendMessage() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val sendMessageResult = sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        // then
        succeeded(sendMessageResult);
    }

    @Test
    void shouldAllowToSubmitAnonymousMessage() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand()
                .allowingAnonymousMessages(true)
                .build()).get().inboxId();

        thereIsNoAuthenticatedUser();
        // when
        val sendMessageResult = sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        // then
        succeeded(sendMessageResult);
    }

    @Test
    void shouldFailWhenSubmittingMessageToExpiredInbox() {
        // given
        userIsAuthenticated("Bob");
        val now = someRandomDateTime();
        val inboxExpirationTime = now.plusDays(5);
        timeIs(now);
        val inboxId = createInbox(sampleCreateInboxCommand().expiringOn(inboxExpirationTime).build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        timeIs(inboxExpirationTime.plusSeconds(1));
        val result = sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        // then
        failedBecauseOf(result, InboxExpired.class);
    }

    @Test
    void shouldFailWhenSubmittingMessageForNonExistingInbox() {
        // given
        userIsAuthenticated("Bob");
        InboxId nonExistingInbox = InboxId.generate();

        userIsAuthenticated("Alice");

        // when
        val result = sendMessage(sampleSendMessageCommand().toInbox(nonExistingInbox).build());

        // then
        failedBecauseOf(result, InboxNotFound.class);
    }

    @Test
    void shouldFailWhenSubmittingMessageWithoutInbox() {
        // when
        userIsAuthenticated("Alice");
        val result = sendMessage(sampleSendMessageCommand().withoutInbox().build());

        // then
        failedBecauseOf(result, InboxNotFound.class);
    }

    @Test
    void shouldFailWhenSubmittingMessageWithoutBody() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val submitWithoutContentResult = sendMessage(sampleSendMessageCommand()
                .toInbox(inboxId)
                .withoutContent()
                .build());

        // then
        failedBecauseOf(submitWithoutContentResult, NoContent.class);

        // when
        val submitWithWhitespaceContentResult = sendMessage(sampleSendMessageCommand()
                .toInbox(inboxId)
                .withContent("   ")
                .build());

        // then
        failedBecauseOf(submitWithWhitespaceContentResult, NoContent.class);

        // when
        val submitWithNewLineContentResult = sendMessage(sampleSendMessageCommand()
                .toInbox(inboxId)
                .withContent("\n")
                .build());

        // then
        failedBecauseOf(submitWithNewLineContentResult, NoContent.class);
    }

    @Test
    void shouldFailWhenSubmittingAnonymousMessageToInboxThatDoesntAllowIt() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand()
                .allowingAnonymousMessages(false)
                .build()).get().inboxId();

        thereIsNoAuthenticatedUser();

        // when
        val result = sendMessage(sampleSendMessageCommand()
                .toInbox(inboxId)
                .build());

        // then
        failedBecauseOf(result, AnonymousMessageNotAllowed.class);
    }

    @Test
    void shouldFailWhenSubmittingMessageWithTooLongBody() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val result = sendMessage(sampleSendMessageCommand()
                .toInbox(inboxId)
                .withContent("a".repeat(MAX_FEEDBACK_CONTENT_LENGTH + 1))
                .build());

        // then
        failedBecauseOf(result, ContentTooLarge.class);
    }

    @Test
    void shouldFailWhenSubmittingMessageToOwnInbox() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        // when
        val result = sendMessage(sampleSendMessageCommand().
                toInbox(inboxId)
                .build());

        // then
        failedBecauseOf(result, SubmittingMessageToYourself.class);
    }
}

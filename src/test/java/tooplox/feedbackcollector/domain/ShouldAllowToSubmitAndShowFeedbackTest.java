package tooplox.feedbackcollector.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.dto.ReadInboxResultDto;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure.InboxNotFound;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure.NotAuthorizedToReadFromInbox;
import tooplox.shared.domain.InboxId;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static tooplox.feedbackcollector.utils.AuthenticatedUserBuilder.authenticatedUser;
import static tooplox.feedbackcollector.utils.SendMessageCommandBuilder.sampleSendMessageCommand;
import static tooplox.feedbackcollector.utils.TestUtils.someRandomDateTime;

public class ShouldAllowToSubmitAndShowFeedbackTest extends BaseFeedbackCollectorTest {

    @Test
    void shouldShowSubmittedMessage() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated(authenticatedUser().withSignature("Alice#hash").build());

        // when
        val messageSubmissionDate = someRandomDateTime();
        timeIs(messageSubmissionDate);

        val SendMessageCommand = sampleSendMessageCommand().toInbox(inboxId).build();
        sendMessage(SendMessageCommand);

        userIsAuthenticated("Bob");

        // then
        val feedback = readInbox(inboxId).get();
        assertThat(feedback.messages()).hasSize(1);

        val message = feedback.messages().getFirst();
        assertThat(message.id()).isNotNull();
        assertThat(message.content()).isEqualTo(SendMessageCommand.content());
        assertThat(message.authorSignature()).isEqualTo("Alice#hash");
        assertThat(message.submittedAt()).isEqualTo(messageSubmissionDate);
    }

    @Test
    void shouldNotShowTheAuthorOfAnonymousMessage() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand()
                .allowingAnonymousMessages(true)
                .build()).get().inboxId();


        // when
        userIsAuthenticated("Alice");
        sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        thereIsNoAuthenticatedUser();
        sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        userIsAuthenticated("Bob");

        // then
        val feedback = readInbox(inboxId).get();
        assertThat(feedback.messages()).hasSize(2);

        thereIsAnonymousMessageIn(feedback.messages());
        thereIsMessageFrom("Alice", feedback.messages());
    }

    @Test
    void shouldOnlyShowMessageForTheGivenInbox() {
        // given
        userIsAuthenticated("Bob");
        val firstInboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();
        val secondInboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");
        sendMessage(sampleSendMessageCommand().toInbox(firstInboxId).build());

        userIsAuthenticated("Mickey Mouse");
        sendMessage(sampleSendMessageCommand().toInbox(secondInboxId).build());

        userIsAuthenticated("Bob");

        // when
        val firstInboxMessage = readInbox(firstInboxId).get();
        val secondInboxMessage = readInbox(secondInboxId).get();

        // then
        assertThat(firstInboxMessage.messages()).hasSize(1);
        assertThat(secondInboxMessage.messages()).hasSize(1);

        thereIsMessageFrom("Alice", firstInboxMessage.messages());
        thereIsMessageFrom("Mickey Mouse", secondInboxMessage.messages());
    }

    @Test
    void shouldOnlyShowMessageToTheOwnerOfTheInbox() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");
        sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        // when
        val result = readInbox(inboxId);

        // then
        failedBecauseOf(result, NotAuthorizedToReadFromInbox.class);
    }

    @Test
    void shouldFailWhenShowingMessageForNonExistingOrMissingInbox() {
        // given
        userIsAuthenticated("Bob");

        // when
        val resultForNonExistingInbox = readInbox(InboxId.generate());
        val resultForMissingInbox = readInbox(null);

        // then
        failedBecauseOf(resultForNonExistingInbox, InboxNotFound.class);
        failedBecauseOf(resultForMissingInbox, InboxNotFound.class);
    }

    // TODO
//    @Test
//    void shouldShowRequestedPageOfMessageMessages() {
//    }


    private void thereIsAnonymousMessageIn(List<ReadInboxResultDto.MessageDto> messages) {
        assertThat(messages).anyMatch(m -> m.authorSignature() == null);
    }

    private void thereIsMessageFrom(String authorUserName, List<ReadInboxResultDto.MessageDto> messages) {
        assertThat(messages).anyMatch(m -> m.authorSignature() != null && m.authorSignature().startsWith(authorUserName + "#"));
    }
}

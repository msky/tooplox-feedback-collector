package tooplox.feedbackcollector.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.dto.ShowFeedbackResultDto;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure.InboxNotFound;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure.NotAuthorizedToReadFromInbox;
import tooplox.shared.domain.InboxId;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static tooplox.feedbackcollector.utils.AuthenticatedUserBuilder.authenticatedUser;
import static tooplox.feedbackcollector.utils.SubmitFeedbackCommandBuilder.sampleSubmitFeedbackCommand;
import static tooplox.feedbackcollector.utils.TestUtils.someRandomDateTime;

public class ShouldAllowToSubmitAndShowFeedbackTest extends BaseFeedbackCollectorTest {

    @Test
    void shouldShowSubmittedFeedback() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated(authenticatedUser().withSignature("Alice#hash").build());

        // when
        val messageSubmissionDate = someRandomDateTime();
        timeIs(messageSubmissionDate);

        val submitFeedbackCommand = sampleSubmitFeedbackCommand().toInbox(inboxId).build();
        submitFeedback(submitFeedbackCommand);

        userIsAuthenticated("Bob");

        // then
        val feedback = showFeedback(inboxId).get();
        assertThat(feedback.messages()).hasSize(1);

        val message = feedback.messages().getFirst();
        assertThat(message.id()).isNotNull();
        assertThat(message.content()).isEqualTo(submitFeedbackCommand.content());
        assertThat(message.authorSignature()).isEqualTo("Alice#hash");
        assertThat(message.submittedAt()).isEqualTo(messageSubmissionDate);
    }

    @Test
    void shouldNotShowTheAuthorOfAnonymousFeedback() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand()
                .allowingAnonymousFeedback(true)
                .build()).get().inboxId();


        // when
        userIsAuthenticated("Alice");
        submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        thereIsNoAuthenticatedUser();
        submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        userIsAuthenticated("Bob");

        // then
        val feedback = showFeedback(inboxId).get();
        assertThat(feedback.messages()).hasSize(2);

        thereIsAnonymousMessageIn(feedback.messages());
        thereIsMessageFrom("Alice", feedback.messages());
    }

    @Test
    void shouldOnlyShowFeedbackForTheGivenInbox() {
        // given
        userIsAuthenticated("Bob");
        val firstInboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();
        val secondInboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");
        submitFeedback(sampleSubmitFeedbackCommand().toInbox(firstInboxId).build());

        userIsAuthenticated("Mickey Mouse");
        submitFeedback(sampleSubmitFeedbackCommand().toInbox(secondInboxId).build());

        userIsAuthenticated("Bob");

        // when
        val firstInboxFeedback = showFeedback(firstInboxId).get();
        val secondInboxFeedback = showFeedback(secondInboxId).get();

        // then
        assertThat(firstInboxFeedback.messages()).hasSize(1);
        assertThat(secondInboxFeedback.messages()).hasSize(1);

        thereIsMessageFrom("Alice", firstInboxFeedback.messages());
        thereIsMessageFrom("Mickey Mouse", secondInboxFeedback.messages());
    }

    @Test
    void shouldOnlyShowFeedbackToTheOwnerOfTheInbox() {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInbox(sampleCreateInboxCommand().build()).get().inboxId();

        userIsAuthenticated("Alice");
        submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        // when
        val result = showFeedback(inboxId);

        // then
        failedBecauseOf(result, NotAuthorizedToReadFromInbox.class);
    }

    @Test
    void shouldFailWhenShowingFeedbackForNonExistingOrMissingInbox() {
        // given
        userIsAuthenticated("Bob");

        // when
        val resultForNonExistingInbox = showFeedback(InboxId.generate());
        val resultForMissingInbox = showFeedback(null);

        // then
        failedBecauseOf(resultForNonExistingInbox, InboxNotFound.class);
        failedBecauseOf(resultForMissingInbox, InboxNotFound.class);
    }

    // TODO
//    @Test
//    void shouldShowRequestedPageOfFeedbackMessages() {
//    }


    private void thereIsAnonymousMessageIn(List<ShowFeedbackResultDto.MessageDto> messages) {
        assertThat(messages).anyMatch(m -> m.authorSignature() == null);
    }

    private void thereIsMessageFrom(String authorUserName, List<ShowFeedbackResultDto.MessageDto> messages) {
        assertThat(messages).anyMatch(m -> m.authorSignature() != null && m.authorSignature().startsWith(authorUserName + "#"));
    }
}

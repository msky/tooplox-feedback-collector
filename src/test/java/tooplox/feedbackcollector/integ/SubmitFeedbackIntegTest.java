package tooplox.feedbackcollector.integ;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure.NotAuthorizedToReadFromInbox;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tooplox.feedbackcollector.utils.SendMessageCommandBuilder.sampleSendMessageCommand;
import static tooplox.feedbackcollector.utils.TestUtils.someRandomDateTime;
import static tooplox.shared.domain.UserSignature.SEPARATOR;

class SubmitMessageIntegIntegTest extends BaseFeedbackCollectorIntegTest {

    @Test
    void shouldAllowToSubmitAnonymousMessage() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInboxIdAndReturnId(sampleCreateInboxCommand()
                .allowingAnonymousMessages(true)
                .build());

        thereIsNoAuthenticatedUser();

        // when
        val submitMessageResult = sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        // then
        succeeded(submitMessageResult);
    }

    @Test
    void shouldShowSubmittedMessage() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInboxIdAndReturnId(sampleCreateInboxCommand().build());

        userIsAuthenticated("Alice");

        // when
        val messageSubmissionDate = someRandomDateTime();
        timeIs(messageSubmissionDate);

        val submitMessageCommand = sampleSendMessageCommand().toInbox(inboxId).build();
        sendMessage(submitMessageCommand);

        userIsAuthenticated("Bob");

        // then
        showMessage(inboxId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].id").isNotEmpty())
                .andExpect(jsonPath("$.messages[0].content").value(submitMessageCommand.content()))
                .andExpect(jsonPath("$.messages[0].authorSignature").value(startsWith("Alice" + SEPARATOR)))
                .andExpect(jsonPath("$.messages[0].submittedAt").value(messageSubmissionDate.toString()));
    }


    @Test
    void shouldOnlyShowMessageToTheOwnerOfTheInbox() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInboxIdAndReturnId(sampleCreateInboxCommand().build());

        userIsAuthenticated("Alice");
        sendMessage(sampleSendMessageCommand().toInbox(inboxId).build());

        // when
        val result = showMessage(inboxId);

        // then
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.type").value(NotAuthorizedToReadFromInbox.class.getSimpleName()));
    }

}

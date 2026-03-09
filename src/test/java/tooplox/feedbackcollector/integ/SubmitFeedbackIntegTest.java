package tooplox.feedbackcollector.integ;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure.NotAuthorizedToReadFromInbox;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tooplox.feedbackcollector.utils.SubmitFeedbackCommandBuilder.sampleSubmitFeedbackCommand;
import static tooplox.feedbackcollector.utils.TestUtils.someRandomDateTime;
import static tooplox.shared.domain.UserSignature.SEPARATOR;

class SubmitFeedbackIntegTest extends BaseFeedbackCollectorIntegTest {

    @Test
    void shouldAllowToSubmitAnonymousFeedback() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInboxIdAndReturnId(sampleCreateInboxCommand()
                .allowingAnonymousFeedback(true)
                .build());

        thereIsNoAuthenticatedUser();

        // when
        val submitFeedbackResult = submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        // then
        succeeded(submitFeedbackResult);
    }

    @Test
    void shouldShowSubmittedFeedback() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInboxIdAndReturnId(sampleCreateInboxCommand().build());

        userIsAuthenticated("Alice");

        // when
        val messageSubmissionDate = someRandomDateTime();
        timeIs(messageSubmissionDate);

        val submitFeedbackCommand = sampleSubmitFeedbackCommand().toInbox(inboxId).build();
        submitFeedback(submitFeedbackCommand);

        userIsAuthenticated("Bob");

        // then
        showFeedback(inboxId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].id").isNotEmpty())
                .andExpect(jsonPath("$.messages[0].content").value(submitFeedbackCommand.content()))
                .andExpect(jsonPath("$.messages[0].authorSignature").value(startsWith("Alice" + SEPARATOR)))
                .andExpect(jsonPath("$.messages[0].submittedAt").value(messageSubmissionDate.toString()));
    }


    @Test
    void shouldOnlyShowFeedbackToTheOwnerOfTheInbox() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxId = createInboxIdAndReturnId(sampleCreateInboxCommand().build());

        userIsAuthenticated("Alice");
        submitFeedback(sampleSubmitFeedbackCommand().toInbox(inboxId).build());

        // when
        val result = showFeedback(inboxId);

        // then
        result.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.type").value(NotAuthorizedToReadFromInbox.class.getSimpleName()));
    }

}

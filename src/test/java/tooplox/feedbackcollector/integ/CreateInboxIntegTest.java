package tooplox.feedbackcollector.integ;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CreateInboxIntegTest extends BaseFeedbackCollectorIntegTest {

    @Test
    void shouldAllowToCreateInbox() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val createInboxCommand = sampleCreateInboxCommand()
                .expiringOn(randomFutureDate())
                .allowingAnonymousFeedback(true)
                .build();

        // when
        val result = createInbox(createInboxCommand);

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.inboxId").exists())
                .andExpect(jsonPath("$.inboxId").isNotEmpty())
                .andExpect(jsonPath("$.inboxId").isString());
    }


    @Test
    void shouldFailWhenCreatingInboxWithoutOwner() throws Exception {
        // given
        thereIsNoAuthenticatedUser();

        // when
        val resultWithoutOwner = createInbox(sampleCreateInboxCommand().build());

        // then
        resultWithoutOwner.andExpect(status().is4xxClientError());
    }
}

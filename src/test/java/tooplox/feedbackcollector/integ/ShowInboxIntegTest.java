package tooplox.feedbackcollector.integ;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tooplox.shared.domain.UserSignature.SEPARATOR;

class ShowInboxIntegTest extends BaseFeedbackCollectorIntegTest {

    @Test
    void shouldShowInboxToTheOwner() throws Exception {
        // given
        userIsAuthenticated("Bob");
        val inboxExpirationDate = randomFutureDate();
        val inboxId = deserialize(createInbox(sampleCreateInboxCommand()
                .expiringOn(inboxExpirationDate)
                .allowingAnonymousFeedback(true)
                .withTopic("my topic")
                .build())
                .andReturn().getResponse().getContentAsString(), CreateInboxResultDto.class).inboxId();

        // when
        val result = showInbox(new ShowInboxQuery(inboxId));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inboxId.value()))
                .andExpect(jsonPath("$.topic").value("my topic"))
                .andExpect(jsonPath("$.expiringOn").value(inboxExpirationDate.format(ISO_LOCAL_DATE_TIME)))
                .andExpect(jsonPath("$.ownerSignature").value(startsWith("Bob" + SEPARATOR)));
    }
}

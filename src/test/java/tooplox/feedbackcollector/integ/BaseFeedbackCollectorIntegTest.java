package tooplox.feedbackcollector.integ;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SendMessageCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.queries.ReadInboxQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxInformationQuery;
import tooplox.feedbackcollector.infra.rest.v1.RequestPerformer;
import tooplox.feedbackcollector.utils.CreateInboxCommandBuilder;
import tooplox.feedbackcollector.utils.TestUtils;
import tooplox.shared.domain.InboxId;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
abstract class BaseFeedbackCollectorIntegTest {

    @MockitoBean
    Clock clock;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RequestPerformer performer;


    Credentials currentCredentials = Credentials.anonymous();

    @BeforeEach
    void setUp() {
        timeIs(LocalDateTime.now(ZoneOffset.UTC));
        currentCredentials = Credentials.anonymous();
    }

    ResultActions createInbox(CreateInboxCommand command) {
        return performer.createInbox(command, currentCredentials);
    }

    InboxId createInboxIdAndReturnId(CreateInboxCommand command) throws Exception {
        return deserialize(createInbox(command).andReturn().getResponse().getContentAsString(),
                CreateInboxResultDto.class).inboxId();
    }

    ResultActions sendMessage(SendMessageCommand command) {
        return performer.sendMessage(command, currentCredentials);
    }

    ResultActions showInboxInformation(ShowInboxInformationQuery query) {
        return performer.showInbox(query, currentCredentials);
    }

    ResultActions showMessage(InboxId inboxId) {
        return performer.showMessage(new ReadInboxQuery(inboxId), currentCredentials);
    }

    void succeeded(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isOk());
    }

    void timeIs(LocalDateTime time) {
        when(clock.instant()).thenReturn(time.toInstant(ZoneOffset.UTC));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    CreateInboxCommandBuilder sampleCreateInboxCommand() {
        return CreateInboxCommandBuilder.sampleCreateInboxCommand(clock);
    }

    LocalDateTime randomFutureDate() {
        return TestUtils.randomFutureDate(clock);
    }

    void userIsAuthenticated(String userName) {
        this.currentCredentials = Credentials.withName(userName);
    }

    void thereIsNoAuthenticatedUser() {
        currentCredentials = Credentials.anonymous();
    }

    <T> T deserialize(String json, Class<T> clazz) {
        return objectMapper.readValue(json, clazz);
    }


}

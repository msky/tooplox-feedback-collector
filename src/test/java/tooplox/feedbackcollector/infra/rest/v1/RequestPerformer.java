package tooplox.feedbackcollector.infra.rest.v1;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.queries.ShowFeedbackQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.feedbackcollector.integ.Credentials;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@RequiredArgsConstructor
@Component
public class RequestPerformer {
    private final MockMvc mockMvc;

    public ResultActions createInbox(CreateInboxCommand command, Credentials credentials) {
        return perform(applyCredentials(post("/api/v1/inboxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "expirationDate": "%s",
                                    "allowsAnonymousFeedback": %s
                                }
                                """
                                .formatted(
                                        command.expirationDate().format(ISO_LOCAL_DATE_TIME),
                                        command.allowsAnonymousFeedback())
                        ),
                credentials));
    }

    public ResultActions showInbox(ShowInboxQuery query, Credentials credentials) {
        return perform(applyCredentials(get("/api/v1/inboxes")
                .param("id", query.inboxId().value()), credentials));
    }

    public ResultActions submitFeedback(SubmitFeedbackCommand command, Credentials credentials) {
        return perform(applyCredentials(post("/api/v1/inboxes/{inboxId}/messages", command.inboxId().value())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "content": "%s"
                        }
                        """
                        .formatted(command.content())), credentials));
    }

    public ResultActions showFeedback(ShowFeedbackQuery query, Credentials credentials) {
        return perform(applyCredentials(get("/api/v1/inboxes/{inboxId}/messages", query.inboxId().value()), credentials));
    }

    private ResultActions perform(MockHttpServletRequestBuilder builder) {
        try {
            return mockMvc.perform(builder);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to perform request", e);
        }
    }

    private MockHttpServletRequestBuilder applyCredentials(MockHttpServletRequestBuilder builder, Credentials credentials) {
        if (credentials == null || credentials.isAnonymous())
            return builder;
        else
            return builder.header("Authorization", credentials.basicAuth());
    }
}

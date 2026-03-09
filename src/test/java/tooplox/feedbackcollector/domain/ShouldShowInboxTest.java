package tooplox.feedbackcollector.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.failures.ShowInboxFailure.InboxNotFound;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.shared.domain.InboxId;

import static org.assertj.core.api.Assertions.assertThat;
import static tooplox.feedbackcollector.utils.AuthenticatedUserBuilder.authenticatedUser;

public class ShouldShowInboxTest extends BaseFeedbackCollectorTest {

    @Test
    void shouldShowInboxToTheOwner() {
        // given
        userIsAuthenticated(authenticatedUser().withSignature("Bob#hash").build());
        val inboxExpirationDate = randomFutureDate();
        val inboxId = createInbox(sampleCreateInboxCommand()
                .expiringOn(inboxExpirationDate)
                .withTopic("my topic")
                .build()).get().inboxId();

        // when
        val result = showInbox(new ShowInboxQuery(inboxId)).get();

        // then
        assertThat(result.id()).isEqualTo(inboxId);
        assertThat(result.topic()).isEqualTo("my topic");
        assertThat(result.expiringOn()).isEqualTo(inboxExpirationDate);
        assertThat(result.ownerSignature()).isEqualTo("Bob#hash");
    }

    @Test
    void shouldShowInboxCreatedBySomeoneElse() {
        // given
        userIsAuthenticated(authenticatedUser().withSignature("Bob#hash").build());
        val expirationDate = randomFutureDate();
        val inboxId = createInbox(sampleCreateInboxCommand()
                .expiringOn(expirationDate)
                .withTopic("shared topic")
                .build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val result = showInbox(new ShowInboxQuery(inboxId)).get();

        // then
        assertThat(result.id()).isEqualTo(inboxId);
        assertThat(result.topic()).isEqualTo("shared topic");
        assertThat(result.expiringOn()).isEqualTo(expirationDate);
        assertThat(result.ownerSignature()).isEqualTo("Bob#hash");
    }

    @Test
    void shouldFailWhenShowingNonExistingInbox() {
        // when
        val notExistingInboxResult = showInbox(new ShowInboxQuery(InboxId.generate()));

        // then
        failedBecauseOf(notExistingInboxResult, InboxNotFound.class);

        // when
        val missingInboxResult = showInbox(new ShowInboxQuery(null));

        // then
        failedBecauseOf(missingInboxResult, InboxNotFound.class);
    }

}

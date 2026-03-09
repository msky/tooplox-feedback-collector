package tooplox.feedbackcollector.domain;

import lombok.val;
import org.junit.jupiter.api.Test;
import tooplox.feedbackcollector.domain.failures.ShowInboxFailure.InboxNotFound;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.shared.domain.InboxId;

import static org.assertj.core.api.Assertions.assertThat;

public class ShouldShowInboxTest extends BaseFeedbackCollectorTest {

    @Test
    void shouldShowInboxToTheOwner() {
        // given
        userIsAuthenticated("Bob");
        val inboxExpirationDate = randomFutureDate();
        val inboxId = createInbox(sampleCreateInboxCommand().expiringOn(inboxExpirationDate).build()).get().inboxId();

        // when
        val result = showInbox(new ShowInboxQuery(inboxId)).get();

        // then
        assertThat(result.id()).isEqualTo(inboxId);
        assertThat(result.expiringOn()).isEqualTo(inboxExpirationDate);
        assertThat(result.ownerSignature()).isEqualTo("Bob");
    }

    @Test
    void shouldShowInboxCreatedBySomeoneElse() {
        // given
        userIsAuthenticated("Bob");
        val expirationDate = randomFutureDate();
        val inboxId = createInbox(sampleCreateInboxCommand().expiringOn(expirationDate).build()).get().inboxId();

        userIsAuthenticated("Alice");

        // when
        val result = showInbox(new ShowInboxQuery(inboxId)).get();

        // then
        assertThat(result.id()).isEqualTo(inboxId);
        assertThat(result.expiringOn()).isEqualTo(expirationDate);
        assertThat(result.ownerSignature()).isEqualTo("Bob");
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

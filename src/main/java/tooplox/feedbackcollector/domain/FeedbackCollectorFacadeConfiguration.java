package tooplox.feedbackcollector.domain;

import tooplox.feedbackcollector.domain.impl.InboxFactory;
import tooplox.feedbackcollector.domain.impl.InboxRepository;
import tooplox.feedbackcollector.domain.impl.NewInboxValidator;

import java.time.Clock;

public class FeedbackCollectorFacadeConfiguration {

    public FeedbackCollectorFacade feedbackCollectorFacade(
            int maxOwnerUserNameLength,
            Clock clock,
            InboxRepository inboxRepository

    ) {
        return new FeedbackCollectorFacade(
                new NewInboxValidator(maxOwnerUserNameLength, clock),
                new InboxFactory(),
                inboxRepository
        );
    }
}

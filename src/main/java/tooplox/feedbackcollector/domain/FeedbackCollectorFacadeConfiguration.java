package tooplox.feedbackcollector.domain;

import tooplox.feedbackcollector.domain.impl.*;

import java.time.Clock;

public class FeedbackCollectorFacadeConfiguration {

    public FeedbackCollectorFacade feedbackCollectorFacade(
            int maxOwnerUserNameLength,
            int maxFeedbackContentLength,
            Clock clock,
            InboxRepository inboxRepository,
            MessageRepository messageRepository
    ) {
        return new FeedbackCollectorFacade(
                new NewInboxValidator(maxOwnerUserNameLength, clock),
                new InboxFactory(),
                inboxRepository,
                new MessageValidator(maxFeedbackContentLength, clock),
                new MessageFactory(),
                messageRepository
        );
    }
}

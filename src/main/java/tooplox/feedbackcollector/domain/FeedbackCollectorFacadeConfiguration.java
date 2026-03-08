package tooplox.feedbackcollector.domain;

import tooplox.feedbackcollector.domain.impl.*;
import tooplox.shared.domain.AuthenticatedUserProvider;

import java.time.Clock;

public class FeedbackCollectorFacadeConfiguration {

    public FeedbackCollectorFacade feedbackCollectorFacade(
            AuthenticatedUserProvider authenticatedUserProvider,
            int maxFeedbackContentLength,
            Clock clock,
            InboxRepository inboxRepository,
            MessageRepository messageRepository
    ) {
        return new FeedbackCollectorFacade(
                new NewInboxValidator(clock, authenticatedUserProvider),
                new InboxFactory(authenticatedUserProvider),
                inboxRepository,
                new MessageValidator(maxFeedbackContentLength, clock, authenticatedUserProvider),
                new MessageFactory(),
                messageRepository,
                authenticatedUserProvider
        );
    }
}

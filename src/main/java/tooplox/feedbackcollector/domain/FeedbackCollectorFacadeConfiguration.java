package tooplox.feedbackcollector.domain;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tooplox.feedbackcollector.domain.impl.*;
import tooplox.shared.authentication.AuthenticatedUserProvider;

import java.time.Clock;

@Configuration
public class FeedbackCollectorFacadeConfiguration {

    @Bean
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
                new MessageFactory(clock, authenticatedUserProvider),
                messageRepository,
                authenticatedUserProvider
        );
    }

    @Bean
    public int maxFeedbackContentLength() {
        return 50;
    }
}

package tooplox.feedbackcollector.domain;


import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SendMessageCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.dto.ReadInboxResultDto;
import tooplox.feedbackcollector.domain.dto.ShowInboxInformationResultDto;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure;
import tooplox.feedbackcollector.domain.failures.ShowInboxInformationFailure;
import tooplox.feedbackcollector.domain.impl.InboxRepository;
import tooplox.feedbackcollector.domain.impl.MessageRepository;
import tooplox.feedbackcollector.domain.queries.ReadInboxQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxInformationQuery;
import tooplox.feedbackcollector.infra.db.embedded.InMemoryInboxRepository;
import tooplox.feedbackcollector.infra.db.embedded.InMemoryMessageRepository;
import tooplox.feedbackcollector.utils.CreateInboxCommandBuilder;
import tooplox.feedbackcollector.utils.TestUtils;
import tooplox.shared.authentication.AuthenticatedUser;
import tooplox.shared.authentication.AuthenticatedUserProvider;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.Success;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.time.ZoneOffset.UTC;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tooplox.feedbackcollector.utils.AuthenticatedUserBuilder.authenticatedUser;

abstract class BaseFeedbackCollectorTest {
    static final int MAX_FEEDBACK_CONTENT_LENGTH = 50;

    FeedbackCollectorFacade feedbackCollectorFacade;
    Clock clock = mock(Clock.class);
    InboxRepository inboxRepository = new InMemoryInboxRepository();
    MessageRepository messageRepository = new InMemoryMessageRepository();
    AuthenticatedUserProvider authenticatedUserProvider = mock(AuthenticatedUserProvider.class);

    @BeforeEach
    public void setUp() {
        timeIs(LocalDateTime.now());
        feedbackCollectorFacade = configureModule();
    }

    Either<CreateInboxFailure, CreateInboxResultDto> createInbox(CreateInboxCommand command) {
        return feedbackCollectorFacade.createInbox(command);
    }

    Either<SendMessageFailure, Success> sendMessage(SendMessageCommand command) {
        return feedbackCollectorFacade.sendMessage(command);
    }

    Either<ShowInboxInformationFailure, ShowInboxInformationResultDto> showInboxInformation(ShowInboxInformationQuery query) {
        return feedbackCollectorFacade.showInboxInformation(query);
    }

    Either<ReadInboxFailure, ReadInboxResultDto> readInbox(InboxId inboxId) {
        return feedbackCollectorFacade.readInbox(new ReadInboxQuery(inboxId));
    }

    private FeedbackCollectorFacade configureModule() {
        FeedbackCollectorFacadeConfiguration configuration = new FeedbackCollectorFacadeConfiguration();
        return configuration.feedbackCollectorFacade(
                authenticatedUserProvider,
                MAX_FEEDBACK_CONTENT_LENGTH,
                clock,
                inboxRepository,
                messageRepository);
    }

    void failedBecauseOf(Either<?, ?> result, Class<?> expectedFailureClass) {
        assertThat(result).isLeft().hasLeftValueSatisfying(failure -> assertTrue(expectedFailureClass.isInstance(failure)));
    }

    void succeeded(Either<?, Success> result) {
        assertThat(result).isRight();
    }

    void timeIs(LocalDateTime time) {
        when(clock.instant()).thenReturn(time.toInstant(UTC));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    CreateInboxCommandBuilder sampleCreateInboxCommand() {
        return CreateInboxCommandBuilder.sampleCreateInboxCommand(clock);
    }

    LocalDateTime randomFutureDate() {
        return TestUtils.randomFutureDate(clock);
    }

    void userIsAuthenticated(String userName) {
        userIsAuthenticated(authenticatedUser().withName(userName).build());
    }

    void userIsAuthenticated(AuthenticatedUser authenticatedUser) {
        when(authenticatedUserProvider.authenticatedUser()).thenReturn(
                authenticatedUser
        );
    }

    void thereIsNoAuthenticatedUser() {
        when(authenticatedUserProvider.authenticatedUser()).thenReturn(null);
    }
}

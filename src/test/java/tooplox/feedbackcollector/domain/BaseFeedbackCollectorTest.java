package tooplox.feedbackcollector.domain;


import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.dto.ShowFeedbackResultDto;
import tooplox.feedbackcollector.domain.dto.ShowInboxResultDto;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure;
import tooplox.feedbackcollector.domain.failures.ShowInboxFailure;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure;
import tooplox.feedbackcollector.domain.impl.InboxRepository;
import tooplox.feedbackcollector.domain.queries.ShowFeedbackQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.feedbackcollector.stubs.InMemoryInboxRepository;
import tooplox.feedbackcollector.utils.CreateInboxCommandBuilder;
import tooplox.feedbackcollector.utils.TestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static java.time.ZoneOffset.UTC;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

abstract class BaseFeedbackCollectorTest {
    static final int MAX_OWNER_USER_NAME_LENGTH = 255;

    FeedbackCollectorFacade feedbackCollectorFacade;
    Clock clock = mock(Clock.class);
    InboxRepository inboxRepository = new InMemoryInboxRepository();

    @BeforeEach
    public void setUp() {
        timeIs(LocalDateTime.now());
        feedbackCollectorFacade = configureModule();
    }

    Either<CreateInboxFailure, CreateInboxResultDto> createInbox(CreateInboxCommand command) {
        return feedbackCollectorFacade.createInbox(command);
    }

    Either<SubmitFeedbackFailure, Void> submitFeedback(SubmitFeedbackCommand command) {
        return feedbackCollectorFacade.submitFeedback(command);
    }

    Either<ShowInboxFailure, ShowInboxResultDto> showInbox(ShowInboxQuery query) {
        return feedbackCollectorFacade.showInbox(query);
    }

    Either<ShowFeedbackFailure, ShowFeedbackResultDto> showFeedback(ShowFeedbackQuery query) {
        return feedbackCollectorFacade.showFeedback(query);
    }

    private FeedbackCollectorFacade configureModule() {
        FeedbackCollectorFacadeConfiguration configuration = new FeedbackCollectorFacadeConfiguration();
        return configuration.feedbackCollectorFacade(MAX_OWNER_USER_NAME_LENGTH, clock, inboxRepository);
    }

    void failedBecauseOf(Either<?, ?> result, Class<?> expectedFailureClass) {
        assertThat(result).isLeft().hasLeftValueSatisfying(failure -> assertTrue(expectedFailureClass.isInstance(failure)));
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
}

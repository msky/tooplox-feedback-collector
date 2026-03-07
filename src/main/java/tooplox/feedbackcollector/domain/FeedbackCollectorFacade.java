package tooplox.feedbackcollector.domain;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.dto.ShowFeedbackResultDto;
import tooplox.feedbackcollector.domain.dto.ShowInboxResultDto;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure;
import tooplox.feedbackcollector.domain.failures.ShowInboxFailure;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure;
import tooplox.feedbackcollector.domain.impl.InboxFactory;
import tooplox.feedbackcollector.domain.impl.InboxRepository;
import tooplox.feedbackcollector.domain.impl.NewInboxValidator;
import tooplox.feedbackcollector.domain.queries.ShowFeedbackQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;

@RequiredArgsConstructor
@Slf4j
public class FeedbackCollectorFacade {
    private final NewInboxValidator newInboxValidator;
    private final InboxFactory inboxFactory;
    private final InboxRepository inboxRepository;

    public Either<CreateInboxFailure, CreateInboxResultDto> createInbox(CreateInboxCommand command) {
        log.info("Creating new inbox [ command = {} ]", command);
        return newInboxValidator.validate(command)
                .map(inboxFactory::createFrom)
                .map(inboxRepository::save)
                .map(inbox -> new CreateInboxResultDto(inbox.id()))
                .peekLeft(CreateInboxFailure::log)
                .peek(result -> log.info("Inbox created successfully [ id = {} ]", result.inboxId().value()));
    }

    public Either<SubmitFeedbackFailure, Void> submitFeedback(SubmitFeedbackCommand command) {
        return null;
    }

    public Either<ShowInboxFailure, ShowInboxResultDto> showInbox(ShowInboxQuery query) {
        return null;
    }

    public Either<ShowFeedbackFailure, ShowFeedbackResultDto> showFeedback(ShowFeedbackQuery query) {
        return null;
    }
}

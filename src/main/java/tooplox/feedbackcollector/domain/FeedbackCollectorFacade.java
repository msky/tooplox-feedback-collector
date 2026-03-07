package tooplox.feedbackcollector.domain;

import io.vavr.control.Either;
import io.vavr.control.Option;
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
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.InboxNotFound;
import tooplox.feedbackcollector.domain.impl.*;
import tooplox.feedbackcollector.domain.queries.ShowFeedbackQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.Success;

import static tooplox.shared.domain.Success.SUCCESS;

@RequiredArgsConstructor
@Slf4j
//TODO Add not null assertions to method args
public class FeedbackCollectorFacade {
    private final NewInboxValidator newInboxValidator;
    private final InboxFactory inboxFactory;
    private final InboxRepository inboxRepository;
    private final MessageValidator messageValidator;
    private final MessageFactory messageFactory;
    private final MessageRepository messageRepository;

    public Either<CreateInboxFailure, CreateInboxResultDto> createInbox(CreateInboxCommand command) {
        log.info("Creating new inbox [ command = {} ]", command);
        return newInboxValidator.validate(command)
                .map(inboxFactory::createFrom)
                .map(inboxRepository::save)
                .map(inbox -> new CreateInboxResultDto(inbox.id()))
                .peekLeft(CreateInboxFailure::log)
                .peek(result -> log.info("Inbox created successfully [ id = {} ]", result.inboxId().value()));
    }

    public Either<SubmitFeedbackFailure, Success> submitFeedback(SubmitFeedbackCommand command) {
        log.info("Submitting feedback. [ inboxId = {} submitter = {} ]", command.inboxId(), command.submitterUserName());
        return findInboxBy(command.inboxId())
                .flatMap(inbox -> messageValidator.checkIfMessageCanBeSubmittedTo(inbox, command))
                .map(messageFactory::createFrom)
                .map(messageRepository::save)
                .map(_ -> SUCCESS)
                .peekLeft(SubmitFeedbackFailure::log)
                .peek(_ -> log.info("Feedback submitted successfully [ inboxId = {} ]", command.inboxId()));
    }

    public Either<ShowInboxFailure, ShowInboxResultDto> showInbox(ShowInboxQuery query) {
        return null;
    }

    public Either<ShowFeedbackFailure, ShowFeedbackResultDto> showFeedback(ShowFeedbackQuery query) {
        return null;
    }

    private Either<SubmitFeedbackFailure, Inbox> findInboxBy(InboxId inboxId) {
        return Option.of(inboxId)
                .flatMap(id -> Option.ofOptional(inboxRepository.findBy(id)))
                .toEither(InboxNotFound::new);
    }
}

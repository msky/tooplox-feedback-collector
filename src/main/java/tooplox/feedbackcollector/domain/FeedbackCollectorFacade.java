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
import tooplox.feedbackcollector.domain.impl.*;
import tooplox.feedbackcollector.domain.queries.ShowFeedbackQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxQuery;
import tooplox.shared.authentication.AuthenticatedUser;
import tooplox.shared.authentication.AuthenticatedUserProvider;
import tooplox.shared.domain.*;

import java.util.Optional;

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
    private final AuthenticatedUserProvider authenticatedUserProvider;

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
        log.info("Submitting feedback. [ inboxId = {} author = {}]",
                command.inboxId(),
                currentUserName().orElse("anonymous"));

        return findInboxBy(command.inboxId(), (SubmitFeedbackFailure) new SubmitFeedbackFailure.InboxNotFound())
                .flatMap(inbox -> messageValidator.checkIfMessageCanBeSubmittedTo(inbox, command))
                .map(messageFactory::createFrom)
                .map(messageRepository::save)
                .map(_ -> SUCCESS)
                .peekLeft(SubmitFeedbackFailure::log)
                .peek(_ -> log.info("Feedback submitted successfully [ inboxId = {} ]", command.inboxId()));
    }

    private Optional<String> currentUserName() {
        return Optional.ofNullable(currentUser()).map(AuthenticatedUser::userName).map(UserName::value);
    }

    public Either<ShowInboxFailure, ShowInboxResultDto> showInbox(ShowInboxQuery query) {
        log.info("Showing inbox [ inboxId = {} ]", query.inboxId());

        return findInboxBy(query.inboxId(), (ShowInboxFailure) new ShowInboxFailure.InboxNotFound())
                .map(inbox -> new ShowInboxResultDto(
                        inbox.id(),
                        inbox.ownerSignature().value(),
                        inbox.expiresOn()))
                .peekLeft(ShowInboxFailure::log)
                .peek(result -> log.info("Inbox retrieved successfully [ inboxId = {} ]", result.id()));
    }

    public Either<ShowFeedbackFailure, ShowFeedbackResultDto> showFeedback(ShowFeedbackQuery query) {
        log.info("Showing feedback for inbox [ inboxId = {} ]", query.inboxId());

        return findInboxBy(query.inboxId(), (ShowFeedbackFailure) new ShowFeedbackFailure.InboxNotFound())
                .flatMap(inbox -> inbox.canBeReadBy(currentUser()))
                .map(_ -> messageRepository.findBy(query.inboxId())
                        .stream()
                        .map(Message::toDto)
                        .toList())
                .map(ShowFeedbackResultDto::new)
                .peekLeft(ShowFeedbackFailure::log)
                .peek(result -> log.info("Feedback for inbox [ inboxId = {} ] retrieved successfully. [ messagesCount = {} ]",
                        query.inboxId(), result.messages().size()));

    }

    private AuthenticatedUser currentUser() {
        return authenticatedUserProvider.authenticatedUser();
    }

    private <T> Either<T, Inbox> findInboxBy(InboxId inboxId, T failure) {
        return Option.of(inboxId)
                .flatMap(id -> Option.ofOptional(inboxRepository.findBy(id)))
                .toEither(() -> failure);
    }
}

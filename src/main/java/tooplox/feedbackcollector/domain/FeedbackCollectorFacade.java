package tooplox.feedbackcollector.domain;

import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.commands.SendMessageCommand;
import tooplox.feedbackcollector.domain.dto.CreateInboxResultDto;
import tooplox.feedbackcollector.domain.dto.ReadInboxResultDto;
import tooplox.feedbackcollector.domain.dto.ShowInboxInformationResultDto;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure;
import tooplox.feedbackcollector.domain.failures.ShowInboxInformationFailure;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure;
import tooplox.feedbackcollector.domain.impl.*;
import tooplox.feedbackcollector.domain.queries.ReadInboxQuery;
import tooplox.feedbackcollector.domain.queries.ShowInboxInformationQuery;
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

    public Either<SendMessageFailure, Success> sendMessage(SendMessageCommand command) {
        log.info("Submitting message. [ inboxId = {} author = {}]",
                command.inboxId(),
                currentUserName().orElse("anonymous"));

        return findInboxBy(command.inboxId(), (SendMessageFailure) new SendMessageFailure.InboxNotFound())
                .flatMap(inbox -> messageValidator.checkIfMessageCanBeSubmittedTo(inbox, command))
                .map(messageFactory::createFrom)
                .map(messageRepository::save)
                .map(_ -> SUCCESS)
                .peekLeft(SendMessageFailure::log)
                .peek(_ -> log.info("Message submitted successfully [ inboxId = {} ]", command.inboxId()));
    }

    public Either<ShowInboxInformationFailure, ShowInboxInformationResultDto> showInboxInformation(ShowInboxInformationQuery query) {
        log.info("Showing inbox [ inboxId = {} ]", query.inboxId());

        return findInboxBy(query.inboxId(), (ShowInboxInformationFailure) new ShowInboxInformationFailure.InboxNotFound())
                .map(inbox -> new ShowInboxInformationResultDto(
                        inbox.id(),
                        inbox.topic().value(),
                        inbox.ownerSignature().value(),
                        inbox.expiresOn()))
                .peekLeft(ShowInboxInformationFailure::log)
                .peek(result -> log.info("Inbox retrieved successfully [ inboxId = {} ]", result.id()));
    }

    public Either<ReadInboxFailure, ReadInboxResultDto> readInbox(ReadInboxQuery query) {
        log.info("Reading inbox [ inboxId = {} ]", query.inboxId());

        return findInboxBy(query.inboxId(), (ReadInboxFailure) new ReadInboxFailure.InboxNotFound())
                .flatMap(inbox -> inbox.canBeReadBy(currentUser()))
                .map(_ -> messageRepository.findBy(query.inboxId())
                        .stream()
                        .map(Message::toDto)
                        .toList())
                .map(ReadInboxResultDto::new)
                .peekLeft(ReadInboxFailure::log)
                .peek(result -> log.info("Inbox [ inboxId = {} ] read successfully. [ messagesCount = {} ]",
                        query.inboxId(), result.messages().size()));

    }

    private Optional<String> currentUserName() {
        return Optional.ofNullable(currentUser()).map(AuthenticatedUser::userName).map(UserName::value);
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

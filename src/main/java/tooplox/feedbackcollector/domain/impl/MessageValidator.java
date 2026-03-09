package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import tooplox.feedbackcollector.domain.commands.SendMessageCommand;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure.ContentTooLarge;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure.NoContent;
import tooplox.shared.authentication.AuthenticatedUser;
import tooplox.shared.authentication.AuthenticatedUserProvider;

import java.time.Clock;

@RequiredArgsConstructor
public class MessageValidator {
    private final int maxMessageContentLength;
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Either<SendMessageFailure, SendMessageCommand> checkIfMessageCanBeSubmittedTo(Inbox inbox,
                                                                                         SendMessageCommand command) {
        if (command.content() == null || command.content().isBlank()) {
            return Either.left(new NoContent());
        } else if (command.content().length() > maxMessageContentLength) {
            return Either.left(new ContentTooLarge());
        } else {
            return inbox.acceptsMessage(messageAuthor(), clock).map(_ -> command);
        }
    }

    private AuthenticatedUser messageAuthor() {
        return authenticatedUserProvider.authenticatedUser();
    }
}

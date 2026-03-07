package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.ContentTooLarge;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.NoContent;

import java.time.Clock;

@RequiredArgsConstructor
public class MessageValidator {
    private final int maxFeedbackContentLength;
    private final Clock clock;

    public Either<SubmitFeedbackFailure, SubmitFeedbackCommand> checkIfMessageCanBeSubmittedTo(Inbox inbox,
                                                                                               SubmitFeedbackCommand command) {
        if (command.content() == null || command.content().isBlank()) {
            return Either.left(new NoContent());
        } else if (command.content().length() > maxFeedbackContentLength) {
            return Either.left(new ContentTooLarge());
        } else {
            return inbox.acceptsMessage(command, clock).map(_ -> command);
        }
    }
}

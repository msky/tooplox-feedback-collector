package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.AnonymousFeedbackNotAllowed;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.InboxExpired;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.SubmittingFeedbackToYourself;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.Success;

import java.time.Clock;
import java.time.LocalDateTime;

import static tooplox.shared.domain.Success.SUCCESS;


public record Inbox(
        InboxId id,
        LocalDateTime expiresOn,
        boolean allowsAnonymousMessages,
        Owner owner
) {

    public Either<SubmitFeedbackFailure, Success> acceptsMessage(SubmitFeedbackCommand command, Clock clock) {
        if (isInboxExpired(clock)) {
            return Either.left(new InboxExpired());
        } else if (command.isMessageAnonymous() && !allowsAnonymousMessages) {
            return Either.left(new AnonymousFeedbackNotAllowed());
        } else if (isMessageSendByOwner(command)) {
            return Either.left(new SubmittingFeedbackToYourself());
        } else {
            return Either.right(SUCCESS);
        }
    }

    private boolean isMessageSendByOwner(SubmitFeedbackCommand command) {
        return owner.isSameAs(command.submitterUserName());
    }

    private boolean isInboxExpired(Clock clock) {
        return expiresOn.isBefore(LocalDateTime.now(clock));
    }

    record Owner(String userName) {
        public boolean isSameAs(String otherUserName) {
            return userName.equals(otherUserName);
        }
    }
}

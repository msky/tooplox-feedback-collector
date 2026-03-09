package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import jakarta.annotation.Nullable;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure;
import tooplox.feedbackcollector.domain.failures.ReadInboxFailure.NotAuthorizedToReadFromInbox;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure.AnonymousMessageNotAllowed;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure.InboxExpired;
import tooplox.feedbackcollector.domain.failures.SendMessageFailure.SubmittingMessageToYourself;
import tooplox.shared.authentication.AuthenticatedUser;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.Success;
import tooplox.shared.domain.UserSignature;

import java.time.Clock;
import java.time.LocalDateTime;

import static tooplox.shared.domain.Success.SUCCESS;


public record Inbox(
        InboxId id,
        Topic topic,
        LocalDateTime expiresOn,
        boolean allowsAnonymousMessages,
        Owner owner
) {

    public Either<SendMessageFailure, Success> acceptsMessage(@Nullable AuthenticatedUser messageAuthor, Clock clock) {
        if (isInboxExpired(clock)) {
            return Either.left(new InboxExpired());
        } else if (isMessageAnonymous(messageAuthor) && !allowsAnonymousMessages) {
            return Either.left(new AnonymousMessageNotAllowed());
        } else if (isMessageSendByOwner(messageAuthor)) {
            return Either.left(new SubmittingMessageToYourself());
        } else {
            return Either.right(SUCCESS);
        }
    }

    private boolean isMessageSendByOwner(AuthenticatedUser messageAuthor) {
        return owner.isSameAs(messageAuthor);
    }

    private boolean isInboxExpired(Clock clock) {
        return expiresOn.isBefore(LocalDateTime.now(clock));
    }

    private boolean isMessageAnonymous(AuthenticatedUser author) {
        return author == null;
    }

    public Either<ReadInboxFailure, Success> canBeReadBy(AuthenticatedUser authenticatedUser) {
        if (owner.isSameAs(authenticatedUser)) {
            return Either.right(SUCCESS);
        } else {
            return Either.left(new NotAuthorizedToReadFromInbox());
        }
    }

    public UserSignature ownerSignature() {
        return owner.signature();
    }

    record Owner(UserSignature signature) {
        public boolean isSameAs(AuthenticatedUser user) {
            return user != null && signature.equals(user.signature());
        }
    }
}

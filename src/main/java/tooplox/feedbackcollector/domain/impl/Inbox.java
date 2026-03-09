package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import jakarta.annotation.Nullable;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure;
import tooplox.feedbackcollector.domain.failures.ShowFeedbackFailure.NotAuthorizedToReadFromInbox;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.AnonymousFeedbackNotAllowed;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.InboxExpired;
import tooplox.feedbackcollector.domain.failures.SubmitFeedbackFailure.SubmittingFeedbackToYourself;
import tooplox.shared.authentication.AuthenticatedUser;
import tooplox.shared.domain.*;

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

    public Either<SubmitFeedbackFailure, Success> acceptsMessage(@Nullable AuthenticatedUser messageAuthor, Clock clock) {
        if (isInboxExpired(clock)) {
            return Either.left(new InboxExpired());
        } else if (isMessageAnonymous(messageAuthor) && !allowsAnonymousMessages) {
            return Either.left(new AnonymousFeedbackNotAllowed());
        } else if (isMessageSendByOwner(messageAuthor)) {
            return Either.left(new SubmittingFeedbackToYourself());
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

    public Either<ShowFeedbackFailure, Success> canBeReadBy(AuthenticatedUser authenticatedUser) {
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

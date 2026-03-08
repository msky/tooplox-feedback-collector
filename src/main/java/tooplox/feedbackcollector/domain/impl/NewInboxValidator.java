package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.IncorrectExpirationDate;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.MissingOwner;
import tooplox.shared.domain.AuthenticatedUserProvider;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class NewInboxValidator {
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Either<CreateInboxFailure, CreateInboxCommand> validate(CreateInboxCommand command) {
        if (thereIsNoAuthenticatedUser()) {
            return Either.left(new MissingOwner());
        } else if (expirationDateIsInPastOrMissing(command)) {
            return Either.left(new IncorrectExpirationDate());
        } else {
            return Either.right(command);
        }
    }

    private boolean thereIsNoAuthenticatedUser() {
        return authenticatedUserProvider.authenticatedUser() == null;
    }

    private boolean expirationDateIsInPastOrMissing(CreateInboxCommand command) {
        return command.expirationDate() == null || command.expirationDate().isBefore(LocalDateTime.now(clock));
    }

}

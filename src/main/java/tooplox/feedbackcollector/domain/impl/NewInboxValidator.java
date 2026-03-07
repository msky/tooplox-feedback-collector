package tooplox.feedbackcollector.domain.impl;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.IncorrectExpirationDate;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.MissingOwner;
import tooplox.feedbackcollector.domain.failures.CreateInboxFailure.TooLongOwnerUserName;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class NewInboxValidator {
    private final int maxOwnerUserNameLength;
    private final Clock clock;

    public Either<CreateInboxFailure, CreateInboxCommand> validate(CreateInboxCommand command) {
        if (ownerUserNameIsMissing(command)) {
            return Either.left(new MissingOwner());
        } else if (expirationDateIsInPastOrMissing(command)) {
            return Either.left(new IncorrectExpirationDate());
        } else if (ownerUserNameIsTooLong(command)) {
            return Either.left(new TooLongOwnerUserName());
        } else {
            return Either.right(command);
        }
    }

    private static boolean ownerUserNameIsMissing(CreateInboxCommand command) {
        return command.ownerUserName() == null || command.ownerUserName().isBlank();
    }

    private boolean expirationDateIsInPastOrMissing(CreateInboxCommand command) {
        return command.expirationDate() == null || command.expirationDate().isBefore(LocalDateTime.now(clock));
    }

    private boolean ownerUserNameIsTooLong(CreateInboxCommand command) {
        return command.ownerUserName().length() > maxOwnerUserNameLength;
    }
}

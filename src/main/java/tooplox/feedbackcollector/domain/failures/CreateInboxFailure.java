package tooplox.feedbackcollector.domain.failures;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract sealed class CreateInboxFailure {
    public abstract void log();

    public static final class IncorrectExpirationDate extends CreateInboxFailure {
        @Override
        public void log() {
            log.error("Failed to create inbox: incorrect expiration date - expiration date must be in the future");
        }
    }

    public static final class MissingOwner extends CreateInboxFailure {
        @Override
        public void log() {
            log.error("Failed to create inbox: missing owner - owner must be provided");
        }
    }
}

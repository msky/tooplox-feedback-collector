package tooplox.feedbackcollector.domain.failures;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract sealed class ShowInboxInformationFailure {

    public abstract void log();

    public static final class InboxNotFound extends ShowInboxInformationFailure {
        @Override
        public void log() {
            log.error("Failed to show inbox because the inbox is not found");
        }
    }

}

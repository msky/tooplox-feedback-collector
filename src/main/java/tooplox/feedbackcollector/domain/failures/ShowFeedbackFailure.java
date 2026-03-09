package tooplox.feedbackcollector.domain.failures;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract sealed class ShowFeedbackFailure {

    public abstract void log();


    public static final class InboxNotFound extends ShowFeedbackFailure {
        @Override
        public void log() {
            log.error("Failed to show feedback because the inbox is not found");
        }
    }

    public static final class NotAuthorizedToReadFromInbox extends ShowFeedbackFailure {

        @Override
        public void log() {
            log.error("Failed to read from inbox because the user is not authorized");
        }
    }

}


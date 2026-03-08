package tooplox.feedbackcollector.domain.failures;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract sealed class SubmitFeedbackFailure {
    public abstract void log();

    public static final class InboxExpired extends SubmitFeedbackFailure {
        @Override
        public void log() {
            log.warn("Failed to submit feedback because the inbox is expired");
        }
    }

    public static final class InboxNotFound extends SubmitFeedbackFailure {
        @Override
        public void log() {
            log.error("Failed to submit feedback because the inbox is not found");
        }
    }

    public static final class NoContent extends SubmitFeedbackFailure {
        @Override
        public void log() {
            log.warn("Failed to submit feedback because the content is missing");
        }
    }

    public static final class AnonymousFeedbackNotAllowed extends SubmitFeedbackFailure {
        @Override
        public void log() {
            log.warn("Failed to submit feedback because anonymous feedback is not allowed for this inbox");
        }
    }

    public static final class ContentTooLarge extends SubmitFeedbackFailure {
        @Override
        public void log() {
            log.warn("Failed to submit feedback because the content is too large");
        }
    }

    public static final class SubmittingFeedbackToYourself extends SubmitFeedbackFailure {
        @Override
        public void log() {
            log.warn("Failed to submit feedback because the author of the message is the owner of the inbox");
        }
    }
}


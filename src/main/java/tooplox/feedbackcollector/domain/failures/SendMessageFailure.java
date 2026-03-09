package tooplox.feedbackcollector.domain.failures;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract sealed class SendMessageFailure {
    public abstract void log();

    public static final class InboxExpired extends SendMessageFailure {
        @Override
        public void log() {
            log.warn("Failed to send message because the inbox is expired");
        }
    }

    public static final class InboxNotFound extends SendMessageFailure {
        @Override
        public void log() {
            log.error("Failed to send message because the inbox is not found");
        }
    }

    public static final class NoContent extends SendMessageFailure {
        @Override
        public void log() {
            log.warn("Failed to send message because the content is missing");
        }
    }

    public static final class AnonymousMessageNotAllowed extends SendMessageFailure {
        @Override
        public void log() {
            log.warn("Failed to send message because anonymous message is not allowed for this inbox");
        }
    }

    public static final class ContentTooLarge extends SendMessageFailure {
        @Override
        public void log() {
            log.warn("Failed to send message because the content is too large");
        }
    }

    public static final class SubmittingMessageToYourself extends SendMessageFailure {
        @Override
        public void log() {
            log.warn("Failed to send message because the author of the message is the owner of the inbox");
        }
    }
}


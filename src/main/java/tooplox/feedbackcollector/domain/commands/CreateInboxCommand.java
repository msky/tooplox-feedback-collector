package tooplox.feedbackcollector.domain.commands;

import java.time.LocalDateTime;

public record CreateInboxCommand(
        LocalDateTime expirationDate,
        boolean allowsAnonymousFeedback,
        String topic
) {
}

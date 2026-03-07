package tooplox.feedbackcollector.domain.commands;

import java.time.LocalDateTime;

public record CreateInboxCommand(
        String ownerUserName,
        LocalDateTime expirationDate,
        boolean allowsAnonymousFeedback
) {
}

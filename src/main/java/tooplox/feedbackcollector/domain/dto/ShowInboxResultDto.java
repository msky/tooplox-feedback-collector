package tooplox.feedbackcollector.domain.dto;

import tooplox.shared.domain.InboxId;

import java.time.LocalDateTime;

public record ShowInboxResultDto(
        InboxId id,
        String topic,
        String ownerSignature,
        LocalDateTime expiringOn) {
}


package tooplox.feedbackcollector.domain.dto;

import tooplox.shared.domain.MessageId;

import java.time.LocalDateTime;
import java.util.List;

public record ReadInboxResultDto(
        List<MessageDto> messages
) {
    public record MessageDto(MessageId id,
                             String content,
                             String authorSignature,
                             LocalDateTime submittedAt) {
    }
}


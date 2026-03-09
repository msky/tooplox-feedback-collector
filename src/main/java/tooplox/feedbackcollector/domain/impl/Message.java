package tooplox.feedbackcollector.domain.impl;

import tooplox.feedbackcollector.domain.dto.ShowFeedbackResultDto.MessageDto;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.MessageId;
import tooplox.shared.domain.UserName;

import java.time.LocalDateTime;

public record Message(
        MessageId id,
        InboxId inboxId,
        String content,
        Author author,
        LocalDateTime submittedAt
) {
    public MessageDto toDto() {
        return new MessageDto(
                id(),
                content(),
                author() == null ? null : author().name().value(),
                submittedAt()
        );
    }

    public record Author(UserName name) {
    }
}

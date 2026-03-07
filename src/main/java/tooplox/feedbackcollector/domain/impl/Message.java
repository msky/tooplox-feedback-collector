package tooplox.feedbackcollector.domain.impl;

import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.MessageId;

public record Message(
        MessageId id,
        InboxId inboxId,
        String content
        // TODO submitter
) {
}

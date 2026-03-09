package tooplox.feedbackcollector.domain.commands;

import tooplox.shared.domain.InboxId;

public record SendMessageCommand(
        InboxId inboxId,
        String content
) {
}


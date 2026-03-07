package tooplox.feedbackcollector.domain.commands;

import tooplox.shared.domain.InboxId;

public record SubmitFeedbackCommand(
        InboxId inboxId,
        String content,
        String submitterUserName
) {
    public boolean isMessageAnonymous() {
        return submitterUserName == null || submitterUserName.isBlank();
    }
}


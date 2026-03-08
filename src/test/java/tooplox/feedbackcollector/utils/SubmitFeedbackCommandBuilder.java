package tooplox.feedbackcollector.utils;

import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.shared.domain.InboxId;

public class SubmitFeedbackCommandBuilder {
    InboxId inboxId = null;
    String content = "sample content";

    public SubmitFeedbackCommand build() {
        return new SubmitFeedbackCommand(inboxId, content);
    }

    public static SubmitFeedbackCommandBuilder sampleSubmitFeedbackCommand() {
        return new SubmitFeedbackCommandBuilder();
    }

    public SubmitFeedbackCommandBuilder toInbox(InboxId inboxId) {
        this.inboxId = inboxId;
        return this;
    }

    public SubmitFeedbackCommandBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public SubmitFeedbackCommandBuilder withoutContent() {
        this.content = null;
        return this;
    }

    public SubmitFeedbackCommandBuilder withoutInbox() {
        this.inboxId = null;
        return this;
    }
}

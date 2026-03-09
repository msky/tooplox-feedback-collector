package tooplox.feedbackcollector.utils;

import tooplox.feedbackcollector.domain.commands.SendMessageCommand;
import tooplox.shared.domain.InboxId;

public class SendMessageCommandBuilder {
    InboxId inboxId = null;
    String content = "sample content";

    public SendMessageCommand build() {
        return new SendMessageCommand(inboxId, content);
    }

    public static SendMessageCommandBuilder sampleSendMessageCommand() {
        return new SendMessageCommandBuilder();
    }

    public SendMessageCommandBuilder toInbox(InboxId inboxId) {
        this.inboxId = inboxId;
        return this;
    }

    public SendMessageCommandBuilder withContent(String content) {
        this.content = content;
        return this;
    }

    public SendMessageCommandBuilder withoutContent() {
        this.content = null;
        return this;
    }

    public SendMessageCommandBuilder withoutInbox() {
        this.inboxId = null;
        return this;
    }
}

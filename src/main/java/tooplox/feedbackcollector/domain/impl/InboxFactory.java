package tooplox.feedbackcollector.domain.impl;

import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.shared.domain.InboxId;


public class InboxFactory {
    public Inbox createFrom(CreateInboxCommand command) {
        return new Inbox(InboxId.generate());
    }
}

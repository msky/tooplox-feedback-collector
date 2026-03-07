package tooplox.feedbackcollector.domain.impl;

import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.shared.domain.MessageId;

public class MessageFactory {

    public Message createFrom(SubmitFeedbackCommand command) {
        return new Message(
                MessageId.generate(),
                command.inboxId(),
                command.content()
        );
    }
}

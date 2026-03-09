package tooplox.feedbackcollector.domain.impl;

import tooplox.shared.domain.InboxId;

import java.util.List;

public interface MessageRepository {
    Message save(Message message);

    List<Message> findBy(InboxId inboxId);
}

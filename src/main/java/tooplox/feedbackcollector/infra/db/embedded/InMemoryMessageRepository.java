package tooplox.feedbackcollector.infra.db.embedded;

import org.springframework.stereotype.Repository;
import tooplox.feedbackcollector.domain.impl.Message;
import tooplox.feedbackcollector.domain.impl.MessageRepository;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.MessageId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryMessageRepository implements MessageRepository {
    private final Map<MessageId, Message> messages = new HashMap<>();

    @Override
    public Message save(Message message) {
        messages.put(message.id(), message);
        return message;
    }

    @Override
    public List<Message> findBy(InboxId inboxId) {
        return messages.values().stream()
                .filter(m -> m.inboxId().equals(inboxId))
                .toList();
    }
}

package tooplox.feedbackcollector.stubs;

import tooplox.feedbackcollector.domain.impl.Message;
import tooplox.feedbackcollector.domain.impl.MessageRepository;
import tooplox.shared.domain.MessageId;

import java.util.HashMap;
import java.util.Map;

public class InMemoryMessageRepository implements MessageRepository {
    private final Map<MessageId, Message> messages = new HashMap<>();

    @Override
    public Message save(Message message) {
        messages.put(message.id(), message);
        return message;
    }
}

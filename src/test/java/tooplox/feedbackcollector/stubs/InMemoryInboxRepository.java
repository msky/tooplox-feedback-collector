package tooplox.feedbackcollector.stubs;

import tooplox.feedbackcollector.domain.impl.Inbox;
import tooplox.feedbackcollector.domain.impl.InboxRepository;
import tooplox.shared.domain.InboxId;

import java.util.HashMap;
import java.util.Map;

public class InMemoryInboxRepository implements InboxRepository {
    private final Map<InboxId, Inbox> inboxes = new HashMap<>();

    @Override
    public Inbox save(Inbox inbox) {
        inboxes.put(inbox.id(), inbox);
        return inbox;
    }
}

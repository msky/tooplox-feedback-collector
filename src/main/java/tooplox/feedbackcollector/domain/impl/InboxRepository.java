package tooplox.feedbackcollector.domain.impl;

import tooplox.shared.domain.InboxId;

import java.util.Optional;

public interface InboxRepository {
    Inbox save(Inbox inbox);

    Optional<Inbox> findBy(InboxId inboxId);
}

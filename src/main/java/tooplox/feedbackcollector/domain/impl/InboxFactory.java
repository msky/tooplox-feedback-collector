package tooplox.feedbackcollector.domain.impl;

import lombok.RequiredArgsConstructor;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.impl.Inbox.Owner;
import tooplox.shared.domain.AuthenticatedUser;
import tooplox.shared.domain.AuthenticatedUserProvider;
import tooplox.shared.domain.InboxId;


@RequiredArgsConstructor
public class InboxFactory {
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Inbox createFrom(CreateInboxCommand command) {
        AuthenticatedUser owner = authenticatedUserProvider.authenticatedUser();
        return new Inbox(
                InboxId.generate(),
                command.expirationDate(),
                command.allowsAnonymousFeedback(),
                new Owner(owner.userName())
        );
    }
}

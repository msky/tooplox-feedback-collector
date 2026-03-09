package tooplox.feedbackcollector.domain.impl;

import lombok.RequiredArgsConstructor;
import tooplox.feedbackcollector.domain.commands.CreateInboxCommand;
import tooplox.feedbackcollector.domain.impl.Inbox.Owner;
import tooplox.shared.authentication.AuthenticatedUser;
import tooplox.shared.authentication.AuthenticatedUserProvider;
import tooplox.shared.domain.InboxId;
import tooplox.shared.domain.UserSignature;


@RequiredArgsConstructor
public class InboxFactory {
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Inbox createFrom(CreateInboxCommand command) {
        AuthenticatedUser owner = authenticatedUserProvider.authenticatedUser();
        return new Inbox(
                InboxId.generate(),
                new Topic(command.topic()),
                command.expirationDate(),
                command.allowsAnonymousFeedback(),
                new Owner(UserSignature.from(owner.userName(), owner.signatureHash()))
        );
    }
}

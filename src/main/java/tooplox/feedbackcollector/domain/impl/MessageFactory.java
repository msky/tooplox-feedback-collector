package tooplox.feedbackcollector.domain.impl;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.val;
import tooplox.feedbackcollector.domain.commands.SubmitFeedbackCommand;
import tooplox.feedbackcollector.domain.impl.Message.Author;
import tooplox.shared.domain.AuthenticatedUserProvider;
import tooplox.shared.domain.MessageId;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class MessageFactory {
    private final Clock clock;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public Message createFrom(SubmitFeedbackCommand command) {
        return new Message(
                MessageId.generate(),
                command.inboxId(),
                command.content(),
                getAuthor(),
                LocalDateTime.now(clock)
        );
    }

    private @Nullable Author getAuthor() {
        val user = authenticatedUserProvider.authenticatedUser();
        return user == null ? null : new Author(user.signature());
    }
}

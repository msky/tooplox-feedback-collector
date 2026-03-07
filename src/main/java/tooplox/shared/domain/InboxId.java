package tooplox.shared.domain;

import static java.util.UUID.randomUUID;

public record InboxId(String value) {
    public static InboxId generate() {
        return new InboxId(randomUUID().toString());
    }
}

package tooplox.shared.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import static java.util.UUID.randomUUID;

public record InboxId(@JsonValue String value) {
    public static InboxId generate() {
        return new InboxId(randomUUID().toString());
    }
}

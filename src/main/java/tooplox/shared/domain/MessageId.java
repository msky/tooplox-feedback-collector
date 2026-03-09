package tooplox.shared.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import static java.util.UUID.randomUUID;

public record MessageId(@JsonValue String value) {
    public static MessageId generate() {
        return new MessageId(randomUUID().toString());
    }
}

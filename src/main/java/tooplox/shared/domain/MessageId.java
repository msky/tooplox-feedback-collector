package tooplox.shared.domain;

import static java.util.UUID.randomUUID;

public record MessageId(String value) {
    public static MessageId generate() {
        return new MessageId(randomUUID().toString());
    }
}

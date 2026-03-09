package tooplox.feedbackcollector.utils;

import tooplox.shared.domain.AuthenticatedUser;
import tooplox.shared.domain.SignatureHash;
import tooplox.shared.domain.UserName;

public class AuthenticatedUserBuilder {
    private String name;
    private String hash = "dummyHash";

    public static AuthenticatedUserBuilder authenticatedUser() {
        return new AuthenticatedUserBuilder();
    }

    public AuthenticatedUserBuilder withSignature(String signature) {
        assert signature != null && !signature.isEmpty() : "Signature must not be null or empty and contain # as separator";
        this.name = signature.split("#")[0];
        this.hash = signature.split("#")[1];

        return this;
    }

    public AuthenticatedUserBuilder withName(String name) {
        assert name != null && !name.isEmpty() : "Name must not be null or empty";
        this.name = name;
        return this;
    }

    public AuthenticatedUser build() {
        assert name != null && !name.isEmpty() : "Name must not be null or empty";
        assert hash != null && !hash.isEmpty() : "Hash must not be null or empty";
        return new AuthenticatedUser(new UserName(name), new SignatureHash(hash));
    }
}

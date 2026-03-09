package tooplox.shared.domain;

public record UserSignature(UserName userName, SignatureHash signatureHash) {
    public static final String SEPARATOR = "#";

    public static UserSignature from(UserName userName, SignatureHash signatureHash) {
        return new UserSignature(userName, signatureHash);
    }

    public String value() {
        return userName.value() + SEPARATOR + signatureHash.value();
    }
}

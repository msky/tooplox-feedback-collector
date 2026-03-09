package tooplox.shared.domain;

public record AuthenticatedUser(UserName userName, SignatureHash signatureHash) {

    public UserSignature signature() {
        return UserSignature.from(userName, signatureHash);
    }
}

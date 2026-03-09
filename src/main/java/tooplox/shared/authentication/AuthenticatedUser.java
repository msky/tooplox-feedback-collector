package tooplox.shared.authentication;

import tooplox.shared.domain.SignatureHash;
import tooplox.shared.domain.UserName;
import tooplox.shared.domain.UserSignature;

public record AuthenticatedUser(UserName userName, SignatureHash signatureHash) {

    public UserSignature signature() {
        return UserSignature.from(userName, signatureHash);
    }
}

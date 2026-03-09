package tooplox.shared.authentication;

import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;
import tooplox.shared.domain.SignatureHash;

@Component
@RequiredArgsConstructor
public class SignatureHashCalculator {
    private final UserSaltRepository userSaltRepository;

    public SignatureHash calculateSignatureHash(String userName, String password) {
        return new SignatureHash(
                new DigestUtils("SHA3-256")
                        .digestAsHex(userName + password + userSaltRepository.getOrCreateSalt(userName, password))
        );
    }


}

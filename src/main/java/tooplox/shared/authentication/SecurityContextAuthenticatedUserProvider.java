package tooplox.shared.authentication;

import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tooplox.shared.domain.UserName;

@Component
@RequiredArgsConstructor
public class SecurityContextAuthenticatedUserProvider implements AuthenticatedUserProvider {
    private final SignatureHashCalculator signatureHashCalculator;

    @Override
    public AuthenticatedUser authenticatedUser() {
        return Option.of(SecurityContextHolder.getContext().getAuthentication())
                .map(authentication -> new AuthenticatedUser(
                        new UserName((String) authentication.getPrincipal()),
                        signatureHashCalculator.calculateSignatureHash(
                                (String) authentication.getPrincipal(),
                                (String) authentication.getCredentials())
                )).getOrNull();
    }
}

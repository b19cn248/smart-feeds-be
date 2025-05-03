package com.olh.feeds.api.configuration.auditor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;
import java.util.Optional;

import static com.olh.feeds.core.exception.constanst.ExceptionConstants.CommonConstants.ANONYMOUS;
import static com.olh.feeds.core.exception.constanst.ExceptionConstants.CommonConstants.SYSTEM;

@Slf4j
public class AuditorAwareImpl implements AuditorAware<String> {

    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String SUB = "sub";

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(authentication)) {
            return Optional.of(SYSTEM);
        }

        log.info("principal: {}", authentication.getPrincipal());

        if (!this.isAnonymous() && (Objects.nonNull(authentication.getPrincipal()))) {
            if (authentication.getPrincipal() instanceof Jwt jwt) {
                // Trả về preferred_username nếu có, nếu không thì trả về subject của JWT
                String username = jwt.getClaim(PREFERRED_USERNAME);
                if (username == null) {
                    username = jwt.getClaim(SUB);
                }
                return Optional.of(username != null ? username : SYSTEM);
            }

            // Trường hợp không phải JWT, lấy thông tin từ getName()
            return Optional.of(authentication.getName());
        }

        return Optional.of(SYSTEM);
    }

    private boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)) {
            return true;
        }
        return authentication.getName().equals(ANONYMOUS);
    }
}
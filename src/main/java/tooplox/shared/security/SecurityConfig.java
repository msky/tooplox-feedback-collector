package tooplox.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static java.util.Collections.emptyList;
import static org.springframework.security.core.context.SecurityContextHolder.MODE_INHERITABLETHREADLOCAL;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static {
        SecurityContextHolder.setStrategyName(MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http.authorizeHttpRequests
                        (
                                auth -> auth.anyRequest().permitAll()
                        )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .addFilterAfter(new BasicAuthFilter(), BasicAuthenticationFilter.class)
                .build();
    }


    static class BasicAuthFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            val authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
                val base64Credentials = authorizationHeader.substring("Basic ".length());
                val credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials)).split(":");
                if (credentials.length == 2) {
                    val userName = credentials[0];
                    val password = credentials[1];
                    SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userName, password, emptyList()));
                }
            }

            filterChain.doFilter(request, response);

        }
    }
}

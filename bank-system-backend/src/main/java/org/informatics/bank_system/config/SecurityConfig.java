package org.informatics.bank_system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.informatics.bank_system.exception.ErrorResponse;
import org.informatics.bank_system.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper, UserService userService)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/login", "/api/logout").permitAll()
                        .requestMatchers("/api/session").authenticated()
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/dashboard").hasAuthority("ADMIN")
                        .requestMatchers("/api/clients/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/accounts").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/accounts/*/close").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/credits").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginProcessingUrl("/api/login")
                        .successHandler((request, response, authentication) ->
                                writeJson(response, objectMapper, HttpServletResponse.SC_OK,
                                        userService.getCurrentSession()))
                        .failureHandler((request, response, exception) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Invalid username or password."))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, exception) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Authentication is required."))
                        .accessDeniedHandler((request, response, exception) ->
                                writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                        "Access is denied."))
                );

        return http.build();
    }

    private void writeError(HttpServletResponse response, ObjectMapper objectMapper, int status, String message)
            throws IOException {
        writeJson(response, objectMapper, status, new ErrorResponse(LocalDateTime.now(), status, List.of(message)));
    }

    private void writeJson(HttpServletResponse response, ObjectMapper objectMapper, int status, Object body)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}

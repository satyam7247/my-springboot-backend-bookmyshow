package com.BookMyShow.bookmyshow.config;

import com.BookMyShow.bookmyshow.Service.AuthSessionService;
import com.BookMyShow.bookmyshow.security.BearerTokenFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BearerTokenFilter bearerTokenFilter(AuthSessionService authSessionService) {
        return new BearerTokenFilter(authSessionService);
    }

    @Bean
    public FilterRegistrationBean<Filter> disableContainerFilterRegistration(BearerTokenFilter bearerTokenFilter) {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(bearerTokenFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                writeSecurityError(response, 401, "Authentication required"))
                        .accessDeniedHandler((request, response, exception) ->
                                writeSecurityError(response, 403, "Access denied")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/cities/**", "/api/movies/**", "/api/theaters/**",
                                "/api/screens/**", "/api/seats/**", "/api/shows/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/cities/**", "/api/movies/**", "/api/theaters/**",
                                "/api/screens/**", "/api/seats/**", "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/movies/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/movies/**", "/api/shows/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/users/**", "/api/bookings/**", "/api/payment/**", "/api/payments/**")
                        .authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeSecurityError(jakarta.servlet.http.HttpServletResponse response,
                                           int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}

package com.qdc.lims.config;

import com.qdc.lims.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((requests) -> requests
                        // 1. Public Pages (Setup, Static Files)
                        .requestMatchers("/setup", "/login", "/css/**", "/js/**", "/webfonts/**", "/images/**")
                        .permitAll()

                        // 2. Role Based Access
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/lab/**").hasAnyRole("LAB", "ADMIN")
                        .requestMatchers("/reception/**").hasAnyRole("RECEPTION", "ADMIN")

                        // 3. API Access (Booking)
                        .requestMatchers("/api/**").authenticated()

                        // 4. Everything else requires login
                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout((logout) -> logout.permitAll())

                // --- FIX IS HERE ---
                .csrf(csrf -> csrf.disable());
        // -------------------

        return http.build();
    }

    // Password Encryption Service
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Connect Spring Security to our Database
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> {
            com.qdc.lims.entity.User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole().replace("ROLE_", "")) // Spring wants "ADMIN", DB has "ROLE_ADMIN"
                    .build();
        };
    }
}
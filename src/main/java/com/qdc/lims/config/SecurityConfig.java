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

    /**
     * Configures the security filter chain for HTTP requests, specifying access rules,
     * login/logout behavior, exception handling, and CSRF settings.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // 1. PUBLIC ACCESS: 
                // We allow "/" (The Cards), "/setup" (First Run), and all static assets (CSS/JS)
                .requestMatchers("/", "/index", "/setup", "/login", "/css/**", "/js/**", "/webfonts/**", "/images/**").permitAll()
                
                // 2. RESTRICTED ACCESS (The Doors):
                // If you click these links, Spring will stop you and ask for a password
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/lab/**").hasAnyRole("LAB", "ADMIN")
                .requestMatchers("/reception/**").hasAnyRole("RECEPTION", "ADMIN")
                .requestMatchers("/api/**").authenticated()
                
                // 3. Catch-all: Anything else requires login
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/login") // Custom Login Page
                // --- FIX 1: Handle Wrong Password ---
                // If login fails, go back to /login?error=true
                .failureUrl("/login?error=true") 

                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/") // <--- Go back to Cards after logout
                .permitAll()
            )
            // --- FIX 2: Handle Wrong Role (403) ---
                .exceptionHandling((ex) -> ex
                .accessDeniedPage("/access-denied") 
            )
        
            .csrf(csrf -> csrf.disable()); // Standard for non-browser clients or simple forms

        return http.build();
    }

    /**
     * Provides a password encoder bean using BCrypt hashing algorithm.
     *
     * @return a PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Connects Spring Security to the application's database by providing a custom
     * UserDetailsService implementation that loads user details from the UserRepository.
     *
     * @param userRepo the UserRepository to fetch user data
     * @return a UserDetailsService instance
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepo) {
        return username -> {
            com.qdc.lims.entity.User user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    String[] roles = user.getRole().split(",");
                                // Clean up "ROLE_" prefix because builder.roles() adds it automatically
            for (int i = 0; i < roles.length; i++) {
                roles[i] = roles[i].trim().replace("ROLE_", "");
            }


            return User.withUsername(user.getUsername())
                    .password(user.getPassword())
                    .roles(roles) 
                    .build();
        };
    }
}
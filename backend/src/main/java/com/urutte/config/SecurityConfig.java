package com.urutte.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {
    
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;
    
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login**", "/oauth2/**", "/error", "/api/**", "/ws/**").permitAll()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google")
                .successHandler(oAuth2LoginSuccessHandler)
                .failureUrl("http://localhost/login?error=true")
            )
            .logout(logout -> logout
                .logoutSuccessUrl("http://localhost/login")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
            );
        return http.build();
    }
}

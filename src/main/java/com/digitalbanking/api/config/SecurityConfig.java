package com.digitalbanking.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança (HTTP Basic + usuários em memória) para fins de
 * demonstração. Em produção, substituir por um provedor de identidade real
 * (OAuth2/OIDC, JWT) e remover as credenciais padrão.
 *
 * <p>Regras: documentação OpenAPI é pública; operações de escrita exigem o papel
 * {@code ADMIN}; demais requisições exigem autenticação. As credenciais são
 * configuráveis por variáveis de ambiente (ver {@code .env.example}).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/clientes",
                                "/contas",
                                "/transferencias",
                                "/contas/*/depositos",
                                "/contas/*/saques")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder,
            @Value("${app.security.admin.username}") String adminUsername,
            @Value("${app.security.admin.password}") String adminPassword,
            @Value("${app.security.client.username}") String clientUsername,
            @Value("${app.security.client.password}") String clientPassword) {
        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        UserDetails cliente = User.withUsername(clientUsername)
                .password(passwordEncoder.encode(clientPassword))
                .roles("CLIENTE")
                .build();
        return new InMemoryUserDetailsManager(admin, cliente);
    }

}

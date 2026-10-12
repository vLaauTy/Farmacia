package Farmacia.Farmacia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import Farmacia.Farmacia.Service.CustomUserDetailsService;
import Farmacia.Farmacia.Repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class UserConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                        .requestMatchers("/pacientes/**").hasAnyRole("ADMIN", "MEDICO")
                        .requestMatchers("/mis-turnos").hasRole("MEDICO")
                        .requestMatchers("/medicos/**", "/turnos/**", "/usuarios", "/usuarios/**",
                            "/correo-prueba")
                        .hasRole("ADMIN")
                        .requestMatchers("/").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/", true)
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .csrf(c -> c.disable())
                .exceptionHandling(e -> e
                        .accessDeniedHandler(customAccessDeniedHandler()));
        return http.build();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Acceso denegado: no tienes permisos para este recurso.\"}");
        };
    }
}

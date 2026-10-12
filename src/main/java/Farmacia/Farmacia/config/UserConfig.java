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

import Farmacia.Farmacia.Repository.UserRepository;
import Farmacia.Farmacia.Service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class UserConfig {

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    // Las contraseñas se guardan sin encriptar
    @Bean
    @SuppressWarnings("deprecation")
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    // Permisos por rol. Las reglas se evalúan en orden: la primera que coincide gana.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/pacientes/nuevo", "/pacientes/guardar", "/pacientes/eliminar/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/pacientes/**").hasAnyRole("ADMIN", "MEDICO")
                        .requestMatchers("/mis-turnos").hasRole("MEDICO")
                        .requestMatchers("/medicos/**", "/turnos/**", "/calendario", "/usuarios/**",
                                "/correo-prueba")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true"))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(e -> e.accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\": \"Acceso denegado: no tienes permisos para este recurso.\"}");
        };
    }
}

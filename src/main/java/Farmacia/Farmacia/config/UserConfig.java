package Farmacia.Farmacia.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import Farmacia.Farmacia.Service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class UserConfig {

    @Autowired
    private UserService userService;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll() // Recursos
                                                                                                                 // estáticos
                        .requestMatchers("/pacientes/**").hasAnyRole("ADMIN", "MEDICO") // Admin y médico pueden acceder
                                                                                        // a pacientes
                        .requestMatchers("/medicos/**", "/turnos/**", "/usuarios/**").hasRole("ADMIN") // Solo admin
                        .requestMatchers("/").authenticated() // Página principal para todos los autenticados
                        .anyRequest().authenticated()) // Requiere autenticación para todas las demás rutas
                .formLogin(form -> form
                        .loginPage("/login") // Página personalizada de inicio de sesión
                        .defaultSuccessUrl("/", true) // Redirigir a la página principal después del login
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout") // URL para cerrar sesión
                        .logoutSuccessUrl("/login?logout") // Redirige al login después del logout
                        .invalidateHttpSession(true) // Invalida la sesión
                        .deleteCookies("JSESSIONID")) // Borra las cookies de sesión
                .csrf(c -> c.disable())
                .exceptionHandling(e -> e
                        .accessDeniedHandler(customAccessDeniedHandler())); // Manejo de acceso denegado
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

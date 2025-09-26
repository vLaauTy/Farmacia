package Farmacia.Farmacia.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import Farmacia.Farmacia.Model.UserModel;
import Farmacia.Farmacia.Repository.UserRepository;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {
        @Autowired
        private UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                System.out.println("UserService - Intentando autenticar usuario: " + username);

                try {
                        // Usuarios de prueba temporales para testing (remover en producción)
                        if ("admin".equals(username)) {
                                return new User("admin", "admin",
                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
                        }
                        if ("medico".equals(username)) {
                                return new User("medico", "medico",
                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEDICO")));
                        }

                        UserModel usuario = userRepository.findByUsername(username)
                                        .orElseThrow(() -> new UsernameNotFoundException(
                                                        "Usuario no encontrado: " + username));

                        String roleFinal = usuario.getRol().toUpperCase().startsWith("ROLE_")
                                        ? usuario.getRol().toUpperCase()
                                        : "ROLE_" + usuario.getRol().toUpperCase();

                        System.out.println("UserService - Usuario encontrado: " + username + " con rol: " + roleFinal);
                        return new User(
                                        usuario.getUsername(),
                                        usuario.getPassword(),
                                        Collections.singletonList(new SimpleGrantedAuthority(roleFinal)));

                } catch (Exception e) {
                        System.err.println("UserService - Error de base de datos para usuario: " + username + " - "
                                        + e.getMessage());
                        throw new UsernameNotFoundException("Error de autenticación para usuario: " + username);
                }
        }
}

package Farmacia.Farmacia.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Farmacia.Farmacia.Model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    
    Optional<UserModel> findByUsername(String username);
    
    boolean existsByUsername(String username);
}

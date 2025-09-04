package Farmacia.Farmacia.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Farmacia.Farmacia.Model.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUsername(String username);
}

package com.textilcircular.usuarios.repository;

import com.textilcircular.usuarios.model.Usuario;
import org.springframework.data.repository.Repository;
import java.util.Optional;

public interface UsuarioRepository extends Repository<Usuario, Long> {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}

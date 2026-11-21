package com.textilcircular.usuarios.repository;

import com.textilcircular.usuarios.model.Emprendedora;
import org.springframework.data.repository.Repository;
import java.util.Optional;

public interface EmprendedoraRepository extends Repository<Emprendedora, Long> {
    Emprendedora save(Emprendedora emprendedora);
    Optional<Emprendedora> findById(Long id);
    Optional<Emprendedora> findByUsuarioId(Long usuarioId);
    boolean existsByRut(String rut);
}

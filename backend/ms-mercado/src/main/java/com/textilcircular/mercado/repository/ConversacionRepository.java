package com.textilcircular.mercado.repository;

import com.textilcircular.mercado.model.Conversacion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConversacionRepository extends Repository<Conversacion, Long> {
    long count();
    Conversacion save(Conversacion conversacion);
    Optional<Conversacion> findById(Long id);
    Optional<Conversacion> findByClienteIdAndEmprendedoraId(Long clienteId, Long emprendedoraId);

    @Query("""
            select c from Conversacion c
            where c.clienteId = :usuarioId or c.emprendedoraId = :usuarioId
            order by c.fechaHora desc, c.id desc
            """)
    List<Conversacion> listarPorUsuario(@Param("usuarioId") Long usuarioId);
}

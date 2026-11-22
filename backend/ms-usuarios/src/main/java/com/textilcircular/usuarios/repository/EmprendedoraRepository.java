package com.textilcircular.usuarios.repository;

import com.textilcircular.usuarios.model.Emprendedora;
import com.textilcircular.usuarios.model.EstadoFichaEmprendimiento;
import com.textilcircular.usuarios.model.Rubro;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.repository.Repository;
import java.util.Optional;

public interface EmprendedoraRepository extends Repository<Emprendedora, Long> {
    long count();
    Emprendedora save(Emprendedora emprendedora);
    Optional<Emprendedora> findById(Long id);
    Optional<Emprendedora> findByUsuarioId(Long usuarioId);
    boolean existsByRut(String rut);

    @Query("""
            select e from Emprendedora e
            where e.activa = true and e.usuario.fechaEliminacion is null
              and (:estado is null or e.estadoFicha = :estado)
            order by e.id
            """)
    List<Emprendedora> listarParaAdmin(@Param("estado") EstadoFichaEmprendimiento estado);

    @Query("""
            select e from Emprendedora e
            where e.estadoFicha = :estado and e.activa = true
              and e.usuario.fechaBaja is null and e.usuario.fechaEliminacion is null
              and (:texto is null or locate(lower(:texto), lower(e.nombreComercial)) > 0)
              and (:region is null or e.region = :region)
              and (:comuna is null or e.comuna = :comuna)
              and (:rubro is null or :rubro member of e.rubros)
            order by e.id
            """)
    List<Emprendedora> buscarActivas(
            @Param("estado") EstadoFichaEmprendimiento estado,
            @Param("texto") String texto,
            @Param("region") String region,
            @Param("comuna") String comuna,
            @Param("rubro") Rubro rubro);
}

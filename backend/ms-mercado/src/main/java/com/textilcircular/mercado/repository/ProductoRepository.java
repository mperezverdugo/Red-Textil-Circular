package com.textilcircular.mercado.repository;

import com.textilcircular.mercado.model.Producto;
import org.springframework.data.repository.Repository;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends Repository<Producto, Long> {
    long count();
    Producto save(Producto producto);
    Optional<Producto> findById(Long id);
    List<Producto> findByEmprendedoraIdOrderByIdAsc(Long emprendedoraId);
    void delete(Producto producto);
}

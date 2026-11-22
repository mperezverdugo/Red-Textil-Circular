package com.textilcircular.mercado.repository;

import com.textilcircular.mercado.model.Mensaje;
import com.textilcircular.mercado.model.TipoEmisor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends Repository<Mensaje, Long> {
    long count();
    Mensaje save(Mensaje mensaje);
    List<Mensaje> findByConversacionIdOrderByIdAsc(Long conversacionId);
    List<Mensaje> findByConversacionIdAndIdGreaterThanOrderByIdAsc(Long conversacionId, Long id);

    @Modifying(clearAutomatically = true)
    @Query("""
            update Mensaje m set m.esLeido = true
            where m.conversacionId = :conversacionId
              and m.emisor = :emisor
              and m.esLeido = false
            """)
    int marcarLeidosPorEmisor(@Param("conversacionId") Long conversacionId,
                              @Param("emisor") TipoEmisor emisor);
}

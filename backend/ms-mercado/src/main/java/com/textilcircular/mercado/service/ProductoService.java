package com.textilcircular.mercado.service;

import com.textilcircular.mercado.dto.CrearProductoRequest;
import com.textilcircular.mercado.dto.EditarProductoRequest;
import com.textilcircular.mercado.dto.ProductoResponse;
import com.textilcircular.mercado.exception.ApiException;
import com.textilcircular.mercado.model.Producto;
import com.textilcircular.mercado.model.Rol;
import com.textilcircular.mercado.repository.ProductoRepository;
import com.textilcircular.mercado.security.TokenService.DatosToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional
    public ProductoResponse crear(DatosToken usuario, CrearProductoRequest solicitud) {
        if (usuario.rol() != Rol.EMPRENDEDORA) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "Solo las emprendedoras pueden publicar productos");
        }
        String proceso = solicitud.explicacionProceso() == null ? null : solicitud.explicacionProceso().strip();
        if (proceso != null && proceso.isBlank()) {
            proceso = null;
        }
        Producto producto = new Producto(
                usuario.usuarioId(),
                solicitud.nombre().strip(),
                solicitud.descripcionCorta().strip(),
                proceso
        );
        return ProductoResponse.desde(productos.save(producto));
    }

    @Transactional
    public ProductoResponse editar(Long id, DatosToken usuario, EditarProductoRequest solicitud) {
        Producto producto = buscar(id);
        comprobarPropiedad(producto, usuario);
        String proceso = solicitud.explicacionProceso() == null ? null : solicitud.explicacionProceso().strip();
        if (proceso != null && proceso.isBlank()) {
            proceso = null;
        }
        producto.actualizar(
                solicitud.nombre().strip(),
                solicitud.descripcionCorta().strip(),
                proceso
        );
        return ProductoResponse.desde(producto);
    }

    @Transactional
    public void eliminar(Long id, DatosToken usuario) {
        Producto producto = buscar(id);
        comprobarPropiedad(producto, usuario);
        productos.delete(producto);
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtener(Long id) {
        return ProductoResponse.desde(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPorEmprendedora(Long emprendedoraId) {
        return productos.findByEmprendedoraIdOrderByIdAsc(emprendedoraId)
                .stream().map(ProductoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarMios(DatosToken usuario) {
        if (usuario.rol() != Rol.EMPRENDEDORA) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "Solo las emprendedoras pueden consultar sus productos");
        }
        return listarPorEmprendedora(usuario.usuarioId());
    }

    private Producto buscar(Long id) {
        return productos.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "PRODUCTO_NO_ENCONTRADO", "El producto no existe"));
    }

    private void comprobarPropiedad(Producto producto, DatosToken usuario) {
        if (usuario.rol() != Rol.EMPRENDEDORA || !producto.getEmprendedoraId().equals(usuario.usuarioId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                    "No tienes permisos para modificar este producto");
        }
    }
}

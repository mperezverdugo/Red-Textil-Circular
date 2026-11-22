package com.textilcircular.mercado.controller;

import com.textilcircular.mercado.dto.CrearProductoRequest;
import com.textilcircular.mercado.dto.EditarProductoRequest;
import com.textilcircular.mercado.dto.ProductoResponse;
import com.textilcircular.mercado.security.AutenticacionFilter;
import com.textilcircular.mercado.security.TokenService.DatosToken;
import com.textilcircular.mercado.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody CrearProductoRequest solicitud,
                                  HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return productos.crear(usuario, solicitud);
    }

    @PutMapping("/{id}")
    public ProductoResponse editar(@PathVariable Long id,
                                   @Valid @RequestBody EditarProductoRequest solicitud,
                                   HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return productos.editar(id, usuario, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id, HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        productos.eliminar(id, usuario);
    }

    @GetMapping
    public List<ProductoResponse> listarPorEmprendedora(@RequestParam Long emprendedoraId) {
        return productos.listarPorEmprendedora(emprendedoraId);
    }

    @GetMapping("/mios")
    public List<ProductoResponse> listarMios(HttpServletRequest request) {
        DatosToken usuario = (DatosToken) request.getAttribute(AutenticacionFilter.USUARIO_AUTENTICADO);
        return productos.listarMios(usuario);
    }

    @GetMapping("/{id}")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productos.obtener(id);
    }
}

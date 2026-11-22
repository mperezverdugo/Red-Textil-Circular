package com.textilcircular.usuarios.controller;

import com.textilcircular.usuarios.dto.EmprendedoraPublicaResponse;
import com.textilcircular.usuarios.service.EmprendedoraService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/emprendedoras")
public class EmprendedoraController {
    private final EmprendedoraService emprendedoras;

    public EmprendedoraController(EmprendedoraService emprendedoras) {
        this.emprendedoras = emprendedoras;
    }

    @GetMapping
    public List<EmprendedoraPublicaResponse> listar(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String comuna,
            @RequestParam(required = false) String rubro) {
        return emprendedoras.listar(texto, region, comuna, rubro);
    }

    @GetMapping("/{id}")
    public EmprendedoraPublicaResponse obtener(@PathVariable Long id) {
        return emprendedoras.obtener(id);
    }
}

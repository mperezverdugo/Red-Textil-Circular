package com.textilcircular.usuarios.controller;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.service.RevisionFichaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/emprendedoras")
public class RevisionFichaController {
    private final RevisionFichaService revisiones;

    public RevisionFichaController(RevisionFichaService revisiones) {
        this.revisiones = revisiones;
    }

    @PatchMapping("/{id}/revision")
    public RevisionFichaResponse revisar(@PathVariable Long id,
                                         @Valid @RequestBody RevisionFichaRequest solicitud) {
        return revisiones.revisar(id, solicitud);
    }
}

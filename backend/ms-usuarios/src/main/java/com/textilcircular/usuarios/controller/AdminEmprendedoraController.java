package com.textilcircular.usuarios.controller;

import com.textilcircular.usuarios.dto.*;
import com.textilcircular.usuarios.service.AdminEmprendedoraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/emprendedoras")
public class AdminEmprendedoraController {
    private final AdminEmprendedoraService adminService;

    public AdminEmprendedoraController(AdminEmprendedoraService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<EmprendedoraAdminResponse> listar(@RequestParam(required = false) String estado) {
        return adminService.listar(estado);
    }

    @GetMapping("/{id}")
    public EmprendedoraAdminResponse obtener(@PathVariable Long id) {
        return adminService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmprendedoraAdminResponse crear(@Valid @RequestBody RegistroEmprendedoraRequest solicitud) {
        return adminService.crear(solicitud);
    }

    @PutMapping("/{id}")
    public EmprendedoraAdminResponse editar(@PathVariable Long id,
                                            @Valid @RequestBody EditarFichaAdminRequest solicitud) {
        return adminService.editar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        adminService.eliminar(id);
    }
}

package com.textilcircular.usuarios.dto;

import com.textilcircular.usuarios.model.Rol;
import com.textilcircular.usuarios.model.Usuario;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol) {
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}

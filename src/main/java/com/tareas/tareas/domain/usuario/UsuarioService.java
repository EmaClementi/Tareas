package com.tareas.tareas.domain.usuario;


import com.tareas.tareas.Validacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public List<DatosRespuestaUsuario> listarUsuarios() {
        List<DatosRespuestaUsuario> usuarios = usuarioRepository.findAll().stream()
                .map(t->new DatosRespuestaUsuario(t.getNombre(),t.getEmail()))
                .toList();

        return usuarios;

    }

    public DatosRespuestaUsuario crearUsuario(DatosCrearUsuario datos) {

        var usuario = usuarioRepository.findByEmail(datos.email());
        if(usuario.isPresent()){
            throw new Validacion("El usuario ya existe");


        }else{
            var nuevoUsuario = new Usuario(datos);
            usuarioRepository.save(nuevoUsuario);
            return new DatosRespuestaUsuario(nuevoUsuario);
        }

    }
}

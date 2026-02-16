package com.tareas.tareas.domain.usuario;


import com.tareas.tareas.Validacion;
import com.tareas.tareas.domain.tarea.DatosRespuestaTarea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;

    public Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = auth.getName();

        return (Usuario) usuarioRepository.findByEmail(email);
    }

    public List<DatosRespuestaUsuario> listarUsuarios() {
        List<DatosRespuestaUsuario> usuarios = usuarioRepository.findAll().stream()
                .map(u-> new DatosRespuestaUsuario(u.getId(),u.getNombre(),u.getEmail(),u.getTareas().stream().map(t-> new DatosRespuestaTarea(t)).toList()))
                .toList();

        return usuarios;

    }
    public DatosRespuestaUsuario buscarUsuario(Long usuarioId){
        var usuario = usuarioRepository.findById(usuarioId);

        if(usuario.isPresent()){
            var usuarioEncontrado = usuario.get();
            return new DatosRespuestaUsuario(usuarioEncontrado);
        }else {
            throw new Validacion("El usuario no existe");
        }
    }

    public DatosRespuestaUsuario crearUsuario(DatosCrearUsuario datos) {

        var usuario = usuarioRepository.findByEmail(datos.email());
        if(usuario != null){
            throw new Validacion("El usuario ya existe");


        }else{
            var nuevoUsuario = new Usuario(datos);
            usuarioRepository.save(nuevoUsuario);
            return new DatosRespuestaUsuario(nuevoUsuario);
        }

    }
    public DatosRespuestaUsuario modificarUsuario(DatosActualizarUsuario datos){
        var usuario = usuarioRepository.findById(datos.id());

        if(usuario.isPresent()){
            var usuarioEncontrado = usuario.get();
            usuarioEncontrado.actualizarUsuario(datos);

            return new DatosRespuestaUsuario(usuarioEncontrado);
        }else{
            throw new Validacion("El usuario no existe");
        }

    }

    public void eliminarUsuario(Long id){

        var usuario = usuarioRepository.findById(id);

        if(usuario.isPresent()){
            usuarioRepository.deleteById(id);
        }else{
            throw new Validacion("El usuario no existe");
        }
    }

}

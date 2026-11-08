package com.financialbeacon.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore // la clave (hash) jamás sale en una respuesta JSON
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombre;

    // ---- Datos personales (se piden en el primer inicio de sesión) ----
    private String apellido;

    private LocalDate fechaNacimiento;

    private String telefono;

    private String ciudad;

    private String ocupacion;

    @Enumerated(EnumType.STRING)
    private TipoIngreso tipoIngreso;

    // Foto de perfil como "data URL" (imagen reducida en el celular). Nunca se serializa sola.
    @JsonIgnore
    @Column(length = 300000)
    private String fotoPerfil;

    // Segundos desde 1970 del último cambio de contraseña: los tokens emitidos antes dejan de valer.
    @JsonIgnore
    private Long cambioPasswordEn;

    public User() {}

    public User(Long id, String email, String password, String nombre) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
    }

    /** El perfil está completo cuando ya entregó todos los datos personales obligatorios. */
    public boolean tienePerfilCompleto() {
        return lleno(nombre) && lleno(apellido) && fechaNacimiento != null
                && lleno(telefono) && lleno(ciudad) && lleno(ocupacion) && tipoIngreso != null;
    }

    private static boolean lleno(String s) {
        return s != null && !s.isBlank();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }

    public TipoIngreso getTipoIngreso() { return tipoIngreso; }
    public void setTipoIngreso(TipoIngreso tipoIngreso) { this.tipoIngreso = tipoIngreso; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public Long getCambioPasswordEn() { return cambioPasswordEn; }
    public void setCambioPasswordEn(Long cambioPasswordEn) { this.cambioPasswordEn = cambioPasswordEn; }
}

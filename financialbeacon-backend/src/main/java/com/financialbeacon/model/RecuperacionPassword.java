package com.financialbeacon.model;

import jakarta.persistence.*;

import java.time.Instant;

/** Código de 6 dígitos enviado por correo para restablecer la contraseña (se guarda con hash). */
@Entity
@Table(name = "recuperaciones_password")
public class RecuperacionPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String codigoHash;

    @Column(nullable = false)
    private Instant expiraEn;

    @Column(nullable = false)
    private int intentos;

    @Column(nullable = false)
    private Instant creadoEn;

    public RecuperacionPassword() {}

    public RecuperacionPassword(Long id, String email, String codigoHash, Instant expiraEn, int intentos, Instant creadoEn) {
        this.id = id;
        this.email = email;
        this.codigoHash = codigoHash;
        this.expiraEn = expiraEn;
        this.intentos = intentos;
        this.creadoEn = creadoEn;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getCodigoHash() { return codigoHash; }
    public Instant getExpiraEn() { return expiraEn; }
    public int getIntentos() { return intentos; }
    public void setIntentos(int intentos) { this.intentos = intentos; }
    public Instant getCreadoEn() { return creadoEn; }
}

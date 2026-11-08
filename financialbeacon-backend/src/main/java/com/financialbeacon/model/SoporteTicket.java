package com.financialbeacon.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "soporte_tickets")
public class SoporteTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String asunto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaSoporte categoria;

    @Column(nullable = false, length = 2000)
    private String mensaje;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private Instant creadoEn;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public SoporteTicket() {}

    public SoporteTicket(Long id, String asunto, CategoriaSoporte categoria, String mensaje,
                         String estado, Instant creadoEn, User user) {
        this.id = id;
        this.asunto = asunto;
        this.categoria = categoria;
        this.mensaje = mensaje;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.user = user;
    }

    public Long getId() { return id; }
    public String getAsunto() { return asunto; }
    public CategoriaSoporte getCategoria() { return categoria; }
    public String getMensaje() { return mensaje; }
    public String getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public User getUser() { return user; }
}

package com.financialbeacon.service;

import com.financialbeacon.dto.ResumenMensualDTO;
import com.financialbeacon.model.*;
import com.financialbeacon.repository.GastoRepository;
import com.financialbeacon.repository.IngresoMensualRepository;
import com.financialbeacon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class FinanzasService {

    @Autowired
    private GastoRepository gastoRepository;

    @Autowired
    private IngresoMensualRepository ingresoMensualRepository;

    @Autowired
    private UserRepository userRepository;

    // Registra o actualiza el ingreso del mes (es "actualizable mensualmente")
    public IngresoMensual registrarIngreso(Long userId, BigDecimal monto, Integer mes, Integer anio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        IngresoMensual ingreso = ingresoMensualRepository
                .findByUserIdAndMesAndAnio(userId, mes, anio)
                .orElse(new IngresoMensual(null, monto, mes, anio, user));

        ingreso.setMonto(monto);
        return ingresoMensualRepository.save(ingreso);
    }

    public Gasto registrarGasto(Long userId, String descripcion, BigDecimal monto,
                                 Categoria categoria, LocalDate fecha) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Gasto gasto = new Gasto(null, descripcion, monto, categoria, fecha, user);
        return gastoRepository.save(gasto);
    }

    public List<Gasto> listarGastosDelMes(Long userId, Integer mes, Integer anio) {
        LocalDate desde = LocalDate.of(anio, mes, 1);
        LocalDate hasta = YearMonth.of(anio, mes).atEndOfMonth();
        return gastoRepository.findByUserIdAndFechaBetween(userId, desde, hasta);
    }

    public ResumenMensualDTO obtenerResumenMensual(Long userId, Integer mes, Integer anio) {
        BigDecimal ingreso = ingresoMensualRepository
                .findByUserIdAndMesAndAnio(userId, mes, anio)
                .map(IngresoMensual::getMonto)
                .orElse(BigDecimal.ZERO);

        List<Gasto> gastos = listarGastosDelMes(userId, mes, anio);

        BigDecimal totalFijos = sumarPorCategoria(gastos, Categoria.FIJO);
        BigDecimal totalVariables = sumarPorCategoria(gastos, Categoria.VARIABLE);
        BigDecimal totalHormiga = sumarPorCategoria(gastos, Categoria.HORMIGA);
        BigDecimal totalGastos = totalFijos.add(totalVariables).add(totalHormiga);
        BigDecimal ahorroEstimado = ingreso.subtract(totalGastos);

        return new ResumenMensualDTO(
                ingreso, totalFijos, totalVariables, totalHormiga, totalGastos, ahorroEstimado
        );
    }

    private BigDecimal sumarPorCategoria(List<Gasto> gastos, Categoria categoria) {
        return gastos.stream()
                .filter(g -> g.getCategoria() == categoria)
                .map(Gasto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

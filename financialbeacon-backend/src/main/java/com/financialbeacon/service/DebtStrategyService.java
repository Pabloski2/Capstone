package com.financialbeacon.service;

import com.financialbeacon.dto.DeudaDTOs.DeudaConPagoDTO;
import com.financialbeacon.dto.DeudaDTOs.EstrategiaDeudaDTO;
import com.financialbeacon.dto.ResumenMensualDTO;
import com.financialbeacon.model.Deuda;
import com.financialbeacon.model.User;
import com.financialbeacon.repository.DeudaRepository;
import com.financialbeacon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DebtStrategyService {

    // Si la diferencia entre la tasa más alta y la más baja supera este
    // umbral (en puntos porcentuales), conviene priorizar por interés.
    private static final BigDecimal UMBRAL_DIFERENCIA_TASA = BigDecimal.valueOf(8);

    // Si la deuda más chica es menor a este % de la deuda más grande,
    // se considera que hay una "victoria rápida" que vale la pena tomar.
    private static final BigDecimal UMBRAL_PROPORCION_MONTO = BigDecimal.valueOf(0.3);

    @Autowired
    private DeudaRepository deudaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinanzasService finanzasService;

    public Deuda registrarDeuda(Long userId, String nombre, BigDecimal montoTotal,
                                 BigDecimal tasaInteres, BigDecimal cuotaMinima) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Deuda deuda = new Deuda(null, nombre, montoTotal, tasaInteres, cuotaMinima, user);
        return deudaRepository.save(deuda);
    }

    public List<Deuda> listarDeudas(Long userId) {
        return deudaRepository.findByUserId(userId);
    }

    public EstrategiaDeudaDTO generarEstrategia(Long userId, Integer mes, Integer anio) {
        List<Deuda> deudas = deudaRepository.findByUserId(userId);

        EstrategiaDeudaDTO resultado = new EstrategiaDeudaDTO();

        if (deudas.isEmpty()) {
            resultado.setEstrategiaRecomendada("SIN_DEUDAS");
            resultado.setJustificacion("No tienes deudas registradas todavía.");
            resultado.setMontoDisponibleParaDeudas(BigDecimal.ZERO);
            resultado.setOrdenDePago(new ArrayList<>());
            return resultado;
        }

        ResumenMensualDTO resumen = finanzasService.obtenerResumenMensual(userId, mes, anio);
        BigDecimal disponible = resumen.getAhorroEstimado().max(BigDecimal.ZERO);

        BigDecimal maxTasa = deudas.stream().map(Deuda::getTasaInteres).max(Comparator.naturalOrder()).get();
        BigDecimal minTasa = deudas.stream().map(Deuda::getTasaInteres).min(Comparator.naturalOrder()).get();
        BigDecimal diferenciaTasas = maxTasa.subtract(minTasa);

        BigDecimal maxMonto = deudas.stream().map(Deuda::getMontoTotal).max(Comparator.naturalOrder()).get();
        BigDecimal minMonto = deudas.stream().map(Deuda::getMontoTotal).min(Comparator.naturalOrder()).get();
        boolean hayVictoriaRapida = deudas.size() > 1
                && minMonto.compareTo(maxMonto.multiply(UMBRAL_PROPORCION_MONTO)) < 0;

        String estrategia;
        String justificacion;
        List<Deuda> orden;

        if (diferenciaTasas.compareTo(UMBRAL_DIFERENCIA_TASA) >= 0) {
            estrategia = "AVALANCHA";
            justificacion = "Tus deudas tienen tasas de interés bastante distintas entre sí. "
                    + "Priorizar la de mayor tasa (" + maxTasa + "%) es lo que más dinero te ahorra en intereses.";
            orden = deudas.stream()
                    .sorted(Comparator.comparing(Deuda::getTasaInteres).reversed())
                    .toList();
        } else if (hayVictoriaRapida) {
            estrategia = "BOLA_DE_NIEVE";
            justificacion = "Tus tasas de interés son parecidas, pero tienes una deuda bastante más pequeña "
                    + "que el resto. Pagarla primero te da una victoria rápida que ayuda a mantener el hábito.";
            orden = deudas.stream()
                    .sorted(Comparator.comparing(Deuda::getMontoTotal))
                    .toList();
        } else {
            estrategia = "AVALANCHA";
            justificacion = "Tus deudas son relativamente parejas en tasa y monto, así que priorizar "
                    + "por mayor interés es la opción financieramente más eficiente.";
            orden = deudas.stream()
                    .sorted(Comparator.comparing(Deuda::getTasaInteres).reversed())
                    .toList();
        }

        BigDecimal sumaCuotasMinimas = deudas.stream()
                .map(Deuda::getCuotaMinima)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal excedente = disponible.subtract(sumaCuotasMinimas).max(BigDecimal.ZERO);

        List<DeudaConPagoDTO> ordenDePago = new ArrayList<>();
        boolean primeraAsignada = false;
        for (Deuda d : orden) {
            BigDecimal pago = d.getCuotaMinima();
            if (!primeraAsignada) {
                pago = pago.add(excedente).setScale(0, RoundingMode.HALF_UP);
                primeraAsignada = true;
            }
            ordenDePago.add(new DeudaConPagoDTO(d.getId(), d.getNombre(), d.getMontoTotal(), d.getTasaInteres(), pago));
        }

        resultado.setEstrategiaRecomendada(estrategia);
        resultado.setJustificacion(justificacion);
        resultado.setMontoDisponibleParaDeudas(disponible);
        resultado.setOrdenDePago(ordenDePago);
        return resultado;
    }
}

package com.financialbeacon.util;

import com.financialbeacon.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PoliticaPasswordTest {

    @Test
    void rechazaContrasenasComunes() {
        assertThrows(ApiException.class, () -> PoliticaPassword.validar("12345678", "ana@correo.cl"));
        assertThrows(ApiException.class, () -> PoliticaPassword.validar("Password1", "ana@correo.cl"));
    }

    @Test
    void exigeLetrasYNumeros() {
        assertThrows(ApiException.class, () -> PoliticaPassword.validar("sololetrasaqui", "ana@correo.cl"));
        assertThrows(ApiException.class, () -> PoliticaPassword.validar("9876543210", "ana@correo.cl"));
    }

    @Test
    void rechazaContrasenasQueContienenElCorreo() {
        assertThrows(ApiException.class, () -> PoliticaPassword.validar("pablo2024x", "pablo@correo.cl"));
    }

    @Test
    void aceptaContrasenasFuertes() {
        assertDoesNotThrow(() -> PoliticaPassword.validar("Faro-Azul-47!", "pablo@correo.cl"));
    }
}

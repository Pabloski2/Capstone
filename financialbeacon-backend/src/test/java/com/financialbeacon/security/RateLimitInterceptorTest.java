package com.financialbeacon.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitInterceptorTest {

    private static MockHttpServletRequest peticion(String ruta, String ip) {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", ruta);
        req.addHeader("X-Forwarded-For", ip);
        return req;
    }

    @Test
    void bloqueaTrasDemasiadosRegistrosDesdeLaMismaIp() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor();
        for (int i = 0; i < 5; i++) {
            assertTrue(interceptor.preHandle(peticion("/api/auth/register", "1.1.1.1"),
                    new MockHttpServletResponse(), new Object()));
        }
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        assertFalse(interceptor.preHandle(peticion("/api/auth/register", "1.1.1.1"), respuesta, new Object()));
        assertEquals(429, respuesta.getStatus());
    }

    @Test
    void otraIpNoSeAfecta() throws Exception {
        RateLimitInterceptor interceptor = new RateLimitInterceptor();
        for (int i = 0; i < 6; i++) {
            interceptor.preHandle(peticion("/api/auth/register", "2.2.2.2"),
                    new MockHttpServletResponse(), new Object());
        }
        assertTrue(interceptor.preHandle(peticion("/api/auth/register", "3.3.3.3"),
                new MockHttpServletResponse(), new Object()));
    }

    @Test
    void tomaLaUltimaIpDeXForwardedFor() {
        MockHttpServletRequest req = peticion("/api/x", "9.9.9.9, 4.4.4.4");
        assertEquals("4.4.4.4", RateLimitInterceptor.ipCliente(req));
    }
}

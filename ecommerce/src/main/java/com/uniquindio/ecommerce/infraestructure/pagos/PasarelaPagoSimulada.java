package com.uniquindio.ecommerce.infraestructure.pagos;

import com.uniquindio.ecommerce.application.port.out.PasarelaPago;
import com.uniquindio.ecommerce.domain.comercial.Pago;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Pasarela simulada con confirmacion automatica: no contacta ningun banco.
 * Una tarjeta terminada en 0000 se rechaza, para poder demostrar el flujo de pago fallido.
 */
@Component
public class PasarelaPagoSimulada implements PasarelaPago {

    static final String DIGITOS_RECHAZO = "0000";

    @Override
    public Resultado autorizar(Pago.Metodo metodo, String ultimosDigitos, BigDecimal monto) {
        if (metodo == Pago.Metodo.TARJETA && DIGITOS_RECHAZO.equals(ultimosDigitos)) {
            return new Resultado(false, null, "fondos insuficientes (simulado).");
        }
        return new Resultado(true, "SIM-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase(), null);
    }
}

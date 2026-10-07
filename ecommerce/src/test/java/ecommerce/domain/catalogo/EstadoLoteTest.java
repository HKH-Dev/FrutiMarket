package ecommerce.domain.catalogo;

import com.uniquindio.ecommerce.domain.catalogo.EstadoLote;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstadoLoteTest {

    @Test
    void unEstadoTerminalNoPuedePasarANingunOtroEstado() {
        // Arrange
        EstadoLote retirado = EstadoLote.RETIRADO;

        // Act
        boolean puedeVolverAPublicarse = retirado.puedePasarA(EstadoLote.PUBLICADO);

        // Assert
        assertTrue(retirado.esTerminal());
        assertFalse(puedeVolverAPublicarse);
    }

    @Test
    void soloUnLotePublicadoAdmitePedidos() {
        // Arrange & Act & Assert
        assertTrue(EstadoLote.PUBLICADO.admitePedidos());
        assertFalse(EstadoLote.BORRADOR.admitePedidos());
        assertFalse(EstadoLote.AGOTADO.admitePedidos());
        assertFalse(EstadoLote.EN_ACOPIO.admitePedidos());
    }
}

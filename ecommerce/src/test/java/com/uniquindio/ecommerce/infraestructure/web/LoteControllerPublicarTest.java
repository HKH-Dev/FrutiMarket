package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.application.port.in.ConsultarLoteUseCase;
import com.uniquindio.ecommerce.application.port.in.RegistrarLoteUseCase;
import com.uniquindio.ecommerce.application.port.out.PublicadorEventos;
import com.uniquindio.ecommerce.application.port.out.Reloj;
import com.uniquindio.ecommerce.application.usecase.PublicarLoteService;
import com.uniquindio.ecommerce.domain.catalogo.Lote;
import com.uniquindio.ecommerce.domain.entity.Campesino;
import com.uniquindio.ecommerce.domain.repository.CampesinoRepository;
import com.uniquindio.ecommerce.domain.repository.LoteRepository;
import com.uniquindio.ecommerce.domain.valueobject.identidad.CampesinoId;
import com.uniquindio.ecommerce.infraestructure.seguridad.SeguridadConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;
import java.util.UUID;

import static com.uniquindio.ecommerce.LotesDePrueba.AHORA;
import static com.uniquindio.ecommerce.LotesDePrueba.mango;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Punto 4: mas casos en el controller, sobre {@code POST /api/lotes/{id}/publicar}.
 *
 * <p>{@code @WebMvcTest} carga solo la capa web (este controller, el manejador de errores y la
 * seguridad). El caso de uso {@link PublicarLoteService} es el real, para que las reglas se apliquen
 * de verdad; lo que se simula con Mockito son los repositorios. Asi se puede comprobar con
 * {@code verify(..., never())} que, si algo falla, el lote <b>nunca se guarda</b>
 * ({@code almacenar} es el "guardar" de nuestro repositorio).</p>
 */
@WebMvcTest(LoteController.class)
@Import({PublicarLoteService.class, SeguridadConfig.class})
class LoteControllerPublicarTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean LoteRepository loteRepository;
    @MockitoBean CampesinoRepository campesinoRepository;
    @MockitoBean Reloj reloj;
    @MockitoBean PublicadorEventos publicadorEventos;
    // El controller tambien depende de estos casos de uso, que estas pruebas no usan.
    @MockitoBean RegistrarLoteUseCase registrarLote;
    @MockitoBean ConsultarLoteUseCase consultarLote;

    private Lote lote;

    @BeforeEach
    void prepararLoteEnBorrador() {
        lote = mango("100").build();
        when(reloj.ahora()).thenReturn(AHORA);
    }

    @Test
    void publicarUnLoteQueNoExisteResponde404YNoGuardaNada() throws Exception {
        // Preparar: el repositorio no encuentra el lote
        when(loteRepository.obtenerLote(any())).thenReturn(Optional.empty());

        // Ejecutar
        publicar(UUID.randomUUID(), lote.campesinoResponsable())
                // Verificar: 404 y nunca se llamo a guardar
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"));
        verify(loteRepository, never()).almacenar(any());
    }

    @Test
    void campesinoNoAutorizadoResponde422ConReglaR1YNoGuardaNada() throws Exception {
        // Preparar: el lote existe, pero su campesino no esta autorizado para publicar (regla 1)
        when(loteRepository.obtenerLote(lote.id())).thenReturn(Optional.of(lote));
        when(campesinoRepository.obtenerCampesino(lote.campesinoResponsable()))
                .thenReturn(Optional.of(campesino(lote.campesinoResponsable(), false)));

        // Ejecutar
        publicar(lote.id().valor(), lote.campesinoResponsable())
                // Verificar: 422 con el codigo de la regla y nunca se llamo a guardar
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R1"));
        verify(loteRepository, never()).almacenar(any());
    }

    @Test
    void otroCampesinoNoPuedePublicarLoteAjenoResponde422ConReglaR5YNoGuardaNada() throws Exception {
        // Preparar: quien pide publicar no es el dueno del lote (regla 5)
        when(loteRepository.obtenerLote(lote.id())).thenReturn(Optional.of(lote));

        // Ejecutar
        publicar(lote.id().valor(), CampesinoId.nuevo())
                // Verificar
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.regla").value("R5"));
        verify(loteRepository, never()).almacenar(any());
    }

    /** Caso de control: si todo esta bien si se guarda, asi sabemos que los never() de arriba significan algo. */
    @Test
    void campesinoAutorizadoPublicaYElLoteSeGuardaUnaVez() throws Exception {
        // Preparar
        when(loteRepository.obtenerLote(lote.id())).thenReturn(Optional.of(lote));
        when(loteRepository.almacenar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(campesinoRepository.obtenerCampesino(lote.campesinoResponsable()))
                .thenReturn(Optional.of(campesino(lote.campesinoResponsable(), true)));

        // Ejecutar
        publicar(lote.id().valor(), lote.campesinoResponsable())
                // Verificar
                .andExpect(status().isNoContent());
        verify(loteRepository, times(1)).almacenar(lote);
    }

    private ResultActions publicar(UUID loteId, CampesinoId solicitante) throws Exception {
        return mvc.perform(post("/api/lotes/" + loteId + "/publicar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"campesinoId\":\"" + solicitante + "\"}"));
    }

    private static Campesino campesino(CampesinoId id, boolean autorizado) {
        Campesino campesino = Campesino.builder().id(id).nombre("Maria Gomez").numeroIdentificacion("1094000001").build();
        if (autorizado) {
            campesino.autorizarParaPublicar();
        }
        return campesino;
    }
}

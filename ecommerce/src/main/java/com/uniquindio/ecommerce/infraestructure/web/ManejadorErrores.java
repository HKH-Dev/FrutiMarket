package com.uniquindio.ecommerce.infraestructure.web;

import com.uniquindio.ecommerce.domain.exception.RecursoNoEncontradoException;
import com.uniquindio.ecommerce.domain.exception.ReglaDeNegocioVioladaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones en respuestas RFC 9457 ({@code application/problem+json}).
 *
 * <ul>
 *   <li>Regla de negocio violada: 422, con el codigo de la regla en {@code regla} (p. ej. "R3").</li>
 *   <li>Recurso inexistente: 404.</li>
 *   <li>Cuerpo invalido: 400, con el detalle por campo en {@code errores}.</li>
 *   <li>JSON ilegible: 400 (con la causa tecnica solo si {@code frutimarket.errores.mostrar-causa=true}); tipos o parametros faltantes: 400 (lo resuelve la clase base).</li>
 * </ul>
 */
@RestControllerAdvice
public class ManejadorErrores extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    /** Si se envia al cliente la causa tecnica de un JSON ilegible. Util en desarrollo; apagado en produccion. */
    private final boolean mostrarCausa;

    public ManejadorErrores(@Value("${frutimarket.errores.mostrar-causa:false}") boolean mostrarCausa) {
        this.mostrarCausa = mostrarCausa;
    }

    @ExceptionHandler(ReglaDeNegocioVioladaException.class)
    public ProblemDetail reglaViolada(ReglaDeNegocioVioladaException ex) {
        // Un id mal formado no es una regla del negocio sino una peticion invalida.
        HttpStatus estado = "INV-ID".equals(ex.codigoRegla()) ? HttpStatus.BAD_REQUEST : HttpStatus.UNPROCESSABLE_CONTENT;
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, ex.getMessage());
        problema.setTitle("Regla de negocio violada");
        problema.setProperty("regla", ex.codigoRegla());
        return problema;
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problema.setTitle("Recurso no encontrado");
        return problema;
    }

    /** P. ej. dos peticiones simultaneas registran el mismo codigo de lote. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail conflicto(DataIntegrityViolationException ex) {
        LOG.warn("Violacion de integridad", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "El registro entra en conflicto con datos existentes (por ejemplo, un codigo repetido).");
        problema.setTitle("Conflicto de datos");
        return problema;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail inesperado(Exception ex) {
        LOG.error("Error no controlado", ex);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Intente de nuevo o contacte al administrador.");
        problema.setTitle("Error interno");
        return problema;
    }

    /** JSON mal formado, campo con tipo equivocado, valor de enum desconocido o campo primitivo ausente. */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "No se pudo leer el JSON: revise la sintaxis, que los campos obligatorios esten presentes "
                        + "y que fechas, numeros y valores de lista tengan el formato correcto.");
        problema.setTitle("Cuerpo de la peticion no valido");
        Throwable causa = ex.getMostSpecificCause();
        if (mostrarCausa && causa != ex && causa.getMessage() != null) {
            problema.setProperty("causa", causa.getMessage().lines().findFirst().orElse(""));
        }
        return ResponseEntity.badRequest().body(problema);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "La peticion tiene campos invalidos.");
        problema.setTitle("Datos invalidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }
}

package com.almara.modules.emotion.services;

import com.almara.common.ExcepcionComentarioInvalido;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio encargado de la validación, sanitización y moderación de comentarios (HU-03).
 * Cumple con RNF Seguridad (mitigación XSS e inyección) y RNF Desempeño (< 300 ms).
 */
@Service
public class ServicioModeracionComentario {

    public static final int LONGITUD_MAXIMA = 200;

    // Expresión regular que detecta presencia de cualquier dígito numérico (Criterio 2)
    private static final Pattern PATRON_DIGITOS = Pattern.compile(".*\\d.*");

    // Expresión regular para detectar etiquetas HTML o patrones XSS peligrosos
    private static final Pattern PATRON_XSS_PELIGROSO = Pattern.compile(
            "(?i)<\\s*script.*?>|(?i)</\\s*script.*?>|(?i)javascript:|(?i)onerror=|(?i)onload=|(?i)<|>"
    );

    // Lista básica de palabras inapropiadas u ofensivas para moderación de contenido
    private static final List<String> PALABRAS_OFENSIVAS = List.of(
            "idiota", "estupido", "imbecil", "maldito", "puta", "mierda", "gonorrea", "hp"
    );

    /**
     * Valida, sanitiza y modera un comentario complementario.
     * Si el comentario es nulo o vacío, se retorna null (Criterio 3: totalmente opcional).
     *
     * @param comentarioTexto Texto ingresado por el usuario.
     * @return Texto limpio, moderado y sanitizado, o null si no se ingresó comentario.
     */
    public String procesarYValidarComentario(String comentarioTexto) {
        if (comentarioTexto == null || comentarioTexto.trim().isEmpty()) {
            return null;
        }

        String texto = comentarioTexto.trim();

        // Criterio 1: Longitud máxima de 200 caracteres
        if (texto.length() > LONGITUD_MAXIMA) {
            throw new ExcepcionComentarioInvalido(
                    "LONGITUD_EXCEDIDA",
                    "El comentario no debe superar la longitud máxima permitida de " + LONGITUD_MAXIMA + " caracteres (actual: " + texto.length() + ")."
            );
        }

        // Criterio 2: El campo de texto no debe permitir el ingreso de números
        if (PATRON_DIGITOS.matcher(texto).matches()) {
            throw new ExcepcionComentarioInvalido(
                    "CARACTERES_INVALIDOS",
                    "El comentario no debe permitir el ingreso de números o dígitos numéricos según la especificación de HU-03."
            );
        }

        // RNF Seguridad: Prevención estricta de XSS
        if (PATRON_XSS_PELIGROSO.matcher(texto).find()) {
            throw new ExcepcionComentarioInvalido(
                    "SEGURIDAD_XSS_DETECTADA",
                    "El comentario contiene etiquetas HTML o caracteres no válidos para la seguridad del sistema."
            );
        }

        // Moderación de contenido: reemplazo/enmascarado de términos ofensivos
        return moderarContenido(texto);
    }

    /**
     * Reemplaza palabras ofensivas por asteriscos para preservar la integridad de la plataforma.
     */
    private String moderarContenido(String texto) {
        String resultado = texto;
        for (String termino : PALABRAS_OFENSIVAS) {
            String patronTermino = "(?i)\\b" + Pattern.quote(termino) + "\\b";
            resultado = resultado.replaceAll(patronTermino, "***");
        }
        return resultado;
    }
}

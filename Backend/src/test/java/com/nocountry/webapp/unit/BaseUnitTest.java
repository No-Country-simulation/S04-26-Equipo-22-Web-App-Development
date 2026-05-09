package com.nocountry.webapp.unit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Clase base para todos los Unit Tests del servicio.
 * Proporciona configuración común de Mockito.
 * 
 * Uso: extender esta clase en lugar de usar @ExtendWith en cada test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class BaseUnitTest {
    
    /**
     * Método helper para crear mensajes de error consistentes
     */
    protected String notFoundMessage(String entityName, Long id) {
        return String.format("%s no encontrada con ID: %d", entityName, id);
    }
    
    /**
     * Método helper para mensajes de conflicto
     */
    protected String conflictMessage(String entityName, String field, String value) {
        return String.format("Ya existe un %s con %s: %s", entityName, field, value);
    }
}
package com.nocountry.webapp.controller;

import com.nocountry.webapp.dto.WeeklyDigestRequestDTO;
import com.nocountry.webapp.dto.WeeklyDigestResponseDTO;
import com.nocountry.webapp.service.WeeklyDigestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/digests")
@RequiredArgsConstructor
public class WeeklyDigestController {

    private final WeeklyDigestService digestService;

    // 1. LISTAR TODOS (Para la pantalla del historial o listado de resúmenes)
    @GetMapping
    public ResponseEntity<List<WeeklyDigestResponseDTO>> getAllDigests() {
        return ResponseEntity.ok(digestService.findAll());
    }

    // 2. DETALLE DE UN RESUMEN (El endpoint clave para la pantalla que me pasaste antes)
    @GetMapping("/{id}")
    public ResponseEntity<WeeklyDigestResponseDTO> getDigestById(@PathVariable Long id) {
        return ResponseEntity.ok(digestService.findById(id));
    }

    // 3. ACTUALIZAR TEXTO O ESTADO (Para cuando el editor cambia el flujo a EN_REVISION, APROBADO, etc.)
    @PutMapping("/{id}")
    public ResponseEntity<WeeklyDigestResponseDTO> updateDigest(
            @PathVariable Long id, 
            @RequestBody WeeklyDigestRequestDTO digestRequestDTO) {
        return ResponseEntity.ok(digestService.update(id, digestRequestDTO));
    }

    // 4. ELIMINAR (Baja por si se genera un borrador erróneo)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDigest(@PathVariable Long id) {
        digestService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
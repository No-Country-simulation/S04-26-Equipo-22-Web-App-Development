package com.nocountry.webapp.unit.service;

import com.nocountry.webapp.entity.Community;
import com.nocountry.webapp.exception.base.ConflictException;
import com.nocountry.webapp.exception.base.NotFoundException;
import com.nocountry.webapp.repository.CommunityRepository;
import com.nocountry.webapp.service.CommunityService;
import com.nocountry.webapp.unit.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test para CommunityService.
 * 
 * Por qué unit test:
 * - Probamos la LÓGICA del servicio, no la base de datos
 * - Los mocks aislan completamente el servicio de sus dependencias
 * - Son más rápidos (no levantan Spring Context)
 * - Si falla, sabemos que el error está en el servicio, no en la DB
 */
class CommunityServiceUnitTest extends BaseUnitTest {

    // @Mock: Crea un objeto falso que simula el comportamiento del repositorio
    // NO usa base de datos real. Mockito crea este mock y luego lo inyecta en el servicio
    @Mock
    private CommunityRepository communityRepository;

    // @InjectMocks: Inyecta los mocks (@Mock) dentro del servicio real
    // CommunityService REAL recibe CommunityRepository MOCKEADO
    @InjectMocks
    private CommunityService communityService;

    // Objeto de prueba reusable para evitar duplicación
    private Community community;

    // @BeforeEach: Se ejecuta ANTES de cada test
    // Garantiza que cada test empieza con un estado limpio y predecible
    @BeforeEach
    void setUp() {
        community = new Community();
        community.setId(1L);
        community.setName("Backend Developers");
        community.setPlatform("DISCORD");
        community.setActive(true);
    }

    // ==================== TESTS DE LECTURA ====================

    @Test
    void getAllActiveCommunities_ShouldReturnOnlyActiveCommunities() {
        // ARRANGE (Given) - Configurar lo que el mock debe devolver
        // when(...).thenReturn(...) : Programamos el comportamiento del mock
        when(communityRepository.findByIsActiveTrue()).thenReturn(List.of(community));

        // ACT (When) - Ejecutar el método que queremos probar
        List<Community> result = communityService.getAllActiveCommunities();

        // ASSERT (Then) - Verificar que el resultado es el esperado
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Backend Developers");
        
        // verify: Comprueba que el mock fue llamado exactamente 1 vez
        // Si no se llama, el test falla. Esto asegura que el servicio USÓ el repositorio
        verify(communityRepository, times(1)).findByIsActiveTrue();
    }

    @Test
    void getCommunityById_WhenExists_ShouldReturnCommunity() {
        // ARRANGE: Simulamos que el repositorio encuentra la comunidad
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));

        // ACT
        Community result = communityService.getCommunityById(1L);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Backend Developers");
    }

    @Test
    void getCommunityById_WhenNotExists_ShouldThrowNotFoundException() {
        // ARRANGE: Simulamos que el repositorio NO encuentra nada
        // Optional.empty() representa "no existe en base de datos"
        when(communityRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificamos que se lanza la excepción correcta
        // assertThatThrownBy() captura la excepción y permite inspeccionarla
        assertThatThrownBy(() -> communityService.getCommunityById(99L))
                .isInstanceOf(NotFoundException.class)  // Tipo de excepción
                .hasMessageContaining(notFoundMessage("Comunidad", 99L)); // Mensaje esperado
        
        // NOTA: Usamos el helper notFoundMessage() de BaseUnitTest
        // Esto asegura mensajes de error consistentes en toda la app
    }

    // ==================== TESTS DE CREACIÓN ====================

    @Test
    void createCommunity_WithValidData_ShouldSaveAndReturn() {
        // ARRANGE: Datos de entrada
        Community newCommunity = new Community();
        newCommunity.setName("Data Science Hub");
        newCommunity.setPlatform("SLACK");

        // Simulamos que NO existe duplicado (primera validación)
        when(communityRepository.existsByNameIgnoreCase("Data Science Hub")).thenReturn(false);
        
        // Simulamos el guardado: cuando se llame a save(), asignamos un ID automáticamente
        // Esto imita el comportamiento real de la base de datos (autoincremental)
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> {
            Community saved = invocation.getArgument(0); // Obtiene el argumento pasado
            saved.setId(3L); // Simula que la DB asigna un ID
            return saved;
        });

        // ACT
        Community result = communityService.createCommunity(newCommunity);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L); // Verifica que se asignó ID
        assertThat(result.getName()).isEqualTo("Data Science Hub");
        
        // Verifica que se llamó a save() EXACTAMENTE una vez
        // Si el servicio llama a save() más veces o ninguna, falla
        verify(communityRepository, times(1)).save(any(Community.class));
    }

    @Test
    void createCommunity_WithDuplicateName_ShouldThrowConflictException() {
        // ARRANGE: Nombre ya existente
        Community duplicate = new Community();
        duplicate.setName("Backend Developers");

        // Simulamos que YA existe (true = ya hay una con ese nombre)
        when(communityRepository.existsByNameIgnoreCase("Backend Developers")).thenReturn(true);

        // ACT & ASSERT: Debe lanzar ConflictException
        assertThatThrownBy(() -> communityService.createCommunity(duplicate))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(conflictMessage("comunidad", "nombre", "Backend Developers"));

        // IMPORTANTE: Verificamos que NUNCA se llame a save()
        // Si el servicio intenta guardar a pesar del duplicado, el test falla
        verify(communityRepository, never()).save(any());
    }

    @Test
    void createCommunity_WithBlankName_ShouldThrowIllegalArgumentException() {
        // ARRANGE: Nombre vacío o solo espacios
        Community invalid = new Community();
        invalid.setName("   ");

        // ACT & ASSERT: Validación síncrona antes de llamar al repositorio
        assertThatThrownBy(() -> communityService.createCommunity(invalid))
                .isInstanceOf(IllegalArgumentException.class) // Excepción de Java, no custom
                .hasMessageContaining("El nombre de la comunidad es obligatorio");

        // Verifica que NUNCA se consultó el repositorio para ver duplicados
        // Porque la validación de nombre vacío ocurre ANTES
        verify(communityRepository, never()).existsByNameIgnoreCase(any());
        verify(communityRepository, never()).save(any());
    }

    // ==================== TESTS DE ACTUALIZACIÓN ====================

    @Test
    void updateCommunity_WhenExists_ShouldUpdateFields() {
        // ARRANGE: Datos nuevos para actualizar
        Community updatedData = new Community();
        updatedData.setName("Backend Experts");
        updatedData.setPlatform("TELEGRAM");
        updatedData.setActive(true);

        // Simulamos que la comunidad existe
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
        // Simulamos que el nuevo nombre NO está siendo usado por OTRA comunidad
        when(communityRepository.existsByNameIgnoreCase("Backend Experts")).thenReturn(false);
        // Simulamos el guardado (devuelve lo mismo que se guarda)
        when(communityRepository.save(any(Community.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // ACT
        Community result = communityService.updateCommunity(1L, updatedData);

        // ASSERT
        assertThat(result.getName()).isEqualTo("Backend Experts");
        assertThat(result.getPlatform()).isEqualTo("TELEGRAM");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void updateCommunity_WithDuplicateName_ShouldThrowConflictException() {
        // ARRANGE: Otra comunidad (ID 2) ya tiene el nombre que queremos poner
        Community updatedData = new Community();
        updatedData.setName("Other Community"); // Este nombre ya existe en ID 2

        // La comunidad a actualizar (ID 1) existe
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
        // existsByNameIgnoreCase devuelve true porque ID 2 ya tiene ese nombre
        when(communityRepository.existsByNameIgnoreCase("Other Community")).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> communityService.updateCommunity(1L, updatedData))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Ya existe una comunidad con el nombre: Other Community");
        
        // Verifica que NUNCA se intentó guardar
        verify(communityRepository, never()).save(any());
    }

    // ==================== TESTS DE BORRADO LÓGICO ====================

    @Test
    void deactivateCommunity_ShouldSetIsActiveToFalse() {
        // ARRANGE: Comunidad existe
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
        when(communityRepository.save(any(Community.class))).thenReturn(community);

        // ACT
        communityService.deactivateCommunity(1L);

        // ASSERT: El objeto fue modificado (isActive = false)
        assertThat(community.isActive()).isFalse();
        
        // Verifica que se guardó el cambio
        verify(communityRepository, times(1)).save(community);
    }

    @Test
    void activateCommunity_ShouldSetIsActiveToTrue() {
        // ARRANGE: Comunidad existe pero está inactiva
        community.setActive(false);
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
        when(communityRepository.save(any(Community.class))).thenReturn(community);

        // ACT
        communityService.activateCommunity(1L);

        // ASSERT: Ahora está activa
        assertThat(community.isActive()).isTrue();
        verify(communityRepository, times(1)).save(community);
    }

    // ==================== TESTS DE BORRADO FÍSICO ====================

    @Test
    void deleteCommunityPermanently_ShouldDeleteFromDatabase() {
        // ARRANGE: Comunidad existe
        when(communityRepository.findById(1L)).thenReturn(Optional.of(community));
        // doNothing(): El método delete() no devuelve nada, solo lo simulamos
        doNothing().when(communityRepository).delete(community);

        // ACT
        communityService.deleteCommunityPermanently(1L);

        // ASSERT: Verifica que se llamó a delete() exactamente una vez
        verify(communityRepository, times(1)).delete(community);
    }
}
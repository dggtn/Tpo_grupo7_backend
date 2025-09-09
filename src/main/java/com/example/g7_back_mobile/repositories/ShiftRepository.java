package com.example.g7_back_mobile.repositories;

import com.example.g7_back_mobile.repositories.entities.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    // Buscar turnos por fecha posterior a la actual
    List<Shift> findByLocalTimeAfter(LocalDateTime dateTime);

    // Buscar turnos por sede
    List<Shift> findBySede_Id(Long sedeId);

    // Buscar turnos por tipo de deporte
    List<Shift> findBySportType_Id(Long sportTypeId);

    // Buscar turnos por clase
    List<Shift> findByClase_Id(Long claseId);

    // Consulta personalizada para obtener turnos disponibles (ejemplo)
    @Query("SELECT s FROM Shift s WHERE s.localTime > CURRENT_TIMESTAMP ORDER BY s.localTime")
    List<Shift> findAvailableShifts();

    // Buscar turnos por sede y deporte
    List<Shift> findBySede_IdAndSportType_Id(Long sedeId, Long sportTypeId);
}
package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.PositionResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Position;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.PositionRepository;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CargoServiceImpl implements CargoService {

    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PositionResponse> list() {
        return positionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PositionResponse create(String name, Role systemRole) {
        validateName(name);
        if (positionRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cargo con el nombre: " + name);
        }
        Position position = positionRepository.save(Position.builder()
                .name(name)
                .systemRole(systemRole)
                .build());
        log.info("Cargo created: id={}, name={}, role={}", position.getId(), name, systemRole);
        return toResponse(position);
    }

    @Override
    @Transactional
    public PositionResponse update(UUID id, String name, Role systemRole) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cargo no encontrado"));
        validateName(name);
        if (!position.getName().equals(name) && positionRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un cargo con el nombre: " + name);
        }
        position.setName(name);
        position.setSystemRole(systemRole);
        position = positionRepository.save(position);

        // El rol de sistema se sincroniza en los empleados que tienen este cargo:
        // si cambia, cambia la candidatura y el rol de los usuarios por crear.
        employeeRepository.findByCargo_Id(id).forEach(e -> {
            e.setPosition(name);
            e.setSystemRole(systemRole);
            employeeRepository.save(e);
        });
        log.info("Cargo updated: id={}, name={}, role={}", id, name, systemRole);
        return toResponse(position);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cargo no encontrado"));
        long vinculados = employeeRepository.countByCargo_Id(id);
        if (vinculados > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el cargo porque tiene " + vinculados
                            + " empleado(s) vinculado(s)");
        }
        positionRepository.delete(position);
        log.info("Cargo deleted: id={}", id);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del cargo es obligatorio");
        }
        if (name.length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del cargo no puede superar los 40 caracteres");
        }
    }

    private PositionResponse toResponse(Position position) {
        return PositionResponse.builder()
                .id(position.getId())
                .name(position.getName())
                .systemRole(position.getSystemRole())
                .build();
    }
}

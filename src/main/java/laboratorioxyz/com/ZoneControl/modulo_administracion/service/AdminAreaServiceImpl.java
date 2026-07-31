package laboratorioxyz.com.ZoneControl.modulo_administracion.service;

import laboratorioxyz.com.ZoneControl.model.entity.ProductionArea;
import laboratorioxyz.com.ZoneControl.model.repository.ProductionAreaRepository;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AreaRequest;
import laboratorioxyz.com.ZoneControl.modulo_administracion.dto.AreaResponse;
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
public class AdminAreaServiceImpl implements AdminAreaService {

    private final ProductionAreaRepository areaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AreaResponse> list() {
        return areaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AreaResponse create(AreaRequest request) {
        if (areaRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un área con el nombre '" + request.name() + "'");
        }
        ProductionArea area = ProductionArea.builder()
                .name(request.name())
                .description(request.description())
                .active(true)
                .build();
        area = areaRepository.save(area);
        log.info("Production area created: {} ({})", area.getId(), area.getName());
        return toResponse(area);
    }

    @Override
    @Transactional
    public AreaResponse update(UUID id, AreaRequest request) {
        ProductionArea area = areaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área no encontrada"));
        if (!area.getName().equals(request.name()) && areaRepository.existsByName(request.name())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un área con el nombre '" + request.name() + "'");
        }
        area.setName(request.name());
        area.setDescription(request.description());
        area = areaRepository.save(area);
        log.info("Production area updated: {} ({})", area.getId(), area.getName());
        return toResponse(area);
    }

    @Override
    @Transactional
    public void setActive(UUID id, boolean active) {
        ProductionArea area = areaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Área no encontrada"));
        area.setActive(active);
        areaRepository.save(area);
        log.info("Production area {} status changed to active={}", id, active);
    }

    private AreaResponse toResponse(ProductionArea a) {
        return new AreaResponse(a.getId(), a.getName(), a.getDescription(), a.isActive());
    }
}

package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.service;

import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.repository.DepartmentRepository;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeRequest;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.dto.RegisterEmployeeResponse;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model.Employee;
import laboratorioxyz.com.ZoneControl.modulo_gestion_personal.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public RegisterEmployeeResponse register(RegisterEmployeeRequest request) {
        if (request.getDocumentType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Tipo de documento no válido. Los tipos permitidos son: CC, CE, TI, PA, RC");
        }

        if (employeeRepository.existsByDocumentTypeAndDocumentNumber(
                request.getDocumentType(), request.getDocumentNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Ya existe un empleado registrado con el documento " + request.getDocumentType()
                + " número " + request.getDocumentNumber());
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Departamento no encontrado"));

        String employeeCode = generateEmployeeCode();

        Employee employee = Employee.builder()
                .employeeCode(employeeCode)
                .documentType(request.getDocumentType())
                .documentNumber(request.getDocumentNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .position(request.getPosition())
                .department(department)
                .build();

        employee = employeeRepository.save(employee);

        return RegisterEmployeeResponse.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .build();
    }

    private String generateEmployeeCode() {
        String maxCode = employeeRepository.findMaxEmployeeCode();
        int nextNumber = 1;
        if (maxCode != null) {
            nextNumber = Integer.parseInt(maxCode.substring(4)) + 1;
        }
        return String.format("EMP-%06d", nextNumber);
    }
}

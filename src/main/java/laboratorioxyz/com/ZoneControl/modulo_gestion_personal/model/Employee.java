package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import lombok.*;

import java.util.UUID;

/**
 * Empleados registrados en el sistema de control de acceso.
 * Cada empleado tiene un código interno único EMP-XXXXXX
 * y un documento de identidad colombiano.
 */
@Entity
@Table(name = "employees",
       uniqueConstraints = @UniqueConstraint(columnNames = {"document_type", "document_number"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 12)
    private String employeeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 3, nullable = false)
    private DocumentType documentType;

    @Size(max = 20)
    @Column(name = "document_number", length = 20, nullable = false)
    private String documentNumber;

    @Size(max = 35)
    @Column(length = 35, nullable = false)
    private String firstName;

    @Size(max = 35)
    @Column(length = 35, nullable = false)
    private String lastName;

    @Size(max = 30)
    @Column(length = 30, nullable = false)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(length = 11, nullable = false)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}

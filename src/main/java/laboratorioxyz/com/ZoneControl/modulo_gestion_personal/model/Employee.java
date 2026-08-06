package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.entity.Office;
import laboratorioxyz.com.ZoneControl.model.enums.ContractType;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
import laboratorioxyz.com.ZoneControl.model.enums.WorkShift;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Empleados registrados en el sistema de control de acceso.
 * Cada empleado tiene un código interno único EMP-XXXXXX
 * y un documento de identidad colombiano.
 *
 * Además de los datos básicos, el modelo incorpora la información
 * pertinente para un "empleado real" (mockup 41): tipo de contrato,
 * sede base, turno/horario y fechas de vigencia del contrato.
 * La foto se almacena en {@code uploads/photos/} y se referencia
 * mediante {@link #photoUrl} (ver HU-25 / §9.4.3.1).
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

    /**
     * Cargo del catálogo (fuente de verdad). El rol de sistema del empleado se
     * deriva del cargo ({@link Position#getSystemRole()}); el campo position
     * es la proyección denormalizada del nombre del cargo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cargo_id", nullable = true)
    private Position cargo;

    /**
     * Correo del empleado. Se usa para enviar el magic link cuando el ADMIN
     * crea un usuario del sistema (HU-05). Es nullable porque no todos los
     * empleados requieren acceso al sistema (solo acceso físico).
     */
    @Email
    @Size(max = 100)
    @Column(length = 100, nullable = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 11, nullable = false)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /**
     * Rol de sistema que el Gestor de Personal asigna al empleado.
     * Cuando es no nulo, indica que este empleado es candidato a
     * ser activado como usuario del sistema.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", length = 20, nullable = true)
    private Role systemRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", length = 20, nullable = true)
    private ContractType contractType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_office_id", nullable = true)
    private Office baseOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_shift", length = 10, nullable = true)
    private WorkShift workShift;

    @Column(name = "hire_date", nullable = true)
    private LocalDate hireDate;

    @Column(name = "contract_end_date", nullable = true)
    private LocalDate contractEndDate;

    @Size(max = 255)
    @Column(name = "photo_url", length = 255, nullable = true)
    private String photoUrl;
}

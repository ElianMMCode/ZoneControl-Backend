package laboratorioxyz.com.ZoneControl.modulo_gestion_personal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.entity.Department;
import laboratorioxyz.com.ZoneControl.model.enums.DocumentType;
import laboratorioxyz.com.ZoneControl.model.enums.EmployeeStatus;
import laboratorioxyz.com.ZoneControl.model.enums.Role;
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

    /**
     * Correo personal del empleado (no corporativo).
     * Se usa para enviar el magic link cuando el ADMIN crea un usuario
     * del sistema (HU-05). Es nullable porque no todos los empleados
     * requieren acceso al sistema (solo acceso físico).
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
     * ser activado como usuario del sistema (Admin podrá entonces
     * crear la cuenta y enviarle el magic link). Es nulo para
     * empleados que solo requieren acceso físico.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", length = 20, nullable = true)
    private Role systemRole;
}

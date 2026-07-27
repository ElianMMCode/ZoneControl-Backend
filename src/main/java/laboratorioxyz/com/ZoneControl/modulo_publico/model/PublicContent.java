package laboratorioxyz.com.ZoneControl.modulo_publico.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import laboratorioxyz.com.ZoneControl.model.enums.ContentSection;
import lombok.*;

import java.util.UUID;

/**
 * Contenido clave-valor del módulo público, organizado por secciones.
 * Ej: INSTITUTIONAL → mission, vision, description
 *     CONTACT → phone, email, socialNetworks
 *     LOCATIONS → mainOffice, hours
 */
@Entity
@Table(name = "public_contents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private ContentSection section;

    @Size(max = 80)
    @Column(length = 80, nullable = false)
    private String key;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;
}

package in.bluebytes.statechanger.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Document {
    /**
     * ID to uniquely identify a document in the workflow
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * The current state at any given time. Enumerated annotation insists the type of Enum as String
     * otherwise it would result in Integer value
     */
    @Enumerated(EnumType.STRING)
    private DocumentState documentState;

    /**
     * Holds the future document state that the current state has to transition to
     */
    private String stateToChange;
}

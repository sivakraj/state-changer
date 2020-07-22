package in.bluebytes.statechanger.services;

import in.bluebytes.statechanger.domain.Document;
import in.bluebytes.statechanger.domain.DocumentEvent;
import in.bluebytes.statechanger.domain.DocumentState;
import org.springframework.statemachine.StateMachine;

public interface DocumentService {

    /**
     * Creates a new document entity in the database with the given machine state and the future
     * state it has to transition to
     * @param document
     * @return saved document entity
     */
    Document newDocument(Document document);

    /**
     * Gets state machine from persistence and processes it for create flow
     * @param documentId
     * @return state machine with create flow
     */
    StateMachine<DocumentState, DocumentEvent> createDocument(Long documentId);

    /**
     * Gets state machine from persistence and processes it for update flow
     * @param documentId
     * @return state machine with update flow
     */
    StateMachine<DocumentState, DocumentEvent> updateDocument(Long documentId);

    /**
     * Gets state machine from persistence and processes it for settle flow
     * @param documentId
     * @return state machine with settle flow
     */
    StateMachine<DocumentState, DocumentEvent> settleDocument(Long documentId);

}

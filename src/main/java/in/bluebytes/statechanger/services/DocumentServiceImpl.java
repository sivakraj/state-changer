package in.bluebytes.statechanger.services;

import in.bluebytes.statechanger.domain.Document;
import in.bluebytes.statechanger.domain.DocumentEvent;
import in.bluebytes.statechanger.domain.DocumentState;
import in.bluebytes.statechanger.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final StateMachineFactory<DocumentState, DocumentEvent> stateMachineFactory;
    private final DocumentStateChangeInterceptor documentStateChangeInterceptor;

    public static final String DOCUMENT_ID_HEADER = "document_id";

    /**
     * Creates a new document entity in the database with the given machine state and the future
     * state it has to transition to
     * @param document
     * @return saved document entity
     */
    @Override
    public Document newDocument(Document document) {
        //document.setDocumentState(DocumentState.IN_PROGRESS);
        return documentRepository.save(document);
    }

    /**
     * Gets state machine from persistence and processes it for create flow
     * Transactional annotation is required to commit transaction within boundary due
     * to using lombok goodness in this project. Throws lazy initialization error otherwise due to
     * lombok Data annotations' toString, equal and hash methods
     * @param documentId
     * @return state machine with create flow
     */
    @Transactional
    @Override
    public StateMachine<DocumentState, DocumentEvent> createDocument(Long documentId) {
        StateMachine<DocumentState, DocumentEvent> stateMachine = getPersistedStateMachine(documentId);

        sendEvent(documentId, stateMachine, DocumentEvent.CREATE);

        return stateMachine;
    }

    /**
     * Gets state machine from persistence and processes it for update flow
     * Transactional annotation is required to commit transaction within boundary due
     * to using lombok goodness in this project. Throws lazy initialization error otherwise due to
     * lombok Data annotations' toString, equal and hash methods
     * @param documentId
     * @return state machine with update flow
     */
    @Transactional
    @Override
    public StateMachine<DocumentState, DocumentEvent> updateDocument(Long documentId) {
        StateMachine<DocumentState, DocumentEvent> stateMachine = getPersistedStateMachine(documentId);

        sendEvent(documentId, stateMachine, DocumentEvent.UPDATE);

        return stateMachine;
    }

    /**
     * Gets state machine from persistence and processes it for settle flow
     * Transactional annotation is required to commit transaction within boundary due
     * to using lombok goodness in this project. Throws lazy initialization error otherwise due to
     * lombok Data annotations' toString, equal and hash methods
     * @param documentId
     * @return state machine with settle flow
     */
    @Transactional
    @Override
    public StateMachine<DocumentState, DocumentEvent> settleDocument(Long documentId) {
        StateMachine<DocumentState, DocumentEvent> stateMachine = getPersistedStateMachine(documentId);

        sendEvent(documentId, stateMachine, DocumentEvent.SETTLE);

        return stateMachine;
    }

    /**
     * Loads the current state machine with the id and repurpose it with the events present in the message
     * if any and from persistence and returns renewed state machine
     * @param documentId
     * @return
     */
    private StateMachine<DocumentState, DocumentEvent> getPersistedStateMachine(Long documentId) {
        Document document = documentRepository.getOne(documentId);

        StateMachine<DocumentState, DocumentEvent> stateMachine = stateMachineFactory.getStateMachine(Long.toString(documentId));
        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(stateMachineAccessor -> {
                    stateMachineAccessor.addStateMachineInterceptor(documentStateChangeInterceptor);
                    stateMachineAccessor.resetStateMachine(new DefaultStateMachineContext<>(document.getDocumentState(),
                            null, null, null));
                });

        //Extended variables is used to pass document object to the action methods during transition
        stateMachine.getExtendedState().getVariables().put("document", document);
        stateMachine.start();

        return stateMachine;
    }

    /**
     * Sends an event to the state machine using Spring message conventions, decorated with document id header
     * @param documentId
     * @param stateMachine
     * @param event
     */
    private void sendEvent(Long documentId, StateMachine<DocumentState, DocumentEvent> stateMachine, DocumentEvent event) {
        Message message = MessageBuilder.withPayload(event)
                .setHeader(DOCUMENT_ID_HEADER, documentId)
                .build();

        stateMachine.sendEvent(message);
    }
}

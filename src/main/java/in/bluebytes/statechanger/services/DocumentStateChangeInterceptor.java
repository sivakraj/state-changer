package in.bluebytes.statechanger.services;

import in.bluebytes.statechanger.domain.Document;
import in.bluebytes.statechanger.domain.DocumentEvent;
import in.bluebytes.statechanger.domain.DocumentState;
import in.bluebytes.statechanger.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DocumentStateChangeInterceptor extends StateMachineInterceptorAdapter<DocumentState, DocumentEvent> {

    private final DocumentRepository documentRepository;

    /**
     * Gets triggered before state change to see if there're any events sent as messages for transition and
     * takes care of that
     * Used in conjunction with StateMachine creation for respective flows.
     * @param state
     * @param message
     * @param transition
     * @param stateMachine
     */
    @Override
    public void preStateChange(State<DocumentState, DocumentEvent> state, Message<DocumentEvent> message,
                               Transition<DocumentState, DocumentEvent> transition, StateMachine<DocumentState, DocumentEvent> stateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(DocumentServiceImpl.DOCUMENT_ID_HEADER, -1L)))
                    .ifPresent(documentId -> {
                        Document document = documentRepository.getOne(documentId);
                        document.setDocumentState(state.getId());
                        documentRepository.save(document);
                    });
        });

    }
}

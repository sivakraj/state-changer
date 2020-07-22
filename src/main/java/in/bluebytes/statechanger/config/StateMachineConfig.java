package in.bluebytes.statechanger.config;

import in.bluebytes.statechanger.domain.Document;
import in.bluebytes.statechanger.domain.DocumentEvent;
import in.bluebytes.statechanger.domain.DocumentState;
import in.bluebytes.statechanger.domain.IncomingState;
import in.bluebytes.statechanger.services.DocumentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import java.util.EnumSet;
import java.util.Optional;

@Slf4j
@EnableStateMachineFactory
@RequiredArgsConstructor
@Configuration
public class StateMachineConfig extends StateMachineConfigurerAdapter<DocumentState, DocumentEvent> {


    /**
     * Configures state machine with initial state and potential end states. Also configures all the states possible as
     * part of this Spring State Machine
     * @param states
     * @throws Exception
     */
    @Override
    public void configure(StateMachineStateConfigurer<DocumentState, DocumentEvent> states) throws Exception {
        states.withStates()
                .initial(DocumentState.IN_PROGRESS)
                .states(EnumSet.allOf(DocumentState.class))
                .end(DocumentState.COMPLETED)
                .end(DocumentState.CLOSED)
                .end(DocumentState.FAILED);
    }

    /**
     * Defines rules to move from one state to another at a given event. Events trigger the transition from one state
     * to another and Action methods contains business logic to determine states based on conditions
     * @param transitions
     * @throws Exception
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<DocumentState, DocumentEvent> transitions) throws Exception {
        transitions.withExternal().source(DocumentState.IN_PROGRESS).target(DocumentState.PENDING).event(DocumentEvent.CREATE)
                .and()
                .withExternal().source(DocumentState.PENDING).target(DocumentState.REVIEW).event(DocumentEvent.UPDATE)
                .and()
                .withExternal().source(DocumentState.REVIEW).target(DocumentState.REVIEW).event(DocumentEvent.SETTLE)
                .action(SettleDocumentAction())
                .and()
                .withExternal().source(DocumentState.REVIEW).target(DocumentState.COMPLETED).event(DocumentEvent.SETTLE_COMPLETE)
                .and()
                .withExternal().source(DocumentState.REVIEW).target(DocumentState.CLOSED).event(DocumentEvent.SETTLE_CLOSE)
                .and()
                .withExternal().source(DocumentState.REVIEW).target(DocumentState.FAILED).event(DocumentEvent.SETTLE_FAIL);
    }

    /**
     * Global listener configured to output whenever a state is changed. Used just for logging purposes
     * @param config
     * @throws Exception
     */
    @Override
    public void configure(StateMachineConfigurationConfigurer<DocumentState, DocumentEvent> config) throws Exception {
        StateMachineListenerAdapter<DocumentState, DocumentEvent> stateMachineConfigurerAdapter = new StateMachineListenerAdapter<DocumentState, DocumentEvent>() {
            @Override
            public void stateChanged(State from, State to) {
                log.info(String.format("State Changed from: %s, to: %s", from.getId().toString(), to.getId().toString()));
            }
        };

        config.withConfiguration().listener(stateMachineConfigurerAdapter);
    }

    /**
     * Action method to be executed before state transition for SETTLE event
     * @return context of State Machine
     */
    @Bean
    public Action<DocumentState, DocumentEvent> SettleDocumentAction() {
        return stateContext -> {
            Document document = (Document) stateContext.getExtendedState().getVariables().get("document");
            Optional.ofNullable(document).ifPresent(doc -> {
                switch(Enum.valueOf(IncomingState.class, doc.getStateToChange())) {
                    case COMPLETE:
                        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(DocumentEvent.SETTLE_COMPLETE)
                                .setHeader(DocumentServiceImpl.DOCUMENT_ID_HEADER, stateContext.getMessageHeader(DocumentServiceImpl.DOCUMENT_ID_HEADER))
                                .build());
                        break;
                    case CLOSE:
                        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(DocumentEvent.SETTLE_CLOSE)
                                .setHeader(DocumentServiceImpl.DOCUMENT_ID_HEADER, stateContext.getMessageHeader(DocumentServiceImpl.DOCUMENT_ID_HEADER))
                                .build());
                        break;
                    case FAIL:
                        stateContext.getStateMachine().sendEvent(MessageBuilder.withPayload(DocumentEvent.SETTLE_FAIL)
                                .setHeader(DocumentServiceImpl.DOCUMENT_ID_HEADER, stateContext.getMessageHeader(DocumentServiceImpl.DOCUMENT_ID_HEADER))
                                .build());
                        break;
                }
            });
        };
    }
}

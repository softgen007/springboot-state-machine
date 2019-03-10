package demo.springboot.statemachine.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import java.util.Optional;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<FeatureStates, FeatureEvents> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<FeatureStates, FeatureEvents> config) throws Exception {
        config.withConfiguration()
              .listener(listener())
              .autoStartup(true);
    }

    private StateMachineListener<FeatureStates, FeatureEvents> listener() {

        return new StateMachineListenerAdapter<FeatureStates, FeatureEvents>() {
            @Override
            public void eventNotAccepted(Message<FeatureEvents> event) {
                log.error("Not accepted event: {}", event);
            }

            @Override
            public void transition(Transition<FeatureStates, FeatureEvents> transition) {
                log.warn("MOVE from: {}, to: {}",
                         ofNullableState(transition.getSource()),
                         ofNullableState(transition.getTarget()));
            }

            private Object ofNullableState(State s) {
                return Optional.ofNullable(s)
                               .map(State::getId)
                               .orElse(null);
            }
        };
    }


    @Override
    public void configure(StateMachineStateConfigurer<FeatureStates, FeatureEvents> featureStates) throws Exception {
        featureStates.withStates()
              .initial(FeatureStates.BACKLOG, developersWakeUpAction())
              .state(FeatureStates.IN_PROGRESS)
              .state(FeatureStates.TESTING, qaWakeUpAction())
              .state(FeatureStates.DONE)
              .end(FeatureStates.DONE);
    }

    
    @Override
    public void configure(StateMachineTransitionConfigurer<FeatureStates, FeatureEvents> transitions) throws Exception {
        transitions
        		   .withExternal().source(FeatureStates.BACKLOG).target(FeatureStates.IN_PROGRESS).event(FeatureEvents.START_FEATURE)
                   .and()
                   .withExternal().source(FeatureStates.IN_PROGRESS).target(FeatureStates.TESTING).event(FeatureEvents.FINISH_FEATURE).guard(deployedToQAGuard())
                   .and()
                   .withExternal().source(FeatureStates.TESTING).target(FeatureStates.DONE).event(FeatureEvents.QA_CHECKED)
                   .and()
                   .withExternal().source(FeatureStates.TESTING).target(FeatureStates.IN_PROGRESS).event(FeatureEvents.QA_REJECTED)
                   .and()
                   .withInternal().source(FeatureStates.IN_PROGRESS).event(FeatureEvents.DEPLOY).action(deployToQA());
    }

    
    private Action<FeatureStates, FeatureEvents> developersWakeUpAction() {
        return stateContext -> log.info("Start Development");
    }

    private Action<FeatureStates, FeatureEvents> qaWakeUpAction() {
        return stateContext -> log.info("Start QA Testing");
    }

    private Action<FeatureStates, FeatureEvents> deployToQA() {
        return stateContext -> {
            log.info("Deployed to QA");
            stateContext.getExtendedState().getVariables().put("deployedToQA", true);
        };
    }
    
    private Guard<FeatureStates, FeatureEvents> deployedToQAGuard() {
        return context -> Optional.ofNullable(context.getExtendedState().getVariables().get("deployedToQA"))
                                  .map(v -> (boolean) v)
                                  .orElse(false);
    }

}

package demo.springboot.statemachine.service;

import java.util.Date;

import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.access.StateMachineAccess;
import org.springframework.statemachine.access.StateMachineFunction;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Service;

import demo.springboot.statemachine.config.FeatureEvents;
import demo.springboot.statemachine.config.FeatureStates;
import demo.springboot.statemachine.model.Feature;
import demo.springboot.statemachine.repo.FeatureRepository;
import lombok.extern.java.Log;

@Service
@Log
public class FeatureService {

	private final FeatureRepository featureRepository;
	private final StateMachineFactory<FeatureStates, FeatureEvents> factory;


	FeatureService(FeatureRepository featureRepository, StateMachineFactory<FeatureStates, FeatureEvents> factory) {
		this.featureRepository = featureRepository;
		this.factory = factory;
	}

	public Feature byId(Long id) {
		return this.featureRepository.findById(id).orElse(null);
	}

	public Feature createFeature(Date when) {
		return this.featureRepository.save(new Feature(when, FeatureStates.BACKLOG));
	}

	public Feature changeFeatureState(Long featureId, FeatureEvents featureEvent) {
		log.info("Inside chanegFeatureState");
		log.info("FeatureId: "+featureId.toString() + "FeatureEvent: "+featureEvent.toString());
		
		StateMachine<FeatureStates, FeatureEvents> sm = this.build(featureId);
		
		if (featureEvent.toString().equalsIgnoreCase("FINISH_FEATURE")) {
			sm.sendEvent(FeatureEvents.DEPLOY);;
		}
		sm.sendEvent(featureEvent);
		
		return this.featureRepository.findById(featureId).orElse(null);
	}

	private StateMachine<FeatureStates, FeatureEvents> build(Long featureId) {
		log.info("Inside State Machine Build");
		
		//Lookup feature from database
		Feature feature = this.featureRepository.findById(featureId).orElse(null);
		
		//Convert Feature Id into Key
		String featureIdKey = Long.toString(feature.getId());
		
		//Initialize new State Machine with current Feature Id
		StateMachine<FeatureStates, FeatureEvents> sm = this.factory.getStateMachine(featureIdKey);
		
		//Stop the State Machine so that Feature doesn't gets reset to initial state i.e. BACKLOG
		sm.stop();
		
		sm.getStateMachineAccessor()
				.doWithAllRegions(new StateMachineFunction<StateMachineAccess<FeatureStates,FeatureEvents>>() {
					
					@Override
					public void apply(StateMachineAccess<FeatureStates, FeatureEvents> sma) {
							
						//Add interceptor to apply and save state changes to repository
						sma.addStateMachineInterceptor(new StateMachineInterceptorAdapter<FeatureStates, FeatureEvents>() {
	
							//Persist updated Feature post state transition
							@Override
							public void postStateChange(State<FeatureStates, FeatureEvents> state, Message<FeatureEvents> message, Transition<FeatureStates, FeatureEvents> transition, StateMachine<FeatureStates, FeatureEvents> stateMachine) {
								
								Feature feature1 = featureRepository.findById(featureId).orElse(null);
								feature1.setFeatureState(state.getId());
								featureRepository.save(feature1);
							}
						});
						
					//Set the State Machine to the State of Feature current State
					sma.resetStateMachine(new DefaultStateMachineContext<FeatureStates, FeatureEvents>(feature.getFeatureState(), null, null, null));
					};
		
				
		});
		//Start State Machine after it is set to current state of Feature
		sm.start();
		return sm;
	}
}

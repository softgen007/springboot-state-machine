package demo.springboot.statemachine;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

import demo.springboot.statemachine.config.FeatureEvents;
import demo.springboot.statemachine.config.FeatureStates;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StatemachineApplicationTests {


	private StateMachine<FeatureStates, FeatureEvents> stateMachine;

	@Autowired
	private StateMachineFactory<FeatureStates, FeatureEvents> stateMachineFactory;

	@Before
	public void setUp() throws Exception {
		stateMachine = stateMachineFactory.getStateMachine();
	}

	@Test
	public void contextLoads() {
		Assertions.assertThat(stateMachine).isNotNull();
	}

	@Test
	public void initialStateTest() {
		// Asserts
		Assertions.assertThat(stateMachine.getInitialState().getId()).isEqualTo(FeatureStates.BACKLOG);
	}

	@Test
	public void firstStepTest() {
		// Act
		stateMachine.sendEvent(FeatureEvents.START_FEATURE);
		// Asserts
		Assertions.assertThat(stateMachine.getState().getId()).isEqualTo(FeatureStates.IN_PROGRESS);
	}

	@Test
	public void testPositiveFlow() {
		// Arrange
		// Act
		stateMachine.sendEvent(FeatureEvents.START_FEATURE);
		stateMachine.sendEvent(FeatureEvents.DEPLOY);
		stateMachine.sendEvent(FeatureEvents.FINISH_FEATURE);
		stateMachine.sendEvent(FeatureEvents.QA_CHECKED);
		// Asserts
		Assertions.assertThat(stateMachine.getState().getId()).isEqualTo(FeatureStates.DONE);
	}

	@Test
	public void testNegativeFlow() {
		// Arrange
		// Act
		stateMachine.sendEvent(FeatureEvents.START_FEATURE);
		stateMachine.sendEvent(FeatureEvents.QA_CHECKED);
		// Asserts
		Assertions.assertThat(stateMachine.getState().getId()).isEqualTo(FeatureStates.IN_PROGRESS);
	}

	@Test
	public void testQADeployGuard() {
		// Arrange & Act
		stateMachine.sendEvent(FeatureEvents.START_FEATURE);
		stateMachine.sendEvent(FeatureEvents.FINISH_FEATURE);
		stateMachine.sendEvent(FeatureEvents.QA_CHECKED); // not accepted!
		// Asserts
		Assertions.assertThat(stateMachine.getState().getId()).isEqualTo(FeatureStates.IN_PROGRESS);
	}

}

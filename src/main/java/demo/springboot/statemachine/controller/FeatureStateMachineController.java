package demo.springboot.statemachine.controller;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.springboot.statemachine.config.FeatureEvents;
import demo.springboot.statemachine.model.Feature;
import demo.springboot.statemachine.service.FeatureService;
import lombok.extern.java.Log;

@RestController
@RequestMapping("/statemachine")
@Log
public class FeatureStateMachineController {

	@Autowired
	private FeatureService featureService;
	
	private Feature feature;
	
	@PostMapping("/feature")
	public Feature createFeature() {
		log.info("Inside Create Feature");
		return featureService.createFeature(Calendar.getInstance().getTime());
	}
	
	@PutMapping("/feature/{featureId}")
	public Feature featureMoveNextState(@PathVariable String featureId, @RequestHeader(name="featureEvent") String featureEvent) {
		log.info("Inside Move Next State");
		log.info("FeatureId: "+featureId + "FeatureEvent: "+featureEvent);
		feature = featureService.changeFeatureState(Long.valueOf(featureId), FeatureEvents.valueOf(featureEvent.toUpperCase()));
		return feature;
	}
}

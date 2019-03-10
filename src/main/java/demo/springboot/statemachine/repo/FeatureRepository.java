package demo.springboot.statemachine.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.springboot.statemachine.model.Feature;


public interface FeatureRepository extends JpaRepository<Feature, Long>{

	
}

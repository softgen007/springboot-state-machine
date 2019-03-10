package demo.springboot.statemachine.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import demo.springboot.statemachine.config.FeatureStates;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Entity(name = "FEATURES")
public class Feature {

	@Id
	@GeneratedValue
	private Long id;
	private Date datetime;
	private String state;
	

	public Feature(Date d, FeatureStates fs) {
		this.datetime = d;
		this.setFeatureState(fs);
	}

	public FeatureStates getFeatureState() {
		return FeatureStates.valueOf(this.state);
	}

	public void setFeatureState(FeatureStates fs) {
		this.state = fs.name();
	}
}

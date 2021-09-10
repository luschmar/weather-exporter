package ch.luschmar.weatherexporter.data;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Datapoint {
	@Id
	private String key;
	private String value;
	private LocalDateTime measurement;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public LocalDateTime getMeasurement() {
		return measurement;
	}

	public void setMeasurement(LocalDateTime measurement) {
		this.measurement = measurement;
	}

}

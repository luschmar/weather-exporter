package ch.luschmar.weatherexporter.service;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "prom")
public class PromWeatherExportConfigProperties {
	private List<String> gauges;
	private List<String> counters;
	
	public List<String> getGauges() {
		return gauges;
	}
	public void setGauges(List<String> gauges) {
		this.gauges = gauges;
	}
	public List<String> getCounters() {
		return counters;
	}
	public void setCounters(List<String> counters) {
		this.counters = counters;
	}
}
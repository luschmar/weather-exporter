package ch.luschmar.weatherexporter.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Info;

@Service
public class PromWeatherExportServiceImpl implements PromWeatherExportService {

	private final Map<String, Gauge> gauges = new HashMap<>();
	private final Map<String, Counter> counters = new HashMap<>();
	private final Map<String, Info> infos = new HashMap<>();
	private final Map<String, Double> corrections;
	private final CollectorRegistry collectorRegistry;

	@Autowired
	public PromWeatherExportServiceImpl(CollectorRegistry collectorRegistry,
			PromWeatherExportConfigProperties properties) {
		this.collectorRegistry = collectorRegistry;
		properties.getGauges().stream().forEach(p -> createAndRegisterGauge(p));
		properties.getCounters().stream().forEach(p -> counters.put(p, createAndRegisterCounter(p)));
		
		corrections = properties.getCorrections().stream().collect(Collectors.toMap(CorrectionProperty::getName, CorrectionProperty::getValue));
	}

	private void createAndRegisterGauge(String name) {
		if(isTempName(name)) {
			var nameWithoutUnit = tempNameWithoutUnit(name);
			gauges.put(nameWithoutUnit+"_fahrenheit", Gauge.build(nameWithoutUnit, name).unit("fahrenheit")
					.register(collectorRegistry));
			gauges.put(nameWithoutUnit+"_celsius", Gauge.build(nameWithoutUnit, name).unit("celsius")
					.register(collectorRegistry));
		} else if(isPressureName(name)) {
			var nameWithoutUnit = pressureNameWithoutUnit(name);
			gauges.put(nameWithoutUnit+"_inhg", Gauge.build(nameWithoutUnit, name).unit("inHg")
					.register(collectorRegistry));
			gauges.put(nameWithoutUnit+"_hpa", Gauge.build(nameWithoutUnit, name).unit("hPa")
					.register(collectorRegistry));
		} else if(isSpeedName(name)) {
			var nameWithoutUnit = speedNameWithoutUnit(name);
			gauges.put(nameWithoutUnit+"_mph", Gauge.build(nameWithoutUnit, name).unit("mph")
					.register(collectorRegistry));
			gauges.put(nameWithoutUnit+"_kmh", Gauge.build(nameWithoutUnit, name).unit("kmh")
					.register(collectorRegistry));
		} else if(isRainInName(name)) {
			var nameWithoutUnit = rainInNameWithoutUnit(name);
			gauges.put(nameWithoutUnit+"_inch", Gauge.build(nameWithoutUnit, name).unit("inch")
					.register(collectorRegistry));
			gauges.put(nameWithoutUnit+"_mm", Gauge.build(nameWithoutUnit, name).unit("mm")
					.register(collectorRegistry));
		} else {
			gauges.put(name, Gauge.build(name, name)
					.register(collectorRegistry));
		}
	}


	private Counter createAndRegisterCounter(String name) {
		return Counter.build(name, name).register(collectorRegistry);
	}
	
	private Info createAndRegisterInfo(String name) {
		return Info.build(name, name).register(collectorRegistry);
	}

	public void processData(Map<String, String> dataParameter) {
		// GAUGES
		//dataParameter.entrySet().stream().filter(e -> gauges.containsKey(e)).peek(p -> {
		//}).collect(Collectors.toList());
		dataParameter.entrySet().forEach(kv -> {
			var name = kv.getKey();
			if(isTempName(name)) {
				var doubleValue = processCorrection(name, Double.valueOf(kv.getValue()));
				var nameWithoutUnit = tempNameWithoutUnit(name);
				gauges.get(nameWithoutUnit+"_fahrenheit").set(doubleValue);
				gauges.get(nameWithoutUnit+"_celsius").set(calculateFahrenheitToCelcius(doubleValue));
			} else if(isPressureName(name)) {
				var doubleValue = processCorrection(name, Double.valueOf(kv.getValue()));
				var nameWithoutUnit = pressureNameWithoutUnit(name);
				gauges.get(nameWithoutUnit+"_inhg").set(doubleValue);
				gauges.get(nameWithoutUnit+"_hpa").set(calculateInhgToHpa(doubleValue));
			} else if(isSpeedName(name)) {
				var doubleValue = processCorrection(name, Double.valueOf(kv.getValue()));
				var nameWithoutUnit = speedNameWithoutUnit(name);
				gauges.get(nameWithoutUnit+"_mph").set(doubleValue);
				gauges.get(nameWithoutUnit+"_kmh").set(calculateMphToKmh(doubleValue));
			} else if(isRainInName(name)) {
				var doubleValue = processCorrection(name, Double.valueOf(kv.getValue()));
				var nameWithoutUnit = rainInNameWithoutUnit(name);
				gauges.get(nameWithoutUnit+"_inch").set(doubleValue);
				gauges.get(nameWithoutUnit+"_mm").set(calculateInchToMm(doubleValue));
			} else {
				if(gauges.containsKey(name)) {
					var doubleValue = processCorrection(name, Double.valueOf(kv.getValue()));
					gauges.get(name).set(doubleValue);
				} else if(counters.containsKey(name)) {
					var doubleValue = processCorrection(name, Double.valueOf(kv.getValue()));
					counters.get(name).inc(doubleValue);
				} else if(infos.containsKey(name)) {
					infos.get(name).info(name, kv.getValue());
				} else {
					infos.put(name, createAndRegisterInfo(name));
					infos.get(name).info(name, kv.getValue());
				}
			}
		});
	}

	private Double processCorrection(String name, Double valueOf) {
		if(corrections.containsKey(name)) {
			var correctedValue = Double.sum(valueOf, corrections.get(name));
			if("winddir".equals(name) && correctedValue < 0)
			{
				correctedValue = Double.sum(correctedValue, 360);
			}
			if("winddir".equals(name) && correctedValue > 360)
			{
				correctedValue = Double.sum(correctedValue, -360);
			}
			return correctedValue;
		}
		return valueOf;
	}

	private boolean isTempName(String name) {
		return name.endsWith("tempf") || name.endsWith("dewptf") || name.endsWith("chillf");
	}
	
	private String tempNameWithoutUnit(String name) {
		return name.substring(0, name.length()-1);
	}
	
	private boolean isSpeedName(String name) {
		return name.endsWith("mph");
	}
	
	private String speedNameWithoutUnit(String name) {
		return name.substring(0, name.length()-3);

	}
	
	private boolean isPressureName(String name) {
		return name.endsWith("baromin");
	}
	
	private String pressureNameWithoutUnit(String name) {
		return name.substring(0, name.length()-2);
	}
	
	private boolean isRainInName(String name) {
		return name.endsWith("rainin");
	}
	
	private String rainInNameWithoutUnit(String name) {
		return name.substring(0, name.length()-2);
	}
	
	
	
	private static final BigDecimal INCH_2_MM_FACTOR = BigDecimal.valueOf(25.4);
	/**
	 * d(mm) = d(inch) × 25.4 
	 * 
	 * @param doubleValue
	 * @return
	 */
	private double calculateInchToMm(Double doubleValue) {
		return BigDecimal.valueOf(doubleValue).multiply(INCH_2_MM_FACTOR).setScale(0, RoundingMode.HALF_EVEN).doubleValue();
	}

	private static final BigDecimal MPH_2_KMH_FACTOR = BigDecimal.valueOf(1.609344);
	/**
	 * km/h = mph x 1.609344
	 * @param doubleValue
	 * @return
	 */
	private double calculateMphToKmh(Double doubleValue) {
		return BigDecimal.valueOf(doubleValue).multiply(MPH_2_KMH_FACTOR).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
	}

	private static final BigDecimal INCH_2_HPA_FACTOR = BigDecimal.valueOf(33.86389);
	/**
	 * Pressure(hPa) = Pressure (inHg) × 33.86389
	 * 
	 * @param doubleValue
	 * @return
	 */
	private double calculateInhgToHpa(double doubleValue) {
		return new BigDecimal(doubleValue).multiply(INCH_2_HPA_FACTOR).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
	}

	private static final BigDecimal FAHRENHEIT_2_CELSIUS_CONV_SUBSTRACT = BigDecimal.valueOf(32);
	private static final BigDecimal FAHRENHEIT_2_CELSIUS_CONV_FACTOR = BigDecimal.valueOf(0.555555555556);
	/**
	 *  Celsius (°C) = (Fahrenheit - 32) / 1.8
	 *  
	 * @param doubleValue
	 * @return
	 */
	private double calculateFahrenheitToCelcius(double doubleValue) {
		return (BigDecimal.valueOf(doubleValue).subtract(FAHRENHEIT_2_CELSIUS_CONV_SUBSTRACT)).multiply(FAHRENHEIT_2_CELSIUS_CONV_FACTOR).setScale(1, RoundingMode.HALF_EVEN).doubleValue();
	}
}

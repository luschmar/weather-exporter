package ch.luschmar.weatherexporter.input;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import ch.luschmar.weatherexporter.data.Datapoint;
import ch.luschmar.weatherexporter.data.DatapointRepository;
import ch.luschmar.weatherexporter.service.PromWeatherExportService;

@RestController
public class WeatherInputController {
    private Logger logger = LoggerFactory.getLogger(WeatherInputController.class);
    
	private final DatapointRepository datapointRepository;

	private final PromWeatherExportService promWeatherExportService;

	@Autowired
	public WeatherInputController(DatapointRepository datapointRepository, PromWeatherExportService promWeatherExportService) {
		this.datapointRepository = datapointRepository;
		this.promWeatherExportService = promWeatherExportService;
	}

	@GetMapping("/input")
	public void input(@RequestParam Map<String,String> dataParameter) {
		var oldData = datapointRepository.findAllById(dataParameter.keySet());
		var oldKeys = oldData.stream().map(Datapoint::getKey).collect(Collectors.toList());
		var newData = dataParameter.entrySet().stream().filter(e -> !oldKeys.contains(e.getKey())).map(e -> {
			var newDatapoint = new Datapoint();
			newDatapoint.setKey(e.getKey());
			newDatapoint.setValue(e.getValue());
			newDatapoint.setMeasurement(LocalDateTime.now());
			return newDatapoint;
		}).collect(Collectors.toList());
		var updatedData = oldData.stream().map(o -> {
			o.setValue(dataParameter.get(o.getKey()));
			o.setMeasurement(LocalDateTime.now());
			return o;
		}).collect(Collectors.toList());
				
		datapointRepository.saveAllAndFlush(newData);
		datapointRepository.saveAllAndFlush(updatedData);
		
		promWeatherExportService.processData(dataParameter);
	}
}

package ch.luschmar.weatherexporter.input;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.luschmar.weatherexporter.service.PromWeatherExportService;

@RestController
public class WeatherInputController {
    private Logger logger = LoggerFactory.getLogger(WeatherInputController.class);

	private final PromWeatherExportService promWeatherExportService;

	@Autowired
	public WeatherInputController(PromWeatherExportService promWeatherExportService) {
		this.promWeatherExportService = promWeatherExportService;
	}

	@GetMapping("/input")
	public void input(@RequestParam Map<String,String> dataParameter) {
		logger.debug("input ", dataParameter);
		promWeatherExportService.processData(dataParameter);
	}
}

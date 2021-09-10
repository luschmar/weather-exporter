package ch.luschmar.weatherexporter.service;

import java.util.Map;

public interface PromWeatherExportService {
	public void processData(Map<String, String> dataParameter) ;
}

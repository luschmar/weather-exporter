package ch.luschmar.weatherexporter.service;

public class MesurementConfigProperty {
	private String name;
	private String description;
	private String unit;
	private String converter;
	private String converterTargetName;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getConverter() {
		return converter;
	}
	public void setConverter(String converter) {
		this.converter = converter;
	}
	public String getConverterTargetName() {
		return converterTargetName;
	}
	public void setConverterTargetName(String converterTargetName) {
		this.converterTargetName = converterTargetName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	
}

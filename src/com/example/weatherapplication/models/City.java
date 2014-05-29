package com.example.weatherapplication.models;

public class City {
	private String name = "Plymouth";
	private String city = "Plymouth";
	private String state = "CA";
	private String country = "US";
	private String country_iso3166 = "US";
	private String country_name = "USA";
	private String zmw = "95669.1.99999";
	private String l = "/q/zmw=95669.1.99999";
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getCity() {
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	public String getCountry() {
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getCountry_iso3166() {
		return country_iso3166;
	}
	
	public void setCountry_iso3166(String country_iso3166) {
		this.country_iso3166 = country_iso3166;
	}
	
	public String getCountry_name() {
		return country_name;
	}
	
	public void setCountry_name(String country_name) {
		this.country_name = country_name;
	}
	
	public String getZmw() {
		return zmw;
	}
	
	public void setZmw(String zmw) {
		this.zmw = zmw;
	}
	
	public String getL() {
		return l;
	}
	
	public void setL(String l) {
		this.l = l;
	}
}
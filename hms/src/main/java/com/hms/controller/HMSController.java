package com.hms.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.DoctorDTO;
import com.hms.model.Doctor;
import com.hms.model.Hospital;
import com.hms.model.Invoice;
import com.hms.model.Patient;
import com.hms.service.HMSService;

@RestController
@RequestMapping("/hms")
public class HMSController {
	
	private final HMSService service;

	public HMSController(HMSService service) {
		this.service = service;
	}
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	String home() {
		return "<h1>Welcome to Hospital Management System</h1>";
	}
	
	//Hospital
	
	@RequestMapping(value = "/hospital", method = RequestMethod.GET)
	List<Hospital> getHospital(){
		return service.getHospital();
	}
	
	//Doctors
	
	@RequestMapping(value = "/doctors", method = RequestMethod.GET)
	List<Doctor> showAllDoctors(){
		List<Doctor> listAllDoctors= new ArrayList<>();
		listAllDoctors=service.showAllDoctors();
		return listAllDoctors;
	}
	
	@RequestMapping(value = "/doctors/viewByName/{name}", method = RequestMethod.GET)
	List<Map<String, Object>> doctorViewByName(@PathVariable("name") String name ) {
		return service.searchDoctorByName(name);
	}
	
	@RequestMapping(value = "/doctors/viewById/{id}", method = RequestMethod.GET)
	Doctor doctorViewById(@PathVariable("id") long id) {
		return service.searchDoctorById(id);
	}
	
	@RequestMapping(value="/doctors/addDoctor",method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	void addDoctor(@RequestBody DoctorDTO doctorDto) throws Exception{
		service.createDoctor(doctorDto.toEntity());
	}
	
	@RequestMapping(value="/doctors/updateDoctor/{id}",method=RequestMethod.PUT,consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value=HttpStatus.OK)
	void updateDoctor(@PathVariable("id") long id, @RequestBody DoctorDTO doctorDto) throws Exception{
		Doctor doctor = doctorDto.toEntity();
		doctor.setId(id);
		service.updateDoctor(doctor);
	}
	
	@RequestMapping(value="/doctors/deleteDoctor/{id}",method=RequestMethod.DELETE)
	@ResponseStatus(value=HttpStatus.OK)
	void deleteDoctor(@PathVariable("id") long id)throws Exception{
		service.deleteDoctor(id);
	}
	//Patients
	
	@RequestMapping(value = "/patients", method = RequestMethod.GET)
	List<Patient> showAllPatients(){
		List<Patient> listAllPatients= new ArrayList<Patient>();
		listAllPatients=service.showAllPatients();
		return listAllPatients;
	}
	
	@RequestMapping(value = "/patients/viewByName/{name}", method = RequestMethod.GET)
	List<Map<String, Object>> patientViewByName(@PathVariable("name") String name ) {
		return service.searchPatientByName(name);
	}
	
	@RequestMapping(value = "/patients/viewById/{id}", method = RequestMethod.GET)
	Patient patientViewById(@PathVariable("id") long id) {
		return service.searchPatientById(id);
	}
	
	@RequestMapping(value="/patients/addPatient",method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	void addPatient(@RequestBody Patient patient) throws Exception{
		service.createPatient(patient);
	}
	
	@RequestMapping(value="/patients/updatePatient/{id}",method=RequestMethod.PUT,consumes=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(value=HttpStatus.OK)
	void updatePatient(@PathVariable("id") long id, @RequestBody Patient patient) throws Exception{
		patient.setId(id);
		service.updatePatient(patient);
	}
	
	@RequestMapping(value="/patients/deletePatient/{id}",method=RequestMethod.DELETE)
	@ResponseStatus(value=HttpStatus.OK)
	void deletePatient(@PathVariable("id") long id)throws Exception{
		service.deletePatient(id);
	}
	//Invoice 
	
	@RequestMapping(value = "/invoice", method = RequestMethod.GET)
	List<Invoice> showinvoice(){
		return service.showInvoice();
	}
	
}

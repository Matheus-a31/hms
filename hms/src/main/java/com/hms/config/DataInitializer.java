package com.hms.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hms.model.Appointment;
import com.hms.model.AppointmentStatus;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Hospital;
import com.hms.model.Invoice;
import com.hms.model.Patient;
import com.hms.model.UserAccount;
import com.hms.model.UserPreference;
import com.hms.model.UserRole;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.HospitalRepository;
import com.hms.repository.InvoiceRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserAccountRepository;
import com.hms.repository.UserPreferenceRepository;

@Configuration
public class DataInitializer {

	@Value("${hms.demo.admin-code:admin123}")
	private String demoAdminCode;

	@Value("${hms.demo.patient-code:paciente123}")
	private String demoPatientCode;

	@Bean
	public CommandLineRunner seedDatabase(
			HospitalRepository hospitalRepository,
			DoctorRepository doctorRepository,
			PatientRepository patientRepository,
			BillRepository billRepository,
			InvoiceRepository invoiceRepository,
			AppointmentRepository appointmentRepository,
			UserAccountRepository userAccountRepository,
			UserPreferenceRepository userPreferenceRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			if (hospitalRepository.count() > 0) {
				return;
			}
			Hospital hospital = hospitalRepository.save(new Hospital(
					"Hospital Central de Fortaleza",
					"Av. Universidade, 2853 - Benfica, Fortaleza-CE",
					"www.hospitalsanatorio.com.br",
					8533669000L));
			List<Doctor> doctors = seedDoctors(doctorRepository, hospital);
			List<Patient> patients = seedPatients(patientRepository, doctors);
			List<Bill> bills = seedBills(billRepository);
			seedInvoices(invoiceRepository, patients, doctors, bills);
			seedAppointments(appointmentRepository, patients, doctors, hospital);
			seedUsers(userAccountRepository, userPreferenceRepository, passwordEncoder, patients.get(0));
		};
	}

	private List<Doctor> seedDoctors(DoctorRepository repo, Hospital hospital) {
		return List.of(
				repo.save(buildDoctor("Dr. Roberto Silva", "Cardiologista / Chefe", "MD Cardiologia", 12,
						"Av. Beira Mar, 1200 - Fortaleza-CE", "roberto@hms.com", 85999112233L, hospital)),
				repo.save(buildDoctor("Dra. Ana Costa", "Pediatra / Residente", "MBBS Pediatria", 6,
						"Rua Monsenhor Tabosa, 410 - Fortaleza-CE", "ana.costa@hms.com", 85999445566L, hospital)),
				repo.save(buildDoctor("Dr. Fernando Ribeiro", "Médico Efetivo / Plantonista", "Neurologia Clínica", 8,
						"Rua das Flores, 120 - Aldeota, Fortaleza-CE", "fernando.ribeiro@hms.com", 85999887766L, hospital)));
	}

	private Doctor buildDoctor(String name, String designation, String degree, int exp,
			String address, String email, long phone, Hospital hospital) {
		Doctor doctor = new Doctor();
		doctor.setName(name);
		doctor.setDesignation(designation);
		doctor.setDegree(degree);
		doctor.setExperience(exp);
		doctor.setAddress(address);
		doctor.setEmailId(email);
		doctor.setContactNumber(phone);
		doctor.setHospital(hospital);
		return doctor;
	}

	private List<Patient> seedPatients(PatientRepository repo, List<Doctor> doctors) {
		return List.of(
				repo.save(buildPatient("Carlos Eduardo", "Hipertensão", "Masculino", "carlos@email.com",
						85988112233L, "Rua das Flores, 120 - Aldeota, Fortaleza-CE", doctors.get(0))),
				repo.save(buildPatient("Maria Oliveira", "Arritmia Cardíaca", "Feminino", "maria@email.com",
						85988223344L, "Av. Santos Dumont, 950 - Aldeota, Fortaleza-CE", doctors.get(1))),
				repo.save(buildPatient("João Pedro", "Enxaqueca Crônica", "Masculino", "joao@email.com",
						85988334455L, "Rua Barão de Studart, 500 - Fortaleza-CE", doctors.get(2))));
	}

	private Patient buildPatient(String name, String disease, String gender, String email,
			long phone, String address, Doctor doctor) {
		Patient patient = new Patient();
		patient.setName(name);
		patient.setDisease(disease);
		patient.setGender(gender);
		patient.setEmailId(email);
		patient.setContactNumber(phone);
		patient.setAddress(address);
		patient.setDoctor(doctor);
		return patient;
	}

	private List<Bill> seedBills(BillRepository repo) {
		return List.of(
				repo.save(new Bill("Diária de UTI / Leito 04", 3, 500)),
				repo.save(new Bill("Ressonância Magnética", 1, 850)),
				repo.save(new Bill("Aplicação de Medicamento IV", 5, 40)),
				repo.save(new Bill("Consulta Especializada", 1, 450)));
	}

	private void seedInvoices(InvoiceRepository repo, List<Patient> patients, List<Doctor> doctors, List<Bill> bills) {
		repo.save(buildInvoice(patients.get(0), doctors.get(0), bills.get(3), 450, 10, "PAGO"));
		repo.save(buildInvoice(patients.get(1), doctors.get(1), bills.get(1), 850, 15, "PAGO"));
		repo.save(buildInvoice(patients.get(0), doctors.get(0), bills.get(0), 1500, 20, "PENDENTE"));
		repo.save(buildInvoice(patients.get(2), doctors.get(2), bills.get(2), 200, 7, "PAGO"));
	}

	private Invoice buildInvoice(Patient patient, Doctor doctor, Bill bill, int total, int plusDays, String status) {
		Invoice invoice = new Invoice();
		invoice.setPatient(patient);
		invoice.setDoctor(doctor);
		invoice.setBill(bill);
		invoice.setTotalAmount(total);
		invoice.setInvoiceType("PACIENTE");
		invoice.setDueDate(LocalDate.now().plusDays(plusDays));
		invoice.setPaymentStatus(status);
		return invoice;
	}

	private void seedAppointments(AppointmentRepository repo, List<Patient> patients, List<Doctor> doctors, Hospital hospital) {
		LocalDate today = LocalDate.now();
		repo.save(buildAppointment(patients.get(0), doctors.get(0), hospital, "Cardiologia Clínica", today.atTime(9, 0), AppointmentStatus.CONFIRMADO, 450));
		repo.save(buildAppointment(patients.get(1), doctors.get(0), hospital, "Cardiologia Clínica", today.atTime(10, 30), AppointmentStatus.CONCLUIDO, 520));
		repo.save(buildAppointment(patients.get(1), doctors.get(1), hospital, "Pediatria Geral", today.atTime(14, 0), AppointmentStatus.CONCLUIDO, 380));
		repo.save(buildAppointment(patients.get(2), doctors.get(2), hospital, "Neurologia Clínica", today.atTime(15, 30), AppointmentStatus.AGENDADO, 480));
	}

	private Appointment buildAppointment(Patient patient, Doctor doctor, Hospital hospital,
			String specialty, LocalDateTime dateTime, AppointmentStatus status, int fee) {
		Appointment appointment = new Appointment();
		appointment.setPatient(patient);
		appointment.setDoctor(doctor);
		appointment.setHospital(hospital);
		appointment.setSpecialty(specialty);
		appointment.setAppointmentDate(dateTime);
		appointment.setStatus(status);
		appointment.setConsultationFee(fee);
		return appointment;
	}

	private void seedUsers(UserAccountRepository userRepo, UserPreferenceRepository prefRepo,
			PasswordEncoder encoder, Patient defaultPatient) {
		saveUserWithPrefs(userRepo, prefRepo, encoder, "Administrador HMS", "admin@hms.com", demoAdminCode, UserRole.ADMIN, null);
		saveUserWithPrefs(userRepo, prefRepo, encoder, "Carlos Eduardo", "paciente@hms.com", demoPatientCode, UserRole.PACIENTE, defaultPatient);
	}

	private void saveUserWithPrefs(UserAccountRepository userRepo, UserPreferenceRepository prefRepo,
			PasswordEncoder encoder, String name, String email, String rawCode, UserRole role, Patient patient) {
		UserAccount user = new UserAccount();
		user.setName(name);
		user.setEmail(email);
		user.setPasswordHash(encoder.encode(rawCode));
		user.setRole(role);
		user.setPatient(patient);
		user = userRepo.save(user);

		UserPreference pref = new UserPreference();
		pref.setUser(user);
		pref.setHighContrast(false);
		pref.setFontSize("normal");
		pref.setEnhancedFocus(true);
		prefRepo.save(pref);
	}
}

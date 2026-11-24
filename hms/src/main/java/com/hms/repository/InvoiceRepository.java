package com.hms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hms.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
	List<Invoice> findByDoctorId(long doctorId);
	List<Invoice> findByPatientId(long patientId);
	List<Invoice> findByBillId(long billId);

	@Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.doctor.id = :doctorId")
	Long sumTotalAmountByDoctorId(@Param("doctorId") long doctorId);
}

package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.stream.Stream;


@EnableDiscoveryClient
@SpringBootApplication
public class PaymentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}
}

@Component
class paymentCommandLineRunner implements CommandLineRunner {

	private PaymentRepository paymentRepository;

	@Autowired
	public paymentCommandLineRunner(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		Stream.of("12345,67890,100.0","67890,12345,100.0","124578,235689,100.0")
				.map(s -> s.split(","))
				.forEach(strings -> paymentRepository.save(new payment(strings[0],strings[1],new BigDecimal(strings[2]))));

		paymentRepository.findAll().forEach(System.out::println);
	}
}

@RepositoryRestResource
interface PaymentRepository extends JpaRepository<payment, Long> {

	@RestResource(path = "by-name")
	Collection<payment> findByFromAccount(@Param("fa") String fromAccount);
}

@RefreshScope
@RestController
class messageController {
	@Value("${message}")
	private String message;

	@RequestMapping(value = "/message")
	String getMessage(){
		return this.message;
	}
}



@RestController
class paymentController {

	@Autowired
	private PaymentRepository paymentRepository;

	@PostMapping(value = "/payment")
	payment performPayment(@RequestBody payment payment) {
		System.out.println(payment.getFromAccount() + " " + payment.getToAccount() + " " + payment.getAmount());
		return this.paymentRepository.save(payment);
	}

	@GetMapping(value = "/searchByFromAccount")
	Collection<payment> searchbyFromAccount(@RequestBody String fromAccount) {
		return this.paymentRepository.findByFromAccount(fromAccount);
	}

	@GetMapping(value = "/getPayments")
	Collection<payment> getPayments() {
		return this.paymentRepository.findAll();
	}
}

@Entity
class payment {

	@Id
	@GeneratedValue
	private Long id;

	@NotNull
	private String fromAccount;
	@NotNull
	private String toAccount;
	@NotNull
	private BigDecimal amount;

	public payment() {

	}

	public payment(String fromAccount, String toAccount, BigDecimal amount) {
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "payment{" +
				"id=" + id +
				", fromAccount='" + fromAccount + '\'' +
				", toAccount='" + toAccount + '\'' +
				", amount=" + amount +
				'}';
	}
}

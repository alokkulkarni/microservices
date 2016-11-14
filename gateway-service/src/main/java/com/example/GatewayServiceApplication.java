package com.example;


import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableDiscoveryClient
@EnableZuulProxy
@EnableCircuitBreaker
@SpringBootApplication
public class GatewayServiceApplication {

	@LoadBalanced
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}
}

@RestController
class makePayment {

	RestTemplate restTemplate;

	@Autowired
	public makePayment(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	ResponseEntity<List<payment>> makePaymentFallbackMethod() {
		List<payment> paymentsList = new ArrayList<payment>();
		paymentsList.add(new payment(1L,"123456","45678","100"));
		paymentsList.add(new payment(1L,"123456","45678","100"));
		paymentsList.add(new payment(1L,"123456","45678","100"));
		paymentsList.add(new payment(1L,"123456","45678","100"));
		return new ResponseEntity<List<payment>>(paymentsList, HttpStatus.OK);
	}

	@PostMapping(value = "/payment")
	ResponseEntity<payment> makeAPayment(@Valid @RequestBody payment payment) {


		Map<String, Object> paymentMap = new HashMap<String, Object>();

		paymentMap.put("fromAccount",payment.getFromAccount());
		paymentMap.put("toAccount", payment.getToAccount());
		paymentMap.put("amount",payment.getAmmount());

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, Object>> paymentHTTPEntity = new HttpEntity<Map<String, Object>>(paymentMap, httpHeaders);

		payment payment1 = this.restTemplate.postForObject("http://payment-service/payment",paymentHTTPEntity,payment.getClass());

		if (payment1.getId() != null) {
			return ResponseEntity.ok()
								.headers(HeaderUtil.createEntityCreationAlert("Payment", payment1.getId().toString()))
								.body(payment1);
		}

		return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("Payment", "BadRequest", "Mandatory PArameters cannot be null")).body(null);
	}

	@GetMapping(value = "/getP")
	@HystrixCommand(fallbackMethod = "makePaymentFallbackMethod")
	ResponseEntity<List<payment>> getPayments() {
		return new ResponseEntity<List<payment>>(this.restTemplate.getForObject("http://payment-service/getPayments",List.class), HttpStatus.OK);
	}
}

ResquestEntity ()
ResponseEntity()

class payment {
	private Long id;

	@NotNull
	private String fromAccount;
	@NotNull
	private String toAccount;
	@NotNull
	private BigDecimal ammount;

	public payment() {
	}

	public payment(String fromAccount, String toAccount, String ammount) {
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.ammount = new BigDecimal(ammount);
	}

	public payment(Long id, String fromAccount, String toAccount, String amount) {
		this.id = id;
		this.fromAccount = fromAccount;
		this.toAccount = toAccount;
		this.ammount = new BigDecimal(amount);
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

	public BigDecimal getAmmount() {
		return ammount;
	}

	public void setAmmount(BigDecimal ammount) {
		this.ammount = ammount;
	}

	@Override
	public String toString() {
		return "payment{" +
				"id=" + id +
				", fromAccount='" + fromAccount + '\'' +
				", toAccount='" + toAccount + '\'' +
				", ammount=" + ammount +
				'}';
	}
}

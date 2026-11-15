package com.BookMyShow.bookmyshow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:bookmyshow-test;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"app.admin.email=",
		"app.admin.password=",
		"razorpay.key.id=",
		"razorpay.key.secret="
})
class BookmyshowApplicationTests {

	@Test
	void contextLoads() {
	}

}

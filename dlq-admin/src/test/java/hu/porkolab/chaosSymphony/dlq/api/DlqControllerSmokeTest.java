package hu.porkolab.chaosSymphony.dlq.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DlqControllerSmokeTest {

	@Autowired
	private DlqController dlqController;

	@Test
	void contextLoadsAndControllerIsNotNull() {
		// Ellenőrizd, hogy a kontroller valóban létrejött és nem null.
		// Ha ez a teszt sikeres, a Spring kontextus garantáltan helyesen épült fel.
		assertThat(dlqController).isNotNull();
	}
}
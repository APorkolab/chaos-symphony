package hu.porkolab.chaosSymphony.orchestrator.idemp;

import hu.porkolab.chaosSymphony.common.idemp.IdempotencyStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JdbcIdempotencyStore implements IdempotencyStore {
	private final JdbcTemplate jdbc;

	public JdbcIdempotencyStore(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	public boolean markIfFirst(String eventId) {
		try {
			int rows = jdbc.update("INSERT INTO idempotency_event(event_id) VALUES (?)", eventId);
			return rows == 1;
		} catch (Exception dup) {
			return false;
		}
	}
}

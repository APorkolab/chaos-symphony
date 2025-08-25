package hu.porkolab.chaosSymphony.common.idemp;

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
            // Attempt to insert the event ID. If it's a duplicate, the PRIMARY KEY constraint will fail.
            int rows = jdbc.update("INSERT INTO idempotency_event(event_id) VALUES (?)", eventId);
            return rows == 1;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // This exception is expected for duplicate entries.
            return false;
        }
    }
}

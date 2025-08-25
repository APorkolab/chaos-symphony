package hu.porkolab.chaosSymphony.common.idemp;

public class NoopIdempotencyStore implements IdempotencyStore {
	@Override
	public boolean markIfFirst(String eventId) {
		return true;
	}
}

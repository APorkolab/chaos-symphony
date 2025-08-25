package hu.porkolab.chaosSymphony.chaos.core;

import hu.porkolab.chaosSymphony.common.chaos.ChaosRules;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class RulesStore {

	private final AtomicReference<Map<String, ChaosRules.Rule>> ref = new AtomicReference<>(Collections.emptyMap());

	public Map<String, ChaosRules.Rule> get() {
		return ref.get();
	}

	public Map<String, ChaosRules.Rule> set(Map<String, ChaosRules.Rule> newRules) {
		ref.set(newRules == null ? Collections.emptyMap() : newRules);
		return ref.get();
	}
}

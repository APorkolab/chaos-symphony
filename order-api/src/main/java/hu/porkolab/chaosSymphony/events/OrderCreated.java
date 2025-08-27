package hu.porkolab.chaosSymphony.events;

/**
 * Simplified stand-in for the Avro-generated OrderCreated event.
 * Provides a minimal builder API compatible with existing service code
 * and getters/setters so Jackson can serialise/deserialise it in tests.
 */
public class OrderCreated {
	private String orderId;
	private double total;
	private String currency;
	private String customerId;

	public OrderCreated() {
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public double getTotal() {
		return total;
	}

	public void setTotal(double total) {
		this.total = total;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private final OrderCreated instance = new OrderCreated();

		public Builder setOrderId(String orderId) {
			instance.setOrderId(orderId);
			return this;
		}

		public Builder setTotal(double total) {
			instance.setTotal(total);
			return this;
		}

		public Builder setCurrency(String currency) {
			instance.setCurrency(currency);
			return this;
		}

		public Builder setCustomerId(String customerId) {
			instance.setCustomerId(customerId);
			return this;
		}

		public OrderCreated build() {
			return instance;
		}
	}
}
package hu.porkolab.chaosSymphony.common;

public class EventEnvelope {
    private String eventId;
    private String orderId;
    private String type;
    private String payload;

    public EventEnvelope() { }

    public EventEnvelope(String eventId, String orderId, String type, String payload) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.type = type;
        this.payload = payload;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}

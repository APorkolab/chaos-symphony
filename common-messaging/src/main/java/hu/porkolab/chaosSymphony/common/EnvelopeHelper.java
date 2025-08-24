package hu.porkolab.chaosSymphony.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

public final class EnvelopeHelper {
  private static final ObjectMapper OM = new ObjectMapper();
  private EnvelopeHelper() {}

  public static String envelope(String orderId, String type, String payloadJson) {
    EventEnvelope ev = new EventEnvelope(UUID.randomUUID().toString(), orderId, type, payloadJson);
    try { return OM.writeValueAsString(ev); }
    catch (Exception e) { throw new RuntimeException("Envelope serialize error", e); }
  }

  public static EventEnvelope parse(String json) {
    try { return OM.readValue(json, EventEnvelope.class); }
    catch (Exception e) { throw new RuntimeException("Envelope parse error", e); }
  }
}

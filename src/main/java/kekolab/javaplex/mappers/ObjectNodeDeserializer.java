package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class ObjectNodeDeserializer<T> extends StdDeserializer<T> {

	protected ObjectNodeDeserializer(Class<T> vc) {
		super(vc);
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		ObjectMapper objectMapper = (ObjectMapper) p.getCodec();
		boolean unwrapEnabled = objectMapper.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE);
		if (unwrapEnabled)
			objectMapper.disable(DeserializationFeature.UNWRAP_ROOT_VALUE);
		ObjectNode n = objectMapper.readTree(p);
		try {
			Class<? extends T> targetClass = chooseDeserializingClass(n);
			return p.getCodec().treeToValue(n, targetClass);
		} catch (IOException e) {
			throw new JsonParseException(p, "Cannot determine the class to deserialize mediatag to", e);
		} finally {
			if (unwrapEnabled)
				objectMapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
		}
	}

	private void ensureFieldExistence(ObjectNode n, String fieldName) throws IOException {
		if (!n.has(fieldName))
			throw new IOException(
					"Cannot determine the right class to deserialize object. Object has no `" + fieldName
							+ "´ attribute");
	}

	protected String extractTextFieldValueFromObjectNode(ObjectNode n, String fieldName) throws IOException {
		ensureFieldExistence(n, fieldName);
		return n.get(fieldName).textValue().toLowerCase();
	}

	protected int extractIntegerFieldValueFromObjectNode(ObjectNode n, String fieldName) throws IOException {
		ensureFieldExistence(n, fieldName);
		return n.get(fieldName).intValue();
	}

	protected boolean extractBooleanFieldValueFromObjectNode(ObjectNode n, String fieldName) throws IOException {
		ensureFieldExistence(n, fieldName);
		return n.get(fieldName).booleanValue();
	}

	protected abstract Class<? extends T> chooseDeserializingClass(ObjectNode node) throws IOException;
}

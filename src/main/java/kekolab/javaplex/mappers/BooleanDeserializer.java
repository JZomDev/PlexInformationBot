package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class BooleanDeserializer extends StdDeserializer<Boolean> {
	private static final long serialVersionUID = -2434816674619514508L;

	public BooleanDeserializer() {
		super(Boolean.class);
	}

	@Override
	public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
		String value = p.getText();
		if (value == null)
			return null;
		if (value.equals("0") || value.isEmpty())
			return Boolean.FALSE;
		if (value.equals("1"))
			return Boolean.TRUE;
		throw new JsonParseException(p, "Cannot map " + value + " to Boolean");
	}

}

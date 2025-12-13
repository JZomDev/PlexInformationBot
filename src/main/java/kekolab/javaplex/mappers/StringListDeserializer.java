package kekolab.javaplex.mappers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class StringListDeserializer extends JsonDeserializer<List<String>> {

	public StringListDeserializer() {
		super();
	}

	@Override
	public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		return Arrays.asList(p.getText().split(","));
	}

}
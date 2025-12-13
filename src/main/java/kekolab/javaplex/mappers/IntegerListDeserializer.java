package kekolab.javaplex.mappers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class IntegerListDeserializer extends JsonDeserializer<List<Integer>> {

	public IntegerListDeserializer() {		}

	@Override
	public List<Integer> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		return Stream.of(p.getText().split(",")).map(s -> Integer.parseInt(s))
				.collect(Collectors.toList());
	}
}
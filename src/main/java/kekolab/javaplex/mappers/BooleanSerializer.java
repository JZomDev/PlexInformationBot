package kekolab.javaplex.mappers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class BooleanSerializer extends StdSerializer<Boolean> {

	public BooleanSerializer() {
		super ((Class<Boolean>) null);
	}


	@Override
	public void serialize(Boolean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeString(value ? "1" : "0");
	}

}

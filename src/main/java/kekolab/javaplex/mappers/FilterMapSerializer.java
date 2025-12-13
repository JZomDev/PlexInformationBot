package kekolab.javaplex.mappers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class FilterMapSerializer extends StdSerializer<Map<String, List<String>>> {
    public FilterMapSerializer() {
        super((Class<Map<String, List<String>>>) null);
    }

    @Override
    public void serialize(Map<String, List<String>> filterMap, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        List<String> values = new ArrayList<>();
        for (String key : filterMap.keySet())
            values.add(key + "=" + String.join(",", filterMap.get(key)));
        gen.writeString(String.join("|", values));
    }
}
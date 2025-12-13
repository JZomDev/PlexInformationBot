package kekolab.javaplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PlexBaseItem {
	@JsonIgnore
	private Map<String, List<Object>> unmappedProperties;
	private final Logger logger = LogManager.getLogger(PlexHTTPClient.class);

	public PlexBaseItem() {
		unmappedProperties = new HashMap<>();
	}

	@JsonAnySetter
	public void setUnmappedProperty(String name, Object value) {
		if (unmappedProperties.containsKey(name))
			unmappedProperties.get(name).add(value);
		else
			unmappedProperties.put(name, new ArrayList<>(Arrays.asList(value)));
		logger.debug("Unmapped property in class " + getClass() + " - Name: " + name + "; value: " + value);
	}

	@JsonAnyGetter
	public Map<String, List<Object>> getUnmappedProperties() {
		return unmappedProperties;
	}
}

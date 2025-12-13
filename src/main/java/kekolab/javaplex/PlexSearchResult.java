package kekolab.javaplex;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.MetadataDeserializer;

public class PlexSearchResult extends PlexInitialisableItem {
	private Double score;
	@JsonProperty("Metadata")
	@JsonDeserialize(using = MetadataDeserializer.class)
	private PlexMetadata item;

	public Double getScore() {
		return score;
	}

	public PlexMetadata getItem() {
		return item;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public void setItem(PlexMetadata item) {
		this.item = item;
	}

	@Override
	protected void initialise(PlexMediaServer server, URI uri) {
		super.initialise(server, uri);
		((PlexMetadata) item).initialise(server, uri);
	}
}

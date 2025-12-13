package kekolab.javaplex;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.DirectoryDeserializer;
import kekolab.javaplex.mappers.MetadataDeserializer;

public class PlexGeneralPurposeMediaContainer<M extends PlexMetadata, D extends PlexDirectory>
		extends PlexServerMediaContainer {
	@JsonProperty("Metadata")
	@JsonDeserialize(contentUsing = MetadataDeserializer.class)
	private List<M> metadata;

	@JsonProperty("SearchResult")
	private List<PlexSearchResult> searchResults;

	@JsonProperty("Directory")
	@JsonDeserialize(contentUsing = DirectoryDeserializer.class)
	private List<D> directories;

	@JsonProperty("TranscodeSession")
	private List<PlexTranscodeSession> transcodeSessions;

	@JsonProperty("Server")
	private List<PlexClient> clients;

	PlexGeneralPurposeMediaContainer(URI uri, PlexMediaServer server) {
		super(uri, server);
		metadata = new ArrayList<>();
		searchResults = new ArrayList<>();
		transcodeSessions = new ArrayList<>();
		clients = new ArrayList<>();
	}

	private void initialiseItems(List<? extends PlexInitialisableItem> list) {
		list.stream().forEach(d -> d.initialise(server(), getUri()));
	}

	public List<PlexClient> getClients() {
		ensureFetched(clients);
		return clients;
	}

	public void setClients(List<PlexClient> clients) {
		this.clients = clients;
	}

	public List<M> getMetadata() {
		ensureFetched(metadata);
		initialiseItems(metadata.stream().map(PlexInitialisableItem.class::cast).toList());
		return metadata;
	}

	public void setMetadata(List<M> metadata) {
		this.metadata = metadata;
	}

	public List<PlexSearchResult> getSearchResults() {
		ensureFetched(searchResults);
		initialiseItems(searchResults.stream().map(PlexSearchResult.class::cast).toList());
		return searchResults;
	}

	public void setSearchResults(List<PlexSearchResult> searchResults) {
		this.searchResults = searchResults;
	}

	public List<PlexTranscodeSession> getTranscodeSessions() {
		ensureFetched(transcodeSessions);
		initialiseItems(transcodeSessions.stream().map(PlexTranscodeSession.class::cast).toList());
		return transcodeSessions;
	}

	public void setTranscodeSessions(List<PlexTranscodeSession> transcodeSessions) {
		this.transcodeSessions = transcodeSessions;
	}

	public List<D> getDirectories() {
		ensureFetched(directories);
		initialiseItems(directories.stream().map(PlexDirectory.class::cast).toList());
		return directories;
	}

	public void setDirectories(List<D> directories) {
		this.directories = directories;
	}
}

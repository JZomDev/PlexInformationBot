package kekolab.javaplex;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PlexInitialisableItem extends PlexBaseItem {
  @JsonIgnore
  private PlexMediaServer server;
  @JsonIgnore
  private URI uri;

  protected void initialise(PlexMediaServer server, URI uri) {
    this.server = server;
    this.uri = uri;
  }

  PlexMediaServer getServer() {
    return server;
  }

  protected URI uri() {
    return uri;
  }
}
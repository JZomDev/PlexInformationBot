package kekolab.javaplex;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlexInviteRequest extends PlexBaseItem {
    @JsonProperty("server_id")
    private String serverId;
    @JsonProperty("shared_server")
    private PlexSharedServer sharedServer;
    @JsonProperty("sharing_settings")
    private PlexSharingSettings sharingSettings;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public PlexSharedServer getSharedServer() {
        return sharedServer;
    }

    public void setSharedServer(PlexSharedServer sharedServer) {
        this.sharedServer = sharedServer;
    }

    public PlexSharingSettings getSharingSettings() {
        return sharingSettings;
    }

    public void setSharingSettings(PlexSharingSettings sharingSettings) {
        this.sharingSettings = sharingSettings;
    }
}
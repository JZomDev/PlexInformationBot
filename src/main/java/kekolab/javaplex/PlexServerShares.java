package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

public class PlexServerShares extends PlexMediaContainer {
    private static final String URI_TEMPLATE;
    private static final String DELETE_URI_TEMPLATE;

    static {
        URI_TEMPLATE = "https://plex.tv/api/servers/{machineIdentifier}/shared_servers";
        DELETE_URI_TEMPLATE = "https://clients.plex.tv/api/v2/shared_servers/{id}";
    }

    @JsonIgnore
    private final String machineIdentifier;

    @JsonProperty("SharedServer")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<PlexServerShare> serverShares;

    PlexServerShares(PlexHTTPClient client, String token, String machineIdentifier) throws URISyntaxException {
        super(new URI(URI_TEMPLATE.replace("{machineIdentifier}", machineIdentifier)), client, Optional.of(token));
        serverShares = new ArrayList<>();
        this.machineIdentifier = machineIdentifier;
    }

    public List<PlexServerShare> getServerShares() {
        ensureFetched(serverShares);
        return serverShares;
    }

    public void setServerShares(List<PlexServerShare> serverShares) {
        this.serverShares = serverShares;
    }

    public List<PlexServerShare> list() {
        return getServerShares();
    }

    /**
     * Invites a friend with default settings.
     * <code>inviteFriend(usernameOrEmail, sections, PlexSharingSettings.DEFAULT)</code>
     * 
     * @see PlexSharingSettings#DEFAULT
     * @param usernameOrEmail
     * @param server
     * @param sections
     * @return
     */

    public PlexServerShare inviteFriend(String usernameOrEmail, List<PlexServer.Section> sections,
            PlexSharingSettings sharingSettings) {
        PlexInviteRequest inviteRequest = new PlexInviteRequest();
        inviteRequest.setServerId(machineIdentifier);
        PlexSharedServer sharedServer = new PlexSharedServer();
        sharedServer.setInvitedEmail(usernameOrEmail);
        sharedServer.setLibrarySectionIds(sections.stream().map(PlexServer.Section::getId).toList());
        inviteRequest.setSharedServer(sharedServer);
        inviteRequest.setSharingSettings(sharingSettings);

        try {
            PlexServerShares serverShares = new PlexServerShares(getClient(), getToken().get(), machineIdentifier);
            return getClient().post(getToken(), Optional.of(inviteRequest), serverShares).list().get(0);
        } catch (URISyntaxException e) {
            throw new PlexException("Unknown error. See attached stacktrace", e);
        }
    }

    public void deleteServerShare(PlexServerShare serverShare) {
        try {
            getClient().delete(new URI(DELETE_URI_TEMPLATE.replace("{id}", serverShare.getId())), getToken(),
                    Optional.empty());
        } catch (URISyntaxException e) {
            throw new PlexException("Unknown error while deleting the server share. See attached stacktrace", e);
        }
    }

    /**
     * invites a friend with default settings.
     * The default implementation calls
     * <code>inviteFriend(usernameOrEmail, sections, PlexSharingSettings.DEFAULT)</code>
     * 
     * @see PlexSharingSettings#DEFAULT
     * @param usernameOrEmail
     * @param server
     * @param sections
     * @return
     */

    public PlexServerShare inviteFriend(String usernameOrEmail, List<PlexServer.Section> sections) {
        return inviteFriend(usernameOrEmail, sections, PlexSharingSettings.DEFAULT);
    }

}

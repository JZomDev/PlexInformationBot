package kekolab.javaplex;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlexSharedServer extends PlexBaseItem {
    @JsonProperty("library_section_ids")
    private List<Integer> librarySectionIds;
    @JsonProperty("invited_email")
    private String invitedEmail;

    public List<Integer> getLibrarySectionIds() {
        return librarySectionIds;
    }

    public void setLibrarySectionIds(List<Integer> librarySectionIds) {
        this.librarySectionIds = librarySectionIds;
    }

    public String getInvitedEmail() {
        return invitedEmail;
    }

    public void setInvitedEmail(String invitedEmail) {
        this.invitedEmail = invitedEmail;
    }

    public PlexSharedServer() {
        librarySectionIds = new ArrayList<>();
    }
}
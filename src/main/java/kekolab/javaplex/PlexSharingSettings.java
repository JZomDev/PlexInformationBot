package kekolab.javaplex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import kekolab.javaplex.mappers.BooleanSerializer;
import kekolab.javaplex.mappers.FilterMapSerializer;

public class PlexSharingSettings extends PlexBaseItem {
    public static final PlexSharingSettings DEFAULT = new PlexSharingSettings();

    @JsonSerialize(using = BooleanSerializer.class)
    private Boolean allowSync;

    @JsonSerialize(using = BooleanSerializer.class)
    private Boolean allowCameraUpload;

    @JsonSerialize(using = BooleanSerializer.class)
    private Boolean allowChannels;

    @JsonSerialize(using = FilterMapSerializer.class)
    private Map<String, List<String>> filterMovies;

    @JsonSerialize(using = FilterMapSerializer.class)
    private Map<String, List<String>> filterTelevision;

    @JsonSerialize(using = FilterMapSerializer.class)
    private Map<String, List<String>> filterMusic;

    public PlexSharingSettings() {
        filterMovies = new HashMap<>();
        filterTelevision = new HashMap<>();
        filterMusic = new HashMap<>();
    }

    public Map<String, List<String>> getFilterMovies() {
        return filterMovies;
    }

    public void setFilterMovies(Map<String, List<String>> filterMovies) {
        this.filterMovies = filterMovies;
    }

    public Map<String, List<String>> getFilterTelevision() {
        return filterTelevision;
    }

    public void setFilterTelevision(Map<String, List<String>> filterTelevision) {
        this.filterTelevision = filterTelevision;
    }

    public Map<String, List<String>> getFilterMusic() {
        return filterMusic;
    }

    public void setFilterMusic(Map<String, List<String>> filterMusic) {
        this.filterMusic = filterMusic;
    }

    public Boolean getAllowSync() {
        return allowSync;
    }

    public void setAllowSync(Boolean allowSync) {
        this.allowSync = allowSync;
    }

    public Boolean getAllowCameraUpload() {
        return allowCameraUpload;
    }

    public void setAllowCameraUpload(Boolean allowCameraUpload) {
        this.allowCameraUpload = allowCameraUpload;
    }

    public Boolean getAllowChannels() {
        return allowChannels;
    }

    public void setAllowChannels(Boolean allowChannels) {
        this.allowChannels = allowChannels;
    }
}
package kekolab.javaplex;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.hc.core5.net.URIBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import kekolab.javaplex.mappers.IntegerListDeserializer;
import kekolab.javaplex.mappers.StringListDeserializer;
import kekolab.javaplex.mappers.TimestampDeserializer;

/**
 * This class represents a Plex Media Server (public or local) and maps the XML
 * that the client receive when a GET to the homepage of the server is executed
 *
 */
public class PlexMediaServer extends PlexMediaContainer {
	private Boolean allowCameraUpload;
	private Boolean allowChannelAccess;
	private Boolean allowMediaDeletion;
	private Boolean allowSharing;
	private Boolean allowSync;
	private Boolean allowTuners;
	private Boolean backgroundProcessing;
	private Boolean certificate;
	private Boolean companionProxy;
	private String countryCode;
	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> diagnostics = new ArrayList<>();
	private Boolean eventStream;
	private String friendlyName;
	private Boolean hubSearch;
	private Boolean itemClusters;
	private Integer livetv;
	private String machineIdentifier;
	private Boolean mediaProviders;
	private Boolean multiuser;
	private Boolean myPlex;
	private String myPlexMappingState;
	private String myPlexSigninState;
	private Boolean myPlexSubscription;
	private String myPlexUsername;
	private Integer offlineTranscode;
	@JsonDeserialize(using = StringListDeserializer.class)
	private List<String> ownerFeatures = new ArrayList<>();
	private Boolean photoAutoTag;
	private String platform;
	private String platformVersion;
	private Boolean pluginHost;
	private Boolean pushNotifications;
	private Boolean readOnlyLibraries;
	private Integer requestParametersInCookie;
	private Integer streamingBrainABRVersion;
	private Integer streamingBrainVersion;
	private Boolean sync;
	private Integer transcoderActiveVideoSessions;
	private Boolean transcoderAudio;
	private Boolean transcoderLyrics;
	private Boolean transcoderPhoto;
	private Boolean transcoderSubtitles;
	private Boolean transcoderVideo;
	@JsonDeserialize(using = IntegerListDeserializer.class)
	private List<Integer> transcoderVideoBitrates = new ArrayList<>();
	@JsonDeserialize(using = IntegerListDeserializer.class)
	private List<Integer> transcoderVideoQualities = new ArrayList<>();
	@JsonDeserialize(using = IntegerListDeserializer.class)
	private List<Integer> transcoderVideoResolutions = new ArrayList<>();
	@JsonDeserialize(using = TimestampDeserializer.class)
	private Date updatedAt;
	private Boolean updater;
	private String version;
	private Boolean voiceSearch;

	PlexMediaServer(URI uri, PlexHTTPClient client, Optional<String> token) {
		super(uri, client, token);
	}

	public Boolean getAllowCameraUpload() {
		ensureFetched(allowCameraUpload);
		return allowCameraUpload;
	}

	public Boolean getAllowChannelAccess() {
		ensureFetched(allowChannelAccess);
		return allowChannelAccess;
	}

	public Boolean getAllowMediaDeletion() {
		ensureFetched(allowMediaDeletion);
		return allowMediaDeletion;
	}

	public Boolean getAllowSharing() {
		ensureFetched(allowSharing);
		return allowSharing;
	}

	public Boolean getAllowSync() {
		ensureFetched(allowSync);
		return allowSync;
	}

	public Boolean getAllowTuners() {
		ensureFetched(allowTuners);
		return allowTuners;
	}

	public Boolean getBackgroundProcessing() {
		ensureFetched(backgroundProcessing);
		return backgroundProcessing;
	}

	public Boolean getCertificate() {
		ensureFetched(certificate);
		return certificate;
	}

	public Boolean getCompanionProxy() {
		ensureFetched(companionProxy);
		return companionProxy;
	}

	public String getCountryCode() {
		ensureFetched(countryCode);
		return countryCode;
	}

	public List<String> getDiagnostics() {
		ensureFetched(diagnostics);
		return diagnostics;
	}

	public Boolean getEventStream() {
		ensureFetched(eventStream);
		return eventStream;
	}

	public String getFriendlyName() {
		ensureFetched(friendlyName);
		return friendlyName;
	}

	public Boolean getHubSearch() {
		ensureFetched(hubSearch);
		return hubSearch;
	}

	public Boolean getItemClusters() {
		ensureFetched(itemClusters);
		return itemClusters;
	}

	public Integer getLivetv() {
		ensureFetched(livetv);
		return livetv;
	}

	public String getMachineIdentifier() {
		ensureFetched(machineIdentifier);
		return machineIdentifier;
	}

	public Boolean getMediaProviders() {
		ensureFetched(mediaProviders);
		return mediaProviders;
	}

	public Boolean getMultiuser() {
		ensureFetched(multiuser);
		return multiuser;
	}

	public Boolean getMyPlex() {
		ensureFetched(myPlex);
		return myPlex;
	}

	public String getMyPlexMappingState() {
		ensureFetched(myPlexMappingState);
		return myPlexMappingState;
	}

	public String getMyPlexSigninState() {
		ensureFetched(myPlexSigninState);
		return myPlexSigninState;
	}

	public Boolean getMyPlexSubscription() {
		ensureFetched(myPlexSubscription);
		return myPlexSubscription;
	}

	public String getMyPlexUsername() {
		ensureFetched(myPlexUsername);
		return myPlexUsername;
	}

	public Integer getOfflineTranscode() {
		ensureFetched(offlineTranscode);
		return offlineTranscode;
	}

	public List<String> getOwnerFeatures() {
		ensureFetched(ownerFeatures);
		return ownerFeatures;
	}

	public Boolean getPhotoAutoTag() {
		ensureFetched(photoAutoTag);
		return photoAutoTag;
	}

	public String getPlatform() {
		ensureFetched(platform);
		return platform;
	}

	public String getPlatformVersion() {
		ensureFetched(platformVersion);
		return platformVersion;
	}

	public Boolean getPluginHost() {
		ensureFetched(pluginHost);
		return pluginHost;
	}

	public Boolean getPushNotifications() {
		ensureFetched(pushNotifications);
		return pushNotifications;
	}

	public Boolean getReadOnlyLibraries() {
		ensureFetched(readOnlyLibraries);
		return readOnlyLibraries;
	}

	public Integer getRequestParametersInCookie() {
		ensureFetched(requestParametersInCookie);
		return requestParametersInCookie;
	}

	public Integer getStreamingBrainABRVersion() {
		ensureFetched(streamingBrainABRVersion);
		return streamingBrainABRVersion;
	}

	public Integer getStreamingBrainVersion() {
		ensureFetched(streamingBrainVersion);
		return streamingBrainVersion;
	}

	public Boolean getSync() {
		ensureFetched(sync);
		return sync;
	}

	public Integer getTranscoderActiveVideoSessions() {
		ensureFetched(transcoderActiveVideoSessions);
		return transcoderActiveVideoSessions;
	}

	public Boolean getTranscoderAudio() {
		ensureFetched(transcoderAudio);
		return transcoderAudio;
	}

	public Boolean getTranscoderLyrics() {
		ensureFetched(transcoderLyrics);
		return transcoderLyrics;
	}

	public Boolean getTranscoderPhoto() {
		ensureFetched(transcoderPhoto);
		return transcoderPhoto;
	}

	public Boolean getTranscoderSubtitles() {
		ensureFetched(transcoderSubtitles);
		return transcoderSubtitles;
	}

	public Boolean getTranscoderVideo() {
		ensureFetched(transcoderVideo);
		return transcoderVideo;
	}

	public List<Integer> getTranscoderVideoBitrates() {
		ensureFetched(transcoderVideoBitrates);
		return transcoderVideoBitrates;
	}

	public List<Integer> getTranscoderVideoQualities() {
		ensureFetched(transcoderVideoQualities);
		return transcoderVideoQualities;
	}

	public List<Integer> getTranscoderVideoResolutions() {
		ensureFetched(transcoderVideoResolutions);
		return transcoderVideoResolutions;
	}

	public Date getUpdatedAt() {
		ensureFetched(updatedAt);
		return updatedAt;
	}

	public Boolean getUpdater() {
		ensureFetched(updater);
		return updater;
	}

	public String getVersion() {
		ensureFetched(version);
		return version;
	}

	public Boolean getVoiceSearch() {
		ensureFetched(voiceSearch);
		return voiceSearch;
	}

	public void setAllowCameraUpload(Boolean allowCameraUpload) {
		this.allowCameraUpload = allowCameraUpload;
	}

	public void setAllowChannelAccess(Boolean allowChannelAccess) {
		this.allowChannelAccess = allowChannelAccess;
	}

	public void setAllowMediaDeletion(Boolean allowMediaDeletion) {
		this.allowMediaDeletion = allowMediaDeletion;
	}

	public void setAllowSharing(Boolean allowSharing) {
		this.allowSharing = allowSharing;
	}

	public void setAllowSync(Boolean allowSync) {
		this.allowSync = allowSync;
	}

	public void setAllowTuners(Boolean allowTuners) {
		this.allowTuners = allowTuners;
	}

	public void setBackgroundProcessing(Boolean backgroundProcessing) {
		this.backgroundProcessing = backgroundProcessing;
	}

	public void setCertificate(Boolean certificate) {
		this.certificate = certificate;
	}

	public void setCompanionProxy(Boolean companionProxy) {
		this.companionProxy = companionProxy;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public void setDiagnostics(List<String> diagnostics) {
		this.diagnostics = diagnostics;
	}

	public void setEventStream(Boolean eventStream) {
		this.eventStream = eventStream;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public void setHubSearch(Boolean hubSearch) {
		this.hubSearch = hubSearch;
	}

	public void setItemClusters(Boolean itemClusters) {
		this.itemClusters = itemClusters;
	}

	public void setLivetv(Integer liveTv) {
		this.livetv = liveTv;
	}

	public void setMachineIdentifier(String machineIdentifier) {
		this.machineIdentifier = machineIdentifier;
	}

	public void setMediaProviders(Boolean mediaProviders) {
		this.mediaProviders = mediaProviders;
	}

	public void setMultiuser(Boolean multiuser) {
		this.multiuser = multiuser;
	}

	public void setMyPlex(Boolean myPlex) {
		this.myPlex = myPlex;
	}

	public void setMyPlexMappingState(String myPlexMappingState) {
		this.myPlexMappingState = myPlexMappingState;
	}

	public void setMyPlexSigninState(String myPlexSigninState) {
		this.myPlexSigninState = myPlexSigninState;
	}

	public void setMyPlexSubscription(Boolean myPlexSubscription) {
		this.myPlexSubscription = myPlexSubscription;
	}

	public void setMyPlexUsername(String myPlexUsername) {
		this.myPlexUsername = myPlexUsername;
	}

	public void setOfflineTranscode(Integer offlineTranscode) {
		this.offlineTranscode = offlineTranscode;
	}

	public void setOwnerFeatures(List<String> ownerFeatures) {
		this.ownerFeatures = ownerFeatures;
	}

	public void setPhotoAutoTag(Boolean photoAutoTag) {
		this.photoAutoTag = photoAutoTag;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setPlatformVersion(String platformVersion) {
		this.platformVersion = platformVersion;
	}

	public void setPluginHost(Boolean pluginHost) {
		this.pluginHost = pluginHost;
	}

	public void setPushNotifications(Boolean pushNotifications) {
		this.pushNotifications = pushNotifications;
	}

	public void setReadOnlyLibraries(Boolean readOnlyLibraries) {
		this.readOnlyLibraries = readOnlyLibraries;
	}

	public void setRequestParametersInCookie(Integer requestParametersInCookie) {
		this.requestParametersInCookie = requestParametersInCookie;
	}

	public void setStreamingBrainABRVersion(Integer streamingBrainABRVersion) {
		this.streamingBrainABRVersion = streamingBrainABRVersion;
	}

	public void setStreamingBrainVersion(Integer streamingBrainVersion) {
		this.streamingBrainVersion = streamingBrainVersion;
	}

	public void setSync(Boolean sync) {
		this.sync = sync;
	}

	public void setTranscoderActiveVideoSessions(Integer transcoderActiveVideoSessions) {
		this.transcoderActiveVideoSessions = transcoderActiveVideoSessions;
	}

	public void setTranscoderAudio(Boolean transcoderAudio) {
		this.transcoderAudio = transcoderAudio;
	}

	public void setTranscoderLyrics(Boolean transcoderLyrics) {
		this.transcoderLyrics = transcoderLyrics;
	}

	public void setTranscoderPhoto(Boolean transcoderPhoto) {
		this.transcoderPhoto = transcoderPhoto;
	}

	public void setTranscoderSubtitles(Boolean transcoderSubtitles) {
		this.transcoderSubtitles = transcoderSubtitles;
	}

	public void setTranscoderVideo(Boolean transcoderVideo) {
		this.transcoderVideo = transcoderVideo;
	}

	public void setTranscoderVideoBitrates(List<Integer> transcoderVideoBitrates) {
		this.transcoderVideoBitrates = transcoderVideoBitrates;
	}

	public void setTranscoderVideoQualities(List<Integer> transcoderVideoQualities) {
		this.transcoderVideoQualities = transcoderVideoQualities;
	}

	public void setTranscoderVideoResolutions(List<Integer> transcoderVideoResolutions) {
		this.transcoderVideoResolutions = transcoderVideoResolutions;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public void setUpdater(Boolean updater) {
		this.updater = updater;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setVoiceSearch(Boolean voiceSearch) {
		this.voiceSearch = voiceSearch;
	}

	public PlexLibrary library() {
		try {
			return new PlexLibrary(this);
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	public PlexStatus status() {
		try {
			return new PlexStatus(this);
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	public PlexServer toPlexServer() {
		if (getToken().isPresent())
			try {
				return new PlexServers(getClient(), getToken().get(), getMachineIdentifier()).getServers().get(0);
			} catch (URISyntaxException e) {
				throw new PlexException("Unknown error. Please see attache stacktrace", e);
			}
		throw new PlexException("Cannot call this method without a token");
	}

	public PlexServerShares serverShares() {
		try {
			return new PlexServerShares(getClient(), getToken().get(), getMachineIdentifier());
		} catch (URISyntaxException e) {
			throw new PlexException("Unknown error. See attached stacktrace", e);
		}
	}

	public PlexTranscode transcode() {
		try {
			return new PlexTranscode(this);
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}

	public PlexPlaylists playlists() {
		return new PlexPlaylists(this);
	}

	public PlexPlayQueues playQueues() {
		return new PlexPlayQueues(this);
	}

	public PlexClients clients() {
		return new PlexClients(this);
	}

	protected String createTemporaryToken() {
		try {
			URI uri = new URIBuilder(getUri())
					.appendPath("security")
					.appendPath("token")
					.addParameter("type", "delegation")
					.addParameter("scope", "all")
					.build();
			PlexGeneralPurposeMediaContainer<?, ?> mc = new PlexGeneralPurposeMediaContainer<>(uri, this);
			getClient().get(getToken(), mc);
			return (String) mc.getUnmappedProperties().get("token").get(0);
		} catch (URISyntaxException e) {
			throw new PlexException(e);
		}
	}
}

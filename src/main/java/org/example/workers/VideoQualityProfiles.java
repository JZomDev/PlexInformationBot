package org.example.workers;

import java.util.LinkedHashMap;
import java.util.Map;

public class VideoQualityProfiles {
	public static final Map<Integer, String> VIDEO_QUALITY_PROFILES;
	static {
		VIDEO_QUALITY_PROFILES = new LinkedHashMap<>();
		VIDEO_QUALITY_PROFILES.put(20000, "20 Mbps 1080p");
		VIDEO_QUALITY_PROFILES.put(12000, "12 Mbps 1080p");
		VIDEO_QUALITY_PROFILES.put(10000, "10 Mbps 1080p");
		VIDEO_QUALITY_PROFILES.put(8000, "8 Mbps 1080p");
		VIDEO_QUALITY_PROFILES.put(4000, "4 Mbps 720p");
		VIDEO_QUALITY_PROFILES.put(3000, "3 Mbps 720p");
		VIDEO_QUALITY_PROFILES.put(2000, "2 Mbps 720p");
		VIDEO_QUALITY_PROFILES.put(1500, "1.5 Mbps 480p");
		VIDEO_QUALITY_PROFILES.put(720, "0.7 Mbps 328p");
		VIDEO_QUALITY_PROFILES.put(320, "0.3 Mbps 240p");
		VIDEO_QUALITY_PROFILES.put(208, "0.2 Mbps 160p");
		VIDEO_QUALITY_PROFILES.put(96, "0.096 Mbps");
		VIDEO_QUALITY_PROFILES.put(64, "0.064 Mbps");
	}
}

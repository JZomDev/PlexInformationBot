package kekolab.javaplex;

public class PlexLyricsStream extends PlexStream {
	public static final int TYPE_ID = 4;
	private String format;
	private String provider;
	private Integer minLines;
	private Boolean timed;

	public String getFormat() {
		ensureDetailed(format);
		return format;
	}

	public String getProvider() {
		ensureDetailed(provider);
		return provider;
	}

	public Integer getMinLines() {
		ensureDetailed(minLines);
		return minLines;
	}

	public Boolean getTimed() {
		ensureDetailed(timed);
		return timed;
	}

	public void setTimed(Boolean timed) {
		this.timed = timed;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setMinLines(Integer minLines) {
		this.minLines = minLines;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
}

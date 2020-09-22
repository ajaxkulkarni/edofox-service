package com.rns.web.edo.service.domain.ext;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdoImpartusResponse {

	private boolean success;
	private String token;
	private Integer userType;
	private String scheduleId;
	private String liveStreamUrl;//":"https://a.impartus.com/vc/#/ttid/3030921", "
	private String liveStreamUrl2;
	private String playbackUrl;
	private String playbackUrl2;
	private String hlsPlaybackUrl;
	private String pptUrl;//":"https://a.impartus.com/download1/3030921_ppt.mp4"
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Integer getUserType() {
		return userType;
	}
	public void setUserType(Integer userType) {
		this.userType = userType;
	}
	public String getScheduleId() {
		return scheduleId;
	}
	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}
	public String getLiveStreamUrl() {
		return liveStreamUrl;
	}
	public void setLiveStreamUrl(String liveStreamUrl) {
		this.liveStreamUrl = liveStreamUrl;
	}
	public String getLiveStreamUrl2() {
		return liveStreamUrl2;
	}
	public void setLiveStreamUrl2(String liveStreamUrl2) {
		this.liveStreamUrl2 = liveStreamUrl2;
	}
	public String getPlaybackUrl() {
		return playbackUrl;
	}
	public void setPlaybackUrl(String playbackUrl) {
		this.playbackUrl = playbackUrl;
	}
	public String getPlaybackUrl2() {
		return playbackUrl2;
	}
	public void setPlaybackUrl2(String playbackUrl2) {
		this.playbackUrl2 = playbackUrl2;
	}
	public String getHlsPlaybackUrl() {
		return hlsPlaybackUrl;
	}
	public void setHlsPlaybackUrl(String hlsPlaybackUrl) {
		this.hlsPlaybackUrl = hlsPlaybackUrl;
	}
	public String getPptUrl() {
		return pptUrl;
	}
	public void setPptUrl(String pptUrl) {
		this.pptUrl = pptUrl;
	}
	
	
}

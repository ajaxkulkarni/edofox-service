package com.rns.web.edo.service.domain.ext;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class EdoGoogleNotification {
	
	private String title;
    private String body;
    private String android_channel_id;
    private String icon;
	private String click_action;
	private String visibility;//": enum (Visibility)
	private String notification_priority;//PRIORITY_HIGH
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getAndroid_channel_id() {
		return android_channel_id;
	}
	public void setAndroid_channel_id(String android_channel_id) {
		this.android_channel_id = android_channel_id;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getClick_action() {
		return click_action;
	}
	public void setClick_action(String click_action) {
		this.click_action = click_action;
	}
	public String getVisibility() {
		return visibility;
	}
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	public String getNotification_priority() {
		return notification_priority;
	}
	public void setNotification_priority(String notification_priority) {
		this.notification_priority = notification_priority;
	}

}

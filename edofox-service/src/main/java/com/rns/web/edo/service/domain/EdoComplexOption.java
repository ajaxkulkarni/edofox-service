package com.rns.web.edo.service.domain;

import java.util.List;

public class EdoComplexOption {
	
	private String optionName;
	private boolean selected;
	private List<EdoComplexOption> matchOptions;
	
	public String getOptionName() {
		return optionName;
	}
	public void setOptionName(String optionName) {
		this.optionName = optionName;
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public List<EdoComplexOption> getMatchOptions() {
		return matchOptions;
	}
	public void setMatchOptions(List<EdoComplexOption> matchOptions) {
		this.matchOptions = matchOptions;
	}
	
}

package com.rns.web.edo.service.domain.ext;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ExtDataQuestionChoice {

    private String label;//": "A",
    private String choice_id;//": 3157201,
    private String choice;//": "Augustin Jean F. Fresnel",
    private String image;//": "",
    private boolean is_right;//": false
    
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getChoice_id() {
		return choice_id;
	}
	public void setChoice_id(String choice_id) {
		this.choice_id = choice_id;
	}
	public String getChoice() {
		return choice;
	}
	public void setChoice(String choice) {
		this.choice = choice;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public boolean isIs_right() {
		return is_right;
	}
	public void setIs_right(boolean is_right) {
		this.is_right = is_right;
	}

    

}

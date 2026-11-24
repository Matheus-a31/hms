package com.hms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_preference")
public class UserPreference {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private boolean highContrast;
	private String fontSize = "normal";
	private boolean enhancedFocus = true;

	@OneToOne
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private UserAccount user;

	public UserPreference() {
		super();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isHighContrast() {
		return highContrast;
	}

	public void setHighContrast(boolean highContrast) {
		this.highContrast = highContrast;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isEnhancedFocus() {
		return enhancedFocus;
	}

	public void setEnhancedFocus(boolean enhancedFocus) {
		this.enhancedFocus = enhancedFocus;
	}

	public UserAccount getUser() {
		return user;
	}

	public void setUser(UserAccount user) {
		this.user = user;
	}
}

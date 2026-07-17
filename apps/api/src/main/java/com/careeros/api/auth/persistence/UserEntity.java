package com.careeros.api.auth.persistence;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class UserEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "google_subject", nullable = false, unique = true, length = 255)
	private String googleSubject;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "display_name", length = 200)
	private String displayName;

	@Column(name = "avatar_url", length = 2048)
	private String avatarUrl;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected UserEntity() {
	}

	public UserEntity(
			String googleSubject,
			String email,
			String displayName,
			String avatarUrl,
			Instant createdAt) {
		this.googleSubject = googleSubject;
		this.email = email;
		this.displayName = displayName;
		this.avatarUrl = avatarUrl;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getGoogleSubject() {
		return googleSubject;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void updateProfile(String email, String displayName, String avatarUrl, Instant updatedAt) {
		this.email = email;
		this.displayName = displayName;
		this.avatarUrl = avatarUrl;
		this.updatedAt = updatedAt;
	}
}

package com.careeros.api.auth.google;

import java.time.Instant;

import com.careeros.api.auth.persistence.UserEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "google_connections")
public class GoogleConnectionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private UserEntity user;

	@Column(name = "google_subject", nullable = false, unique = true, length = 255)
	private String googleSubject;

	@Column(name = "gmail_address", nullable = false, length = 320)
	private String gmailAddress;

	@Column(name = "encrypted_refresh_token", nullable = false, columnDefinition = "text")
	private String encryptedRefreshToken;

	@Column(name = "granted_scopes", nullable = false, columnDefinition = "text")
	private String grantedScopes;

	@Column(name = "connected_at", nullable = false)
	private Instant connectedAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected GoogleConnectionEntity() {
	}

	public GoogleConnectionEntity(
			UserEntity user,
			String googleSubject,
			String gmailAddress,
			String encryptedRefreshToken,
			String grantedScopes,
			Instant connectedAt) {
		this.user = user;
		this.googleSubject = googleSubject;
		this.gmailAddress = gmailAddress;
		this.encryptedRefreshToken = encryptedRefreshToken;
		this.grantedScopes = grantedScopes;
		this.connectedAt = connectedAt;
		this.updatedAt = connectedAt;
	}

	public Long getId() {
		return id;
	}

	public UserEntity getUser() {
		return user;
	}

	public String getGoogleSubject() {
		return googleSubject;
	}

	public String getGmailAddress() {
		return gmailAddress;
	}

	public String getEncryptedRefreshToken() {
		return encryptedRefreshToken;
	}

	public String getGrantedScopes() {
		return grantedScopes;
	}

	public Instant getConnectedAt() {
		return connectedAt;
	}

	public void reconnect(
			String gmailAddress,
			String encryptedRefreshToken,
			String grantedScopes,
			Instant updatedAt) {
		this.gmailAddress = gmailAddress;
		this.encryptedRefreshToken = encryptedRefreshToken;
		this.grantedScopes = grantedScopes;
		this.updatedAt = updatedAt;
	}
}

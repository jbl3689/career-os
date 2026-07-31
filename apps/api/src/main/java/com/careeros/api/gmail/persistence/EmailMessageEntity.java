package com.careeros.api.gmail.persistence;

import java.time.Instant;

import com.careeros.api.auth.persistence.UserEntity;
import com.careeros.api.gmail.GmailMessageMetadata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_messages")
public class EmailMessageEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "gmail_message_id", nullable = false, length = 255)
	private String gmailMessageId;

	@Column(name = "gmail_thread_id", nullable = false, length = 255)
	private String gmailThreadId;

	@Column(nullable = false, columnDefinition = "text")
	private String sender;

	@Column(nullable = false, columnDefinition = "text")
	private String subject;

	@Column(nullable = false, length = GmailMessageMetadata.MAXIMUM_EXCERPT_LENGTH)
	private String excerpt;

	@Column(name = "received_at", nullable = false)
	private Instant receivedAt;

	@Column(name = "first_seen_at", nullable = false)
	private Instant firstSeenAt;

	@Column(name = "last_seen_at", nullable = false)
	private Instant lastSeenAt;

	protected EmailMessageEntity() {
	}

	public EmailMessageEntity(
			UserEntity user,
			GmailMessageMetadata message,
			Instant seenAt) {
		this.user = user;
		this.gmailMessageId = message.gmailMessageId();
		this.gmailThreadId = message.gmailThreadId();
		this.sender = message.sender();
		this.subject = message.subject();
		this.excerpt = message.excerpt();
		this.receivedAt = message.receivedAt();
		this.firstSeenAt = seenAt;
		this.lastSeenAt = seenAt;
	}

	public Long getId() {
		return id;
	}

	public String getGmailMessageId() {
		return gmailMessageId;
	}

	public String getGmailThreadId() {
		return gmailThreadId;
	}

	public String getSender() {
		return sender;
	}

	public String getSubject() {
		return subject;
	}

	public String getExcerpt() {
		return excerpt;
	}

	public Instant getReceivedAt() {
		return receivedAt;
	}

	public UserEntity getUser() {
		return user;
	}

	public Instant getLastSeenAt() {
		return lastSeenAt;
	}

	public void markSeen(GmailMessageMetadata message, Instant seenAt) {
		this.gmailThreadId = message.gmailThreadId();
		this.sender = message.sender();
		this.subject = message.subject();
		this.excerpt = message.excerpt();
		this.receivedAt = message.receivedAt();
		this.lastSeenAt = seenAt;
	}
}

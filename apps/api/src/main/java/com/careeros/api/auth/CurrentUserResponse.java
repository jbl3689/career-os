package com.careeros.api.auth;

import com.careeros.api.auth.persistence.UserEntity;

public record CurrentUserResponse(
		long id,
		String email,
		String displayName,
		String avatarUrl) {

	static CurrentUserResponse from(UserEntity user) {
		return new CurrentUserResponse(
				user.getId(),
				user.getEmail(),
				user.getDisplayName(),
				user.getAvatarUrl());
	}
}

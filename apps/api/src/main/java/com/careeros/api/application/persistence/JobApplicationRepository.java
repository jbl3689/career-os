package com.careeros.api.application.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {

	@EntityGraph(attributePaths = {"company", "user"})
	List<JobApplicationEntity> findAllByUserIdOrderByIdAsc(long userId);

	@EntityGraph(attributePaths = {"company", "user"})
	Optional<JobApplicationEntity> findByIdAndUserId(long id, long userId);
}

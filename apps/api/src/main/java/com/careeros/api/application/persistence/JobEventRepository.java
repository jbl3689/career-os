package com.careeros.api.application.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobEventRepository extends JpaRepository<JobEventEntity, Long> {

	List<JobEventEntity> findAllByJobApplicationIdOrderByEventDateAscIdAsc(long jobApplicationId);
}

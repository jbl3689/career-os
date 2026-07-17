ALTER TABLE gmail_scan_results
    DROP CONSTRAINT gmail_scan_results_status_check;

ALTER TABLE gmail_scan_results
    ADD COLUMN classification VARCHAR(30),
    ADD COLUMN event_type VARCHAR(30),
    ADD COLUMN confidence_score INTEGER,
    ADD COLUMN classification_reason TEXT;

ALTER TABLE gmail_scan_results
    ADD CONSTRAINT gmail_scan_results_status_check CHECK (
        status IN (
            'PENDING_CLASSIFICATION',
            'READY_FOR_MATCHING',
            'PENDING_REVIEW',
            'IGNORED'
        )
    ),
    ADD CONSTRAINT gmail_scan_results_classification_check CHECK (
        classification IS NULL
        OR classification IN ('JOB_RELATED', 'NOT_JOB_RELATED', 'UNCERTAIN')
    ),
    ADD CONSTRAINT gmail_scan_results_event_type_check CHECK (
        event_type IS NULL
        OR event_type IN (
            'APPLICATION',
            'INTERVIEW',
            'ASSESSMENT',
            'OFFER',
            'REJECTION',
            'RECRUITER_CONTACT',
            'UNKNOWN'
        )
    ),
    ADD CONSTRAINT gmail_scan_results_confidence_score_check CHECK (
        confidence_score IS NULL
        OR confidence_score BETWEEN 0 AND 100
    );

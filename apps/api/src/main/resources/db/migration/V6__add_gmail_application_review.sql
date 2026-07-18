ALTER TABLE gmail_scan_results
    DROP CONSTRAINT gmail_scan_results_status_check;

ALTER TABLE gmail_scan_results
    ADD COLUMN match_attempted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN suggested_application_id BIGINT
        REFERENCES job_applications (id) ON DELETE SET NULL,
    ADD COLUMN selected_application_id BIGINT
        REFERENCES job_applications (id) ON DELETE SET NULL,
    ADD COLUMN match_confidence_score INTEGER,
    ADD COLUMN match_reason TEXT,
    ADD COLUMN reviewed_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE gmail_scan_results
    ADD CONSTRAINT gmail_scan_results_status_check CHECK (
        status IN (
            'PENDING_CLASSIFICATION',
            'READY_FOR_MATCHING',
            'PENDING_REVIEW',
            'IGNORED',
            'MATCHED',
            'DISMISSED'
        )
    ),
    ADD CONSTRAINT gmail_scan_results_match_confidence_score_check CHECK (
        match_confidence_score IS NULL
        OR match_confidence_score BETWEEN 0 AND 100
    );

CREATE INDEX gmail_scan_results_suggested_application_index
    ON gmail_scan_results (suggested_application_id);

CREATE INDEX gmail_scan_results_selected_application_index
    ON gmail_scan_results (selected_application_id);

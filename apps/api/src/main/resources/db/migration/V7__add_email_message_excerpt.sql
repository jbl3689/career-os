ALTER TABLE email_messages
    ADD COLUMN excerpt VARCHAR(500) NOT NULL DEFAULT '';

-- These rows have not been accepted by the user yet. Reclassifying them on the
-- next manual scan lets the new excerpt-aware rules improve existing reviews
-- without reopening matched or dismissed decisions.
UPDATE gmail_scan_results
SET status = 'PENDING_CLASSIFICATION',
    classification = NULL,
    event_type = NULL,
    confidence_score = NULL,
    classification_reason = NULL,
    match_attempted = FALSE,
    suggested_application_id = NULL,
    match_confidence_score = NULL,
    match_reason = NULL
WHERE status IN ('READY_FOR_MATCHING', 'PENDING_REVIEW', 'IGNORED');

-- Negotiations no longer start in DRAFT; they go directly to SENT (pending review).
-- Convert any existing DRAFT records so accept/reject can be applied to them.
UPDATE negotiations.negotiation SET status = 'SENT' WHERE status = 'DRAFT';

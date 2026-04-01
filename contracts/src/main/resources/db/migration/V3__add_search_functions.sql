-- =============================================================================
-- V3__add_search_functions.sql
-- Adds database functions for optimized searching with label values intersection.
-- Improves performance for complex search scenarios with labelValues filtering.
-- =============================================================================

-- Function to find contracts matching ALL label values (intersection)
-- This approach pushes filtering to the database level for better performance
CREATE OR REPLACE FUNCTION contracts.fn_find_contracts_by_label_values(
    p_label_values TEXT[],  -- Array of search values
    p_case_insensitive BOOLEAN DEFAULT TRUE
)
RETURNS TABLE (contract_id BIGINT)
LANGUAGE SQL
STABLE
PARALLEL SAFE
AS $$
    WITH value_count AS (
        SELECT COUNT(DISTINCT val) AS total_values
        FROM UNNEST(p_label_values) AS t(val)
    ),
    matching_contracts AS (
        SELECT
            c.id AS contract_id,
            vc.total_values,
            COUNT(DISTINCT search_val.val) AS matching_count
        FROM contracts.generated_contract c
        JOIN contracts.contract_field_value cfv ON c.id = cfv.generated_contract_id
        CROSS JOIN value_count vc
        CROSS JOIN LATERAL UNNEST(p_label_values) AS search_val(val)
        WHERE
            CASE
                WHEN p_case_insensitive THEN
                    LOWER(cfv.field_value) LIKE LOWER(CONCAT('%', search_val.val, '%'))
                ELSE
                    cfv.field_value LIKE CONCAT('%', search_val.val, '%')
            END
        GROUP BY c.id, vc.total_values
        HAVING COUNT(DISTINCT search_val.val) = vc.total_values
    )
    SELECT DISTINCT matching_contracts.contract_id
    FROM matching_contracts;
$$;

-- Grant execute permission to application role
-- (Uncomment and adjust role names based on your configuration)
-- GRANT EXECUTE ON FUNCTION contracts.fn_find_contracts_by_label_values(TEXT[], BOOLEAN) TO app_user;

-- Function to get contract search summary with field values
-- Useful for debugging and analytics
CREATE OR REPLACE FUNCTION contracts.fn_get_contract_search_summary(
    p_contract_id BIGINT
)
RETURNS TABLE (
    contract_id BIGINT,
    template_name VARCHAR,
    client_id INTEGER,
    contract_status VARCHAR,
    field_count BIGINT,
    field_values_csv TEXT,
    created_at TIMESTAMP
)
LANGUAGE SQL
STABLE
AS $$
    SELECT
        gc.id,
        ct.template_name,
        gc.client_id,
        gc.contract_status::TEXT,
        COUNT(cfv.id),
        STRING_AGG(DISTINCT cfv.field_value, ', ' ORDER BY cfv.field_value),
        gc.created_at
    FROM contracts.generated_contract gc
    LEFT JOIN contracts.contract_template ct ON gc.template_id = ct.id
    LEFT JOIN contracts.contract_field_value cfv ON gc.id = cfv.generated_contract_id
    WHERE gc.id = p_contract_id
    GROUP BY gc.id, ct.template_name, gc.client_id, gc.contract_status, gc.created_at;
$$;

-- Grant execute permission
-- GRANT EXECUTE ON FUNCTION contracts.fn_get_contract_search_summary(BIGINT) TO app_user;

-- Function to optimize search with multiple filters (for potential future use)
-- Returns contract IDs matching all non-null filters
CREATE OR REPLACE FUNCTION contracts.fn_search_contracts_advanced(
    p_client_id INTEGER DEFAULT NULL,
    p_contract_status VARCHAR DEFAULT NULL,
    p_generated_by INTEGER DEFAULT NULL,
    p_created_after DATE DEFAULT NULL,
    p_created_before DATE DEFAULT NULL,
    p_notes_search VARCHAR DEFAULT NULL
)
RETURNS TABLE (contract_id BIGINT, score FLOAT)
LANGUAGE SQL
STABLE
PARALLEL SAFE
AS $$
    SELECT
        gc.id,
        0.0::FLOAT AS score  -- Placeholder for future scoring logic
    FROM contracts.generated_contract gc
    WHERE
        (p_client_id IS NULL OR gc.client_id = p_client_id)
        AND (p_contract_status IS NULL OR gc.contract_status::TEXT = p_contract_status)
        AND (p_generated_by IS NULL OR gc.generated_by = p_generated_by)
        AND (p_created_after IS NULL OR CAST(gc.created_at AS DATE) >= p_created_after)
        AND (p_created_before IS NULL OR CAST(gc.created_at AS DATE) <= p_created_before)
        AND (p_notes_search IS NULL OR LOWER(gc.notes) LIKE LOWER(CONCAT('%', p_notes_search, '%')))
    ORDER BY gc.created_at DESC;
$$;

-- Grant execute permission
-- GRANT EXECUTE ON FUNCTION contracts.fn_search_contracts_advanced(INTEGER, VARCHAR, INTEGER, DATE, DATE, VARCHAR) TO app_user;


#!/bin/bash
# Test script to verify the fullyMapped trigger is working

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Testing V9 Trigger Fix${NC}\n"

# Test 1: Check if triggers exist
echo -e "${YELLOW}Test 1: Verify triggers are installed${NC}"
psql -h localhost -U clm_user -d clm_platform << EOF
SELECT trigger_name, event_manipulation, event_object_table
FROM information_schema.triggers
WHERE event_object_schema = 'contracts'
AND event_object_table = 'template_field'
ORDER BY trigger_name;
EOF

echo -e "\n${YELLOW}Test 2: Check current state of Template 1${NC}"
psql -h localhost -U clm_user -d clm_platform << EOF
SELECT id, template_name, is_fully_mapped, field_count, updated_at
FROM contracts.contract_template
WHERE id = 1;
EOF

echo -e "\n${YELLOW}Test 3: Check fields for Template 1${NC}"
psql -h localhost -U clm_user -d clm_platform << EOF
SELECT id, field_label, is_required, data_type
FROM contracts.template_field
WHERE template_id = 1
ORDER BY id;
EOF

echo -e "\n${YELLOW}Test 4: Trigger test - Update is_required to recalculate fully_mapped${NC}"
psql -h localhost -U clm_user -d clm_platform << EOF
UPDATE contracts.template_field
SET is_required = is_required
WHERE id IN (1, 2);

-- Check the result
SELECT id, template_name, is_fully_mapped, updated_at
FROM contracts.contract_template
WHERE id = 1;
EOF

echo -e "\n${GREEN}Test complete!${NC}"
echo -e "${YELLOW}Expected result for Template 1: is_fully_mapped = TRUE${NC}"
echo -e "${YELLOW}(because both fields have labels and are required)${NC}"


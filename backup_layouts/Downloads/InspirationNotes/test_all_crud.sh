#!/bin/bash

# Complete CRUD Test for Supabase API
SUPABASE_URL="https://qzbiovtdvhtgsnamvvoo.supabase.co"
SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF6YmlvdnRkdmh0Z3NuYW12dm9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NTQzOTMsImV4cCI6MjA2MjEzMDM5M30.vTRU1bjKYJRQJrxGS4mrP9sw5TZa6c9IRY5a2iXYCdE"

echo "=== COMPLETE CRUD TEST ==="
echo

# 1. READ - Get all notes
echo "1. READ TEST - Getting all notes:"
curl -s -X GET "${SUPABASE_URL}/rest/v1/travel_notes?select=*&order=id.desc" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}" | python3 -c "import sys, json; data=json.load(sys.stdin); print(f'Found {len(data)} notes')"

# 2. CREATE - Create a new note
echo -e "\n2. CREATE TEST - Creating new note:"
CREATE_RESPONSE=$(curl -s -X POST "${SUPABASE_URL}/rest/v1/travel_notes" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}" \
     -H "Content-Type: application/json" \
     -H "Prefer: return=representation" \
     -d '{
       "title": "CRUD Test Note",
       "content": "Testing all CRUD operations",
       "category": "测试",
       "user_id": 1,
       "is_favorite": false,
       "rating": 5.0
     }')

NEW_ID=$(echo $CREATE_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)[0]['id'])")
echo "Created note with ID: $NEW_ID"

# 3. UPDATE - Update the created note
echo -e "\n3. UPDATE TEST - Updating note $NEW_ID:"
UPDATE_RESPONSE=$(curl -s -X PATCH "${SUPABASE_URL}/rest/v1/travel_notes?id=eq.${NEW_ID}" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}" \
     -H "Content-Type: application/json" \
     -H "Prefer: return=representation" \
     -d '{
       "title": "CRUD Test Note - UPDATED",
       "is_favorite": true
     }')

UPDATED_TITLE=$(echo $UPDATE_RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)[0]['title'])")
echo "Updated title to: $UPDATED_TITLE"

# 4. READ SINGLE - Get the updated note
echo -e "\n4. READ SINGLE TEST - Getting note $NEW_ID:"
SINGLE_RESPONSE=$(curl -s -X GET "${SUPABASE_URL}/rest/v1/travel_notes?id=eq.${NEW_ID}" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}")

echo $SINGLE_RESPONSE | python3 -c "import sys, json; d=json.load(sys.stdin)[0]; print(f\"Title: {d['title']}, Favorite: {d['is_favorite']}\")"

# 5. DELETE - Delete the test note
echo -e "\n5. DELETE TEST - Deleting note $NEW_ID:"
curl -s -X DELETE "${SUPABASE_URL}/rest/v1/travel_notes?id=eq.${NEW_ID}" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}"
echo "Delete request sent"

# 6. VERIFY DELETE - Check if note is gone
echo -e "\n6. VERIFY DELETE - Checking if note $NEW_ID is deleted:"
VERIFY_RESPONSE=$(curl -s -X GET "${SUPABASE_URL}/rest/v1/travel_notes?id=eq.${NEW_ID}" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}")

if [ "$VERIFY_RESPONSE" = "[]" ]; then
    echo "✅ DELETE SUCCESSFUL - Note not found"
else
    echo "❌ DELETE FAILED - Note still exists"
fi

echo -e "\n=== ALL CRUD OPERATIONS TESTED ==="
#!/bin/bash

# Test updating a note with the fixed API format
echo "Testing note update with fixed SupabaseApi format..."

SUPABASE_URL="https://qzbiovtdvhtgsnamvvoo.supabase.co"
SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF6YmlvdnRkdmh0Z3NuYW12dm9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NTQzOTMsImV4cCI6MjA2MjEzMDM5M30.vTRU1bjKYJRQJrxGS4mrP9sw5TZa6c9IRY5a2iXYCdE"

echo -e "\nUpdating note ID 7 with the correct query format:"
curl -X PATCH "${SUPABASE_URL}/rest/v1/travel_notes?id=eq.7" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}" \
     -H "Content-Type: application/json" \
     -H "Prefer: return=representation" \
     -d '{
       "title": "Updated Test Note",
       "content": "This note was successfully updated after fixing the query format from ?id=7 to ?id=eq.7",
       "is_favorite": true
     }'

echo -e "\n"
#!/bin/bash

# Test creating a note with the fixed model
echo "Testing note creation with fixed TravelNote model..."

SUPABASE_URL="https://qzbiovtdvhtgsnamvvoo.supabase.co"
SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF6YmlvdnRkdmh0Z3NuYW12dm9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NTQzOTMsImV4cCI6MjA2MjEzMDM5M30.vTRU1bjKYJRQJrxGS4mrP9sw5TZa6c9IRY5a2iXYCdE"

echo -e "\nCreating a new note with the correct schema:"
curl -X POST "${SUPABASE_URL}/rest/v1/travel_notes" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}" \
     -H "Content-Type: application/json" \
     -H "Prefer: return=representation" \
     -d '{
       "title": "Fixed Model Test",
       "content": "This note was created after fixing the TravelNote model to match the database schema",
       "category": "测试分类",
       "user_id": 1,
       "is_favorite": false,
       "rating": 5.0
     }'

echo -e "\n"
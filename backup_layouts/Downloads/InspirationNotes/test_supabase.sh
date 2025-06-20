#!/bin/bash

# Test Supabase connection
echo "Testing Supabase travel_notes table..."

# Your Supabase URL and anon key
SUPABASE_URL="https://qzbiovtdvhtgsnamvvoo.supabase.co"
SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InF6YmlvdnRkdmh0Z3NuYW12dm9vIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDY1NTQzOTMsImV4cCI6MjA2MjEzMDM5M30.vTRU1bjKYJRQJrxGS4mrP9sw5TZa6c9IRY5a2iXYCdE"

# Test GET request
echo -e "\n1. Testing GET all notes:"
curl -X GET "${SUPABASE_URL}/rest/v1/travel_notes?select=*" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}"

# Test creating a note
echo -e "\n\n2. Testing POST create note:"
curl -X POST "${SUPABASE_URL}/rest/v1/travel_notes" \
     -H "apikey: ${SUPABASE_KEY}" \
     -H "Authorization: Bearer ${SUPABASE_KEY}" \
     -H "Content-Type: application/json" \
     -H "Prefer: return=representation" \
     -d '{
       "title": "测试游记",
       "content": "这是一个测试游记内容",
       "category": "自然风光",
       "location": "北京",
       "created_at": "2025-06-08T12:00:00Z",
       "owner_username": "test_user",
       "is_favorite": false,
       "rating": 5.0
     }'

echo -e "\n"
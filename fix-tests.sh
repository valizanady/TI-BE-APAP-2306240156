#!/bin/bash

# Script to add security context to all MockMvc requests in OrderedActivityRestControllerTest

FILE="src/test/java/apap/ti/_5/tour_package_2306240156_be/restcontroller/OrderedActivityRestControllerTest.java"

# Replace all mockMvc.perform(get("/ordered-activities/ with /api/ordered-activities/
sed -i '' 's|mockMvc\.perform(get("/ordered-activities/|mockMvc.perform(get("/api/ordered-activities/|g' "$FILE"

# Replace all mockMvc.perform(post("/ordered-activities/ with /api/ordered-activities/
sed -i '' 's|mockMvc\.perform(post("/ordered-activities/|mockMvc.perform(post("/api/ordered-activities/|g' "$FILE"

# Replace all mockMvc.perform(put("/ordered-activities/ with /api/ordered-activities/
sed -i '' 's|mockMvc\.perform(put("/ordered-activities/|mockMvc.perform(put("/api/ordered-activities/|g' "$FILE"

# Replace all mockMvc.perform(delete("/ordered-activities/ with /api/ordered-activities/
sed -i '' 's|mockMvc\.perform(delete("/ordered-activities/|mockMvc.perform(delete("/api/ordered-activities/|g' "$FILE"

echo "✅ Updated API paths to /api/ordered-activities/*"
echo "Now adding security context..."

# Add .with(user(createAuthenticatedUser())) after each mockMvc.perform(method(...
# This is complex, so we'll do it with a more targeted approach

sed -i '' 's|\(mockMvc\.perform(get("/api/ordered-activities/[^"]*")\)|\1\n                        .with(user(createAuthenticatedUser()))|g' "$FILE"
sed -i '' 's|\(mockMvc\.perform(post("/api/ordered-activities/[^"]*")\)|\1\n                        .with(user(createAuthenticatedUser()))|g' "$FILE"
sed -i '' 's|\(mockMvc\.perform(put("/api/ordered-activities/[^{]*{[^}]*}",[^)]*)\)|\1\n                        .with(user(createAuthenticatedUser()))|g' "$FILE"
sed -i '' 's|\(mockMvc\.perform(delete("/api/ordered-activities/[^{]*{[^}]*}",[^)]*)\)|\1\n                        .with(user(createAuthenticatedUser()))|g' "$FILE"

echo "✅ Added security context to all requests"
echo "Done!"

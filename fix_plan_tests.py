#!/usr/bin/env python3
"""
Script to fix PlanRestControllerTest by adding security context
"""

import re

file_path = "src/test/java/apap/ti/_5/tour_package_2306240156_be/restcontroller/PlanRestControllerTest.java"

with open(file_path, 'r') as f:
    content = f.read()

# Add helper method after setUp()
helper_method = '''
    // Helper method to create authenticated user for security context
    private AuthenticatedUser createAuthenticatedUser() {
        return AuthenticatedUser.builder()
                .id("test-user-id")
                .username("testuser")
                .email("testuser@example.com")
                .name("Test User")
                .role("Customer")
                .build();
    }
'''

# Find position after setUp() method closes (after the last closing brace before first @Test)
setup_end = content.find('    // ==================== POST /package/{id}/plans/create ====================')
if setup_end > 0:
    content = content[:setup_end] + helper_method + '\n' + content[setup_end:]

# Update API paths from /package/ to /api/package/ and /plans/ to /api/plans/
content = content.replace('post("/package/', 'post("/api/package/')
content = content.replace('get("/plans/', 'get("/api/plans/')
content = content.replace('put("/plans/', 'put("/api/plans/')
content = content.replace('delete("/plans/', 'delete("/api/plans/')
content = content.replace('get("/package/', 'get("/api/package/')

# Add .with(user(...)) to all mockMvc.perform lines
lines = content.split('\n')
new_lines = []
i = 0

while i < len(lines):
    line = lines[i]
    
    # Check if this is a mockMvc.perform line and doesn't already have .with(user
    if 'mockMvc.perform(' in line and '.with(user(' not in line:
        new_lines.append(line)
        
        # Check next line - if it has .param or .contentType, insert .with(user(...))
        if i + 1 < len(lines):
            next_line = lines[i + 1]
            if ('.param(' in next_line or '.contentType(' in next_line or '.content(' in next_line) and '.with(user(' not in next_line:
                indent = len(next_line) - len(next_line.lstrip())
                with_user_line = ' ' * indent + '.with(user(createAuthenticatedUser()))'
                new_lines.append(with_user_line)
        
        i += 1
    else:
        new_lines.append(line)
        i += 1

content = '\n'.join(new_lines)

# Write back
with open(file_path, 'w') as f:
    f.write(content)

print("✅ Fixed PlanRestControllerTest:")
print("   - Added createAuthenticatedUser() helper method")
print("   - Updated all API paths to /api/*")
print("   - Added .with(user(createAuthenticatedUser())) to MockMvc requests")

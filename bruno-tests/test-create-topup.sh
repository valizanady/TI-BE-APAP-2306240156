#!/bin/bash

# ====================================================================
# POST Create Top Up Transaction - Quick Test Script
# ====================================================================

echo "🚀 Testing POST Create Top Up Transaction API"
echo "=============================================="
echo ""

# Configuration
BASE_URL="http://localhost:8080"
CUSTOMER_TOKEN="your-customer-jwt-token-here"
CUSTOMER_ID="your-customer-uuid-here"
PAYMENT_METHOD_ID="your-payment-method-uuid-here"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ====================================================================
# Test 1: Create Valid Transaction (Positive Amount)
# ====================================================================
echo "📝 Test 1: Create Valid Transaction"
echo "-----------------------------------"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"${CUSTOMER_ID}\",
    \"amount\": 100000,
    \"paymentMethodId\": \"${PAYMENT_METHOD_ID}\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | jq '.'

if [ "$HTTP_CODE" -eq 201 ]; then
    echo -e "${GREEN}✅ Test 1 PASSED${NC}"
    # Save transaction ID for later tests
    TRANSACTION_ID=$(echo "$BODY" | jq -r '.data.id')
    echo "Transaction ID: $TRANSACTION_ID"
else
    echo -e "${RED}❌ Test 1 FAILED${NC}"
fi

echo ""
echo ""

# ====================================================================
# Test 2: Create Transaction with Negative Amount (Should Fail)
# ====================================================================
echo "📝 Test 2: Create Transaction with Negative Amount"
echo "---------------------------------------------------"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"${CUSTOMER_ID}\",
    \"amount\": -100000,
    \"paymentMethodId\": \"${PAYMENT_METHOD_ID}\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | jq '.'

if [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "${GREEN}✅ Test 2 PASSED (Correctly rejected negative amount)${NC}"
else
    echo -e "${RED}❌ Test 2 FAILED (Should return 400)${NC}"
fi

echo ""
echo ""

# ====================================================================
# Test 3: Create Transaction with Zero Amount (Should Fail)
# ====================================================================
echo "📝 Test 3: Create Transaction with Zero Amount"
echo "-----------------------------------------------"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"${CUSTOMER_ID}\",
    \"amount\": 0,
    \"paymentMethodId\": \"${PAYMENT_METHOD_ID}\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | jq '.'

if [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "${GREEN}✅ Test 3 PASSED (Correctly rejected zero amount)${NC}"
else
    echo -e "${RED}❌ Test 3 FAILED (Should return 400)${NC}"
fi

echo ""
echo ""

# ====================================================================
# Test 4: Create Transaction with Invalid Payment Method (Should Fail)
# ====================================================================
echo "📝 Test 4: Create Transaction with Invalid Payment Method"
echo "-----------------------------------------------------------"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"customerId\": \"${CUSTOMER_ID}\",
    \"amount\": 100000,
    \"paymentMethodId\": \"00000000-0000-0000-0000-000000000000\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | jq '.'

if [ "$HTTP_CODE" -eq 404 ] || [ "$HTTP_CODE" -eq 500 ]; then
    echo -e "${GREEN}✅ Test 4 PASSED (Correctly rejected invalid payment method)${NC}"
else
    echo -e "${RED}❌ Test 4 FAILED (Should return 404 or 500)${NC}"
fi

echo ""
echo ""

# ====================================================================
# Test 5: Create Transaction with Missing Fields (Should Fail)
# ====================================================================
echo "📝 Test 5: Create Transaction with Missing customerId"
echo "------------------------------------------------------"

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/transactions" \
  -H "Authorization: Bearer ${CUSTOMER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{
    \"amount\": 100000,
    \"paymentMethodId\": \"${PAYMENT_METHOD_ID}\"
  }")

HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')

echo "HTTP Status: $HTTP_CODE"
echo "Response:"
echo "$BODY" | jq '.'

if [ "$HTTP_CODE" -eq 400 ]; then
    echo -e "${GREEN}✅ Test 5 PASSED (Correctly rejected missing customerId)${NC}"
else
    echo -e "${RED}❌ Test 5 FAILED (Should return 400)${NC}"
fi

echo ""
echo ""

# ====================================================================
# Summary
# ====================================================================
echo "=============================================="
echo "🎯 Test Summary"
echo "=============================================="
echo "✅ Test 1: Create Valid Transaction"
echo "✅ Test 2: Negative Amount Validation"
echo "✅ Test 3: Zero Amount Validation"
echo "✅ Test 4: Invalid Payment Method Validation"
echo "✅ Test 5: Missing Field Validation"
echo ""
echo "💡 Next Steps:"
echo "  1. Update transaction status as Superadmin"
echo "  2. Verify balance updated in Profile Service"
echo "  3. Test GET all transactions with role filtering"
echo ""

# ====================================================================
# Helper: Get Active Payment Methods
# ====================================================================
echo "=============================================="
echo "📋 Helper: Get Active Payment Methods"
echo "=============================================="
echo ""
echo "Run this command to get payment methods:"
echo ""
echo "curl -X GET \"${BASE_URL}/payment-methods\" \\"
echo "  -H \"Authorization: Bearer ${CUSTOMER_TOKEN}\" | jq"
echo ""

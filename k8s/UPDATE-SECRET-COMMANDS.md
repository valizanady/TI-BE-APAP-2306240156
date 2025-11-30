# Update Kubernetes Secret - Tour Package BE

## 🚨 Error yang Terjadi

```
Error: couldn't find key API_KEY_BILL_SERVICE in Secret default/tourpackage-be-secret
```

Secret `tourpackage-be-secret` belum memiliki key `API_KEY_BILL_SERVICE` dan `BILL_SERVICE_API_KEY`.

---

## ✅ Solution: Update Secret

### Option 1: Delete & Recreate Secret (RECOMMENDED)

```bash
# 1. Delete existing secret
sudo k3s kubectl delete secret tourpackage-be-secret

# 2. Create new secret with all required keys
sudo k3s kubectl create secret generic tourpackage-be-secret \
  --from-literal=DATABASE_PASSWORD='your-db-password' \
  --from-literal=CORS_ALLOWED_ORIGINS='https://tour-package-fe-url.com' \
  --from-literal=API_KEY_BILL_SERVICE='NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS' \
  --from-literal=BILL_SERVICE_API_KEY='GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC'

# 3. Verify secret has all keys
sudo k3s kubectl describe secret tourpackage-be-secret

# 4. Delete failed pod to trigger restart
sudo k3s kubectl delete pod tourpackage-be-68965fc5b-256tt

# 5. Check new pod status
sudo k3s kubectl get pods -w
```

---

### Option 2: Patch Existing Secret (Add Missing Keys Only)

```bash
# 1. Get current secret in base64
echo -n 'NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS' | base64
# Output: TmxmVXhLTmtYSXdPUmhLWkNiYllldkZlY3hSQ0Z0dE5ueWNUUw==

echo -n 'GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC' | base64
# Output: R1dKQmRqSWtLQVlsWXNNdGhyZ0lMZ2xjQWNWd0xKUnVkQw==

# 2. Patch secret to add missing keys
sudo k3s kubectl patch secret tourpackage-be-secret \
  -p='{"data":{"API_KEY_BILL_SERVICE":"TmxmVXhLTmtYSXdPUmhLWkNiYllldkZlY3hSQ0Z0dE5ueWNUUw==","BILL_SERVICE_API_KEY":"R1dKQmRqSWtLQVlsWXNNdGhyZ0lMZ2xjQWNWd0xKUnVkQw=="}}'

# 3. Verify
sudo k3s kubectl get secret tourpackage-be-secret -o yaml

# 4. Delete failed pod
sudo k3s kubectl delete pod tourpackage-be-68965fc5b-256tt

# 5. Watch pods
sudo k3s kubectl get pods -w
```

---

## 🔍 Verify Secret Contents

```bash
# List all keys in secret
sudo k3s kubectl get secret tourpackage-be-secret -o jsonpath='{.data}' | jq 'keys'

# Decode specific key to verify
sudo k3s kubectl get secret tourpackage-be-secret -o jsonpath='{.data.API_KEY_BILL_SERVICE}' | base64 -d
# Expected: NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS

sudo k3s kubectl get secret tourpackage-be-secret -o jsonpath='{.data.BILL_SERVICE_API_KEY}' | base64 -d
# Expected: GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC
```

---

## 📝 Required Secret Keys

Secret `tourpackage-be-secret` harus memiliki **4 keys**:

| Key | Value | Purpose |
|-----|-------|---------|
| `DATABASE_PASSWORD` | Your DB password | Database authentication |
| `CORS_ALLOWED_ORIGINS` | Frontend URL | CORS configuration |
| `API_KEY_BILL_SERVICE` | `NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS` | Bill Service → Tour Package callback auth |
| `BILL_SERVICE_API_KEY` | `GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC` | Tour Package → Bill Service request auth |

---

## 🐛 Troubleshooting

### Pod still in CreateContainerConfigError

```bash
# Check pod logs
sudo k3s kubectl logs tourpackage-be-68965fc5b-256tt

# Describe pod for detailed error
sudo k3s kubectl describe pod tourpackage-be-68965fc5b-256tt

# Force delete pod
sudo k3s kubectl delete pod tourpackage-be-68965fc5b-256tt --force --grace-period=0
```

### Check if secret exists

```bash
# List all secrets
sudo k3s kubectl get secrets

# Get secret details
sudo k3s kubectl describe secret tourpackage-be-secret
```

### Rollback deployment if needed

```bash
# Check rollout history
sudo k3s kubectl rollout history deployment/tourpackage-be

# Rollback to previous version
sudo k3s kubectl rollout undo deployment/tourpackage-be

# Check status
sudo k3s kubectl rollout status deployment/tourpackage-be
```

---

## ✅ Expected Result

After updating secret and restarting pod:

```bash
sudo k3s kubectl get pods
```

```
NAME                              READY   STATUS    RESTARTS   AGE
tourpackage-be-68965fc5b-xxxxx    1/1     Running   0          2m
```

Pod should be in **Running** status with **1/1 Ready**.

---

## 📌 Quick Fix Command (All-in-One)

```bash
# Replace with your actual values
sudo k3s kubectl delete secret tourpackage-be-secret && \
sudo k3s kubectl create secret generic tourpackage-be-secret \
  --from-literal=DATABASE_PASSWORD='YOUR_DB_PASSWORD' \
  --from-literal=CORS_ALLOWED_ORIGINS='https://your-frontend-url.com' \
  --from-literal=API_KEY_BILL_SERVICE='NlfUxKNkXIwORhKZCbbYevFecxRCFttNnycTS' \
  --from-literal=BILL_SERVICE_API_KEY='GWJBdjIkKAYlYsMthrgILglcAcVwLJRudC' && \
sudo k3s kubectl delete pod -l app=tourpackage-be && \
sudo k3s kubectl get pods -w
```

---

**Created**: November 30, 2025  
**Issue**: Missing `API_KEY_BILL_SERVICE` and `BILL_SERVICE_API_KEY` in Kubernetes secret  
**Status**: ⚠️ Action Required

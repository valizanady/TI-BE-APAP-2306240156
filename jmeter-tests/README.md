# JMeter Load Testing - Quick Start

## 🚀 Quick Start

### 1. Install JMeter

**macOS:**
```bash
brew install jmeter
```

**Verify:**
```bash
jmeter -v
# Should show: Apache JMeter 5.x.x
```

### 2. Run Tests

#### Option A: Run All Tests (Automated)
```bash
# From project root
./jmeter-tests/scripts/run-all-tests.sh
```

This will:
1. Build the application
2. Start the application
3. Run Basic Load, Stress, and Spike tests
4. Generate HTML reports
5. Stop the application

**Options:**
- `--skip-build` or `-s`: Skip building the application
- `--keep-running` or `-k`: Keep application running after tests
- `--help` or `-h`: Show help

**Examples:**
```bash
# Skip build if already built
./jmeter-tests/scripts/run-all-tests.sh --skip-build

# Keep app running for manual testing
./jmeter-tests/scripts/run-all-tests.sh --keep-running
```

#### Option B: Run Single Test
```bash
# Make sure app is running first
./gradlew bootRun &

# Run specific test
./jmeter-tests/scripts/run-single-test.sh basic    # Basic load test
./jmeter-tests/scripts/run-single-test.sh stress   # Stress test
./jmeter-tests/scripts/run-single-test.sh spike    # Spike test
./jmeter-tests/scripts/run-single-test.sh endurance # Endurance test (30 min)
```

#### Option C: Manual JMeter CLI
```bash
# Start app
./gradlew bootRun &

# Run test manually
jmeter -n -t jmeter-tests/test-plans/BasicLoadTest.jmx \
       -l jmeter-tests/results/results.jtl \
       -e -o jmeter-tests/results/html-report

# View report
open jmeter-tests/results/html-report/index.html
```

#### Option D: JMeter GUI (for development)
```bash
# Start JMeter GUI
jmeter

# Open test plan: jmeter-tests/test-plans/BasicLoadTest.jmx
# Start app: ./gradlew bootRun
# Run test: Click green play button
```

## 📊 Test Plans

| Test | Users | Duration | Purpose |
|------|-------|----------|---------|
| **BasicLoadTest** | 50 | 5 min | Normal load simulation |
| **StressTest** | 500 | 6 min | Find breaking point |
| **SpikeTest** | 20→300 | 3 min | Sudden traffic surge |
| **EnduranceTest** | 50 | 30 min | Long-term stability |

## 📈 View Results

After running tests, HTML reports are generated:

```bash
# Results location
jmeter-tests/results/
├── basic-load-html-report/index.html
├── stress-test-html-report/index.html
├── spike-test-html-report/index.html
└── endurance-test-html-report/index.html
```

**Open in browser:**
```bash
# macOS
open jmeter-tests/results/basic-load-html-report/index.html

# Linux
xdg-open jmeter-tests/results/basic-load-html-report/index.html

# Windows
start jmeter-tests/results/basic-load-html-report/index.html
```

## 🎯 Success Criteria

### Basic Load Test
- ✅ Avg Response Time: < 500ms
- ✅ 95th Percentile: < 1000ms
- ✅ Error Rate: < 1%
- ✅ Throughput: > 100 req/s

### Stress Test
- ✅ Avg Response Time: < 2000ms
- ✅ Max Users: > 300
- ✅ Error Rate: < 5%
- ✅ No crashes

### Spike Test
- ✅ Recovery Time: < 30s
- ✅ Error Rate: < 10% during spike
- ✅ System stability maintained

## 🔍 Troubleshooting

### Application not starting
```bash
# Check if port 8080 is in use
lsof -ti:8080

# Kill existing process
kill $(lsof -ti:8080)

# Start fresh
./gradlew bootRun
```

### JMeter not found
```bash
# Install JMeter
brew install jmeter

# Or download from:
# https://jmeter.apache.org/download_jmeter.cgi
```

### Connection refused errors
```bash
# Verify app is running
curl http://localhost:8080/actuator/health

# Check application logs
tail -f application.log
```

### Out of memory
```bash
# Increase JMeter heap size
export JVM_ARGS="-Xms1024m -Xmx4096m"
jmeter -n -t test-plan.jmx ...
```

## 📚 Full Documentation

For complete documentation, see: **[JMETER_GUIDE.md](./JMETER_GUIDE.md)**

Topics covered:
- Detailed installation instructions
- Test plan configuration
- Performance benchmarks
- CI/CD integration
- Advanced troubleshooting
- Monitoring tips

## 🎓 Example Output

```bash
$ ./jmeter-tests/scripts/run-all-tests.sh

========================================
JMeter Load Testing - Tour Package API
========================================
[INFO] Checking prerequisites...
[SUCCESS] JMeter 5.6.3 detected
[SUCCESS] Java 17 detected
[INFO] Project root: /Users/.../tour-package-2306240156-be

========================================
Building Application
========================================
[INFO] Running: ./gradlew clean build -x test
BUILD SUCCESSFUL in 45s
[SUCCESS] Build completed

========================================
Starting Application
========================================
[INFO] Running: ./gradlew bootRun
[SUCCESS] Application started (PID: 12345)
[INFO] Waiting for application to be ready...
[SUCCESS] Application is ready!

========================================
Running Basic Load Test
========================================
[INFO] Test Plan: .../BasicLoadTest.jmx
[INFO] Results: .../basic-load-results.jtl
[INFO] HTML Report: .../basic-load-html-report
[SUCCESS] Basic Load Test completed in 320s

  Total Requests: 1250
  Successful: 1245
  Failed: 5
  Error Rate: 0.40%

[SUCCESS] HTML Report: file://.../basic-load-html-report/index.html

========================================
Test Execution Summary
========================================
Tests Run: 3
Tests Passed: 3
Tests Failed: 0
[SUCCESS] All tests completed successfully! ✓

========================================
Test Reports
========================================
Basic Load Test:
  file://.../basic-load-html-report/index.html

Stress Test:
  file://.../stress-test-html-report/index.html

Spike Test:
  file://.../spike-test-html-report/index.html

[SUCCESS] Load testing completed!
```

## 📞 Need Help?

1. Check **[JMETER_GUIDE.md](./JMETER_GUIDE.md)** for detailed documentation
2. Review application logs: `application.log`
3. Check JMeter logs: `jmeter.log` in JMeter bin directory
4. Verify all endpoints are accessible:
   ```bash
   curl http://localhost:8080/api/packages
   curl http://localhost:8080/api/locations
   curl http://localhost:8080/api/activities
   curl http://localhost:8080/api/payment-methods
   ```

---

**Coverage Status**: ✅ 83% (exceeds 80% target)  
**Load Testing**: ✅ JMeter configured  
**Last Updated**: January 2025

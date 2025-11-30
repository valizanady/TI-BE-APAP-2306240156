# JMeter Load Testing Guide - Tour Package API

## 📋 Table of Contents
1. [Overview](#overview)
2. [Installation](#installation)
3. [Test Plans](#test-plans)
4. [Running Tests](#running-tests)
5. [Interpreting Results](#interpreting-results)
6. [Performance Benchmarks](#performance-benchmarks)
7. [CI/CD Integration](#cicd-integration)
8. [Troubleshooting](#troubleshooting)

## 🎯 Overview

This project includes comprehensive JMeter load testing for the Tour Package REST API. The test suite validates the system's performance under various load conditions and ensures it meets the required performance benchmarks.

### Test Coverage
- **BasicLoadTest**: Normal load simulation (50 concurrent users)
- **StressTest**: Finding breaking point (up to 500 users)
- **SpikeTest**: Sudden traffic surge (20 → 300 users in 10 seconds)
- **EnduranceTest**: Long-duration stability (50 users for 30 minutes)

### Endpoints Tested
- `GET /api/packages` - Browse tour packages
- `GET /api/locations` - Get locations
- `GET /api/activities` - Get activities
- `GET /api/payment-methods` - Get payment methods

## 🔧 Installation

### Prerequisites
- Java 8 or higher
- 1GB+ free memory

### Download and Install JMeter

#### macOS
```bash
# Using Homebrew
brew install jmeter

# Verify installation
jmeter -v
```

#### Windows
1. Download from: https://jmeter.apache.org/download_jmeter.cgi
2. Extract to `C:\apache-jmeter`
3. Add `C:\apache-jmeter\bin` to PATH
4. Verify: `jmeter -v`

#### Linux
```bash
# Download latest version
wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz

# Extract
tar -xzf apache-jmeter-5.6.3.tgz

# Move to /opt
sudo mv apache-jmeter-5.6.3 /opt/jmeter

# Add to PATH
echo 'export PATH=$PATH:/opt/jmeter/bin' >> ~/.bashrc
source ~/.bashrc

# Verify
jmeter -v
```

### Verify Installation
```bash
jmeter -v
# Expected output:
# Apache JMeter 5.6.3 (or higher)
```

## 📊 Test Plans

### 1. BasicLoadTest.jmx
**Purpose**: Simulate normal user load

**Configuration**:
- **Users**: 50 concurrent users
- **Duration**: 5 minutes
- **Ramp-up**: 60 seconds
- **Loops**: 5 iterations per user

**Scenarios**:
- 20 users browsing packages (5 loops, 300s)
- 15 users checking locations (3 loops, 300s)
- 15 users viewing activities (3 loops, 300s)

**Expected Results**:
- Response Time: < 500ms (95th percentile)
- Error Rate: < 1%
- Throughput: > 100 requests/sec

### 2. StressTest.jmx
**Purpose**: Find the system's breaking point

**Configuration**:
- **Users**: 0 → 500 concurrent users
- **Duration**: 6 minutes
- **Ramp-up**: 120 seconds
- **Pattern**: Gradual increase to maximum load

**Expected Results**:
- Response Time: < 2000ms under peak load
- Error Rate: < 5%
- System should not crash
- Identify maximum sustainable load

### 3. SpikeTest.jmx
**Purpose**: Test response to sudden traffic surges

**Configuration**:
- **Users**: 20 → 300 in 10 seconds
- **Duration**: 3 minutes
- **Loops**: 3 iterations
- **Pattern**: Sudden spike, then sustained high load

**Expected Results**:
- Response Time: < 3000ms during spike
- Error Rate: < 10% during initial spike
- System should recover within 30 seconds
- No crashes or timeouts

### 4. EnduranceTest.jmx
**Purpose**: Verify system stability over extended period

**Configuration**:
- **Users**: 50 constant concurrent users
- **Duration**: 30 minutes
- **Ramp-up**: 60 seconds
- **Pattern**: Continuous load

**Expected Results**:
- Response Time: Consistent throughout test
- Error Rate: < 1%
- No memory leaks or degradation
- CPU/Memory stable

## 🚀 Running Tests

### Method 1: GUI Mode (Recommended for development)

1. **Start JMeter GUI**:
```bash
jmeter
```

2. **Open Test Plan**:
   - File → Open
   - Navigate to `jmeter-tests/test-plans/`
   - Select desired test plan (e.g., `BasicLoadTest.jmx`)

3. **Configure Variables** (if needed):
   - In Test Plan, find "User Defined Variables"
   - Modify `SERVER`, `PORT`, or `PROTOCOL` as needed
   - Default: `http://localhost:8080`

4. **Start Application**:
```bash
./gradlew bootRun
```

5. **Run Test**:
   - Click green "Start" button (▶️) or `Ctrl+R`
   - Monitor in real-time:
     - View Results Tree
     - Summary Report
     - Graph Results

6. **View Results**:
   - Results automatically saved to `jmeter-tests/results/`
   - Analyze graphs and reports in GUI

### Method 2: CLI Mode (Recommended for CI/CD)

#### Basic Load Test
```bash
# Ensure application is running
./gradlew bootRun &

# Run test in non-GUI mode
jmeter -n -t jmeter-tests/test-plans/BasicLoadTest.jmx \
       -l jmeter-tests/results/basic-load-results.jtl \
       -e -o jmeter-tests/results/basic-load-html-report
```

#### Stress Test
```bash
jmeter -n -t jmeter-tests/test-plans/StressTest.jmx \
       -l jmeter-tests/results/stress-test-results.jtl \
       -e -o jmeter-tests/results/stress-test-html-report
```

#### Spike Test
```bash
jmeter -n -t jmeter-tests/test-plans/SpikeTest.jmx \
       -l jmeter-tests/results/spike-test-results.jtl \
       -e -o jmeter-tests/results/spike-test-html-report
```

#### Endurance Test
```bash
jmeter -n -t jmeter-tests/test-plans/EnduranceTest.jmx \
       -l jmeter-tests/results/endurance-test-results.jtl \
       -e -o jmeter-tests/results/endurance-test-html-report
```

#### CLI Options Explained
- `-n`: Non-GUI mode
- `-t`: Test plan file
- `-l`: Results log file (.jtl)
- `-e`: Generate report dashboard after test
- `-o`: Output folder for HTML report
- `-J`: Set JMeter property (e.g., `-Jthreads=100`)

### Method 3: Using Scripts (Automation)

Create `jmeter-tests/scripts/run-all-tests.sh`:
```bash
#!/bin/bash

# Run all JMeter tests

echo "Starting Tour Package API..."
./gradlew bootRun > /dev/null 2>&1 &
APP_PID=$!
sleep 30  # Wait for application to start

echo "Running Basic Load Test..."
jmeter -n -t jmeter-tests/test-plans/BasicLoadTest.jmx \
       -l jmeter-tests/results/basic-results.jtl \
       -e -o jmeter-tests/results/basic-html-report

echo "Running Stress Test..."
jmeter -n -t jmeter-tests/test-plans/StressTest.jmx \
       -l jmeter-tests/results/stress-results.jtl \
       -e -o jmeter-tests/results/stress-html-report

echo "Running Spike Test..."
jmeter -n -t jmeter-tests/test-plans/SpikeTest.jmx \
       -l jmeter-tests/results/spike-results.jtl \
       -e -o jmeter-tests/results/spike-html-report

echo "Stopping application..."
kill $APP_PID

echo "All tests completed! Check jmeter-tests/results/ for reports."
```

Make executable and run:
```bash
chmod +x jmeter-tests/scripts/run-all-tests.sh
./jmeter-tests/scripts/run-all-tests.sh
```

## 📈 Interpreting Results

### HTML Dashboard Report

After running with `-e -o` options, open `index.html` in the results folder:

```bash
# macOS
open jmeter-tests/results/basic-load-html-report/index.html

# Linux
xdg-open jmeter-tests/results/basic-load-html-report/index.html

# Windows
start jmeter-tests/results/basic-load-html-report/index.html
```

### Key Metrics to Analyze

#### 1. Response Time
- **Average**: Mean response time across all requests
- **Median (50th percentile)**: Half of requests faster than this
- **90th percentile**: 90% of requests faster than this
- **95th percentile**: 95% of requests faster than this
- **99th percentile**: 99% of requests faster than this
- **Max**: Slowest request

**Good Targets**:
- Average: < 500ms
- 95th percentile: < 1000ms
- Max: < 3000ms

#### 2. Throughput
- **Requests/sec**: How many requests the system handles per second
- **KB/sec**: Data transfer rate

**Good Targets**:
- Basic Load: > 100 req/sec
- Stress Test: Identify maximum sustainable throughput

#### 3. Error Rate
- **% Errors**: Percentage of failed requests
- **Types**: HTTP errors (4xx, 5xx), timeouts, connection failures

**Good Targets**:
- Basic Load: < 1%
- Stress Test: < 5%
- Spike Test: < 10% during spike, < 5% after stabilization

#### 4. Active Threads Over Time
- Monitor thread ramp-up and ramp-down
- Verify thread count matches test plan
- Check for thread leaks

### Sample Analysis

#### ✅ Good Results Example
```
Test: BasicLoadTest
Users: 50
Duration: 300s

Response Times:
- Average: 342ms
- Median: 289ms
- 90th percentile: 623ms
- 95th percentile: 856ms
- Max: 1,234ms

Throughput: 145 req/sec
Error Rate: 0.3%
Total Requests: 43,500
Failed: 130

Analysis: PASSED ✓
- All response times within acceptable range
- Throughput exceeds target (100 req/sec)
- Error rate minimal (< 1%)
```

#### ❌ Poor Results Example
```
Test: StressTest
Users: 500
Duration: 360s

Response Times:
- Average: 4,567ms
- Median: 3,891ms
- 90th percentile: 12,345ms
- 95th percentile: 18,234ms
- Max: 30,000ms (timeout)

Throughput: 78 req/sec
Error Rate: 23%
Total Requests: 28,080
Failed: 6,458

Analysis: FAILED ✗
- Response times too high (avg > 2000ms)
- Throughput degraded under load
- Unacceptable error rate (> 5%)
- System overloaded at 500 concurrent users

Recommendations:
1. Optimize database queries
2. Add caching layer
3. Increase application resources
4. Consider horizontal scaling
```

### Understanding JMeter Listeners

#### View Results Tree
- Shows individual request/response details
- Useful for debugging failures
- **Warning**: Consumes memory, disable for large tests

#### Summary Report
- Tabular summary of all samplers
- Shows avg, min, max, error%, throughput
- Good for quick overview

#### Aggregate Report
- Similar to Summary but with percentiles
- Shows 90%, 95%, 99% response times
- Better for performance analysis

#### Response Time Graph
- Visual representation of response times over time
- Helps identify performance patterns
- Shows degradation trends

## 🎯 Performance Benchmarks

### Success Criteria

| Test Type | Metric | Target | Critical |
|-----------|--------|--------|----------|
| **Basic Load** | Avg Response Time | < 500ms | < 1000ms |
| | 95th Percentile | < 1000ms | < 2000ms |
| | Throughput | > 100 req/s | > 50 req/s |
| | Error Rate | < 1% | < 5% |
| **Stress Test** | Avg Response Time | < 2000ms | < 5000ms |
| | Max Concurrent Users | > 300 | > 200 |
| | Error Rate | < 5% | < 15% |
| | System Stability | No crashes | - |
| **Spike Test** | Recovery Time | < 30s | < 60s |
| | Error Rate (spike) | < 10% | < 20% |
| | Error Rate (post) | < 5% | < 10% |
| **Endurance** | Performance Drift | < 10% | < 20% |
| | Error Rate | < 1% | < 3% |
| | Memory Growth | Stable | < 20% |

### Response Time Targets by Endpoint

| Endpoint | Expected Avg | Acceptable Max |
|----------|--------------|----------------|
| GET /api/packages | < 300ms | < 1000ms |
| GET /api/locations | < 200ms | < 800ms |
| GET /api/activities | < 250ms | < 900ms |
| GET /api/payment-methods | < 150ms | < 500ms |

## 🔄 CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/load-test.yml`:

```yaml
name: Load Testing

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]
  schedule:
    - cron: '0 2 * * 0'  # Weekly on Sunday at 2 AM

jobs:
  load-test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Install JMeter
      run: |
        wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
        tar -xzf apache-jmeter-5.6.3.tgz
        echo "${PWD}/apache-jmeter-5.6.3/bin" >> $GITHUB_PATH
    
    - name: Build Application
      run: ./gradlew build -x test
    
    - name: Start Application
      run: |
        ./gradlew bootRun > application.log 2>&1 &
        echo $! > app.pid
        sleep 30
    
    - name: Wait for Application
      run: |
        timeout 60 bash -c 'until curl -f http://localhost:8080/actuator/health; do sleep 2; done'
    
    - name: Run Basic Load Test
      run: |
        jmeter -n -t jmeter-tests/test-plans/BasicLoadTest.jmx \
               -l jmeter-tests/results/basic-results.jtl \
               -e -o jmeter-tests/results/basic-html-report
    
    - name: Run Stress Test
      run: |
        jmeter -n -t jmeter-tests/test-plans/StressTest.jmx \
               -l jmeter-tests/results/stress-results.jtl \
               -e -o jmeter-tests/results/stress-html-report
    
    - name: Stop Application
      if: always()
      run: |
        if [ -f app.pid ]; then
          kill $(cat app.pid) || true
        fi
    
    - name: Upload Results
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: jmeter-results
        path: jmeter-tests/results/
    
    - name: Check Performance Thresholds
      run: |
        # Parse JTL file and check if metrics meet thresholds
        python3 jmeter-tests/scripts/check-thresholds.py \
                jmeter-tests/results/basic-results.jtl
```

### GitLab CI Example

Create `.gitlab-ci.yml`:

```yaml
stages:
  - build
  - test
  - load-test

load-test:
  stage: load-test
  image: openjdk:17-jdk
  
  before_script:
    - apt-get update && apt-get install -y wget curl
    - wget https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-5.6.3.tgz
    - tar -xzf apache-jmeter-5.6.3.tgz
    - export PATH=$PATH:${PWD}/apache-jmeter-5.6.3/bin
  
  script:
    - ./gradlew build -x test
    - ./gradlew bootRun &
    - APP_PID=$!
    - sleep 30
    - jmeter -n -t jmeter-tests/test-plans/BasicLoadTest.jmx 
             -l results.jtl -e -o report
    - kill $APP_PID
  
  artifacts:
    when: always
    paths:
      - report/
      - results.jtl
    expire_in: 1 week
  
  only:
    - main
    - merge_requests
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    
    tools {
        jdk 'JDK 17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean build -x test'
            }
        }
        
        stage('Start Application') {
            steps {
                sh '''
                    ./gradlew bootRun > app.log 2>&1 &
                    echo $! > app.pid
                    sleep 30
                '''
            }
        }
        
        stage('Load Test') {
            steps {
                sh '''
                    jmeter -n -t jmeter-tests/test-plans/BasicLoadTest.jmx \
                           -l jmeter-tests/results/basic-results.jtl \
                           -e -o jmeter-tests/results/basic-report
                '''
            }
        }
        
        stage('Performance Analysis') {
            steps {
                perfReport sourceDataFiles: 'jmeter-tests/results/*.jtl'
            }
        }
    }
    
    post {
        always {
            sh 'kill $(cat app.pid) || true'
            publishHTML([
                reportDir: 'jmeter-tests/results/basic-report',
                reportFiles: 'index.html',
                reportName: 'JMeter Load Test Report'
            ])
        }
    }
}
```

## 🔍 Troubleshooting

### Common Issues

#### 1. Connection Refused
```
Error: java.net.ConnectException: Connection refused
```

**Solutions**:
- Verify application is running: `curl http://localhost:8080/actuator/health`
- Check correct port in test plan variables
- Ensure firewall allows connections

#### 2. Out of Memory
```
Error: java.lang.OutOfMemoryError: Java heap space
```

**Solutions**:
```bash
# Increase JMeter heap size
export JVM_ARGS="-Xms1024m -Xmx4096m"
jmeter -n -t test-plan.jmx ...

# Or edit jmeter.bat / jmeter.sh:
set HEAP=-Xms1g -Xmx4g
```

#### 3. Too Many Open Files (macOS/Linux)
```
Error: java.io.IOException: Too many open files
```

**Solutions**:
```bash
# Check current limit
ulimit -n

# Increase limit
ulimit -n 10000

# Permanent fix (macOS)
echo "ulimit -n 10000" >> ~/.zshrc

# Permanent fix (Linux)
# Edit /etc/security/limits.conf:
* soft nofile 10000
* hard nofile 10000
```

#### 4. High Error Rate

**Analysis**:
1. Check View Results Tree for error details
2. Look for HTTP status codes:
   - 4xx: Client errors (bad requests)
   - 5xx: Server errors (application issues)
   - Timeout: Response too slow

**Solutions**:
- Increase timeout: `HTTPSampler.response_timeout`
- Add retry logic
- Optimize application code
- Scale infrastructure

#### 5. Inconsistent Results

**Causes**:
- Background processes
- Network instability
- Insufficient warm-up

**Solutions**:
- Close unnecessary applications
- Use stable network connection
- Add warm-up period: Set delay before main thread groups
- Run tests multiple times and average results

### Performance Optimization Tips

#### Application Side
1. **Enable Caching**: Redis, Caffeine
2. **Database Optimization**: 
   - Add indexes
   - Optimize queries
   - Use connection pooling
3. **Async Processing**: Use @Async for non-critical tasks
4. **Resource Management**: Adjust thread pool sizes
5. **Logging**: Reduce log level in production

#### JMeter Side
1. **Use Non-GUI Mode**: GUI consumes significant resources
2. **Disable Unnecessary Listeners**: Remove View Results Tree in production runs
3. **Distributed Testing**: Use multiple JMeter instances for high load
4. **Data Parameterization**: Use CSV files for varied test data
5. **Think Time**: Add realistic delays between requests

### Monitoring During Tests

#### Check Application Metrics
```bash
# CPU and Memory usage
top -pid $(lsof -ti:8080)

# Thread count
jstack $(lsof -ti:8080) | grep "java.lang.Thread.State" | wc -l

# Database connections
# Check your database monitoring tools
```

#### Spring Boot Actuator
```bash
# Health check
curl http://localhost:8080/actuator/health

# Metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Thread dump
curl http://localhost:8080/actuator/threaddump > thread-dump.json
```

## 📚 Additional Resources

### JMeter Documentation
- Official Docs: https://jmeter.apache.org/usermanual/
- Best Practices: https://jmeter.apache.org/usermanual/best-practices.html
- Functions: https://jmeter.apache.org/usermanual/functions.html

### Tutorials
- JMeter Tutorial: https://www.guru99.com/jmeter-tutorials.html
- Performance Testing: https://www.blazemeter.com/blog/category/jmeter

### Plugins
- Plugins Manager: https://jmeter-plugins.org/
- Custom Thread Groups: Better control over load patterns
- Response Times Over Time: Enhanced visualization

### Alternative Tools
- **Gatling**: Scala-based, code-as-config approach
- **K6**: Modern, developer-friendly load testing
- **Artillery**: Node.js-based, YAML configuration
- **Locust**: Python-based, distributed testing

## 🎓 Next Steps

1. **Baseline Testing**: Run tests on current system to establish baseline
2. **Optimize**: Identify and fix performance bottlenecks
3. **Re-test**: Verify optimizations improved performance
4. **Automate**: Integrate tests into CI/CD pipeline
5. **Monitor**: Set up continuous performance monitoring
6. **Scale**: Plan infrastructure scaling based on test results

## 📞 Support

For issues or questions:
1. Check JMeter logs: `jmeter.log` in JMeter bin directory
2. Review application logs: `application.log`
3. Consult JMeter documentation
4. Search JMeter user mailing list archives

---

**Last Updated**: January 2025  
**JMeter Version**: 5.6.3  
**Java Version**: 17+

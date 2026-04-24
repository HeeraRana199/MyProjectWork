# SLF4J with Logback Configuration - Logging Setup Guide

## Overview
This project implements comprehensive logging using SLF4J (Simple Logging Facade for Java) with Logback as the underlying implementation. The logging system provides date-based and size-based log file rolling with organized log management.

## Architecture

### Components Implemented:
1. **SLF4J Logger**: Industry-standard logging facade
2. **Logback**: Powerful logging framework
3. **Rolling File Appenders**: Automatic log rotation based on date and file size
4. **Multiple Log Files**: Organized logs for different purposes
5. **Structured Logging**: Consistent logging across all controllers and services

## Logback Configuration (logback.xml)

### File Location
`src/main/resources/logback.xml`

### Log Files Structure
The logging system creates and maintains the following log files in the `logs/` directory:

```
logs/
├── application.log                    # Main application logs
├── application-2025-01-15.0.log      # Date-based rolling (example)
├── application-2025-01-15.1.log      # Size overflow rollover
├── application-app.log               # Application-specific logs
├── application-error.log             # Error logs only
└── ...
```

### Configuration Details

#### 1. **Console Appender**
- Outputs logs to console during development
- Pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`
- Charset: UTF-8

#### 2. **File Appender (application.log)**
- Main application log file
- Rolling Policy: SizeAndTimeBasedRollingPolicy
- Max File Size: 10MB
- Max History: 30 days
- Total Size Cap: 1GB
- File Name Pattern: `application-%d{yyyy-MM-dd}.%i.log`
  - `%d{yyyy-MM-dd}`: Date pattern
  - `%i`: Index (increments when size exceeds 10MB)

#### 3. **Application-Specific Appender (application-app.log)**
- Dedicated logs for application code
- Same rolling policy as main appender
- Helps in separating framework logs from application logs

#### 4. **Error Appender (application-error.log)**
- Captures ERROR level logs only
- Separate error tracking
- Total Size Cap: 500MB (smaller than general logs)

### Logger Configuration

```xml
<logger name="com.cts" level="DEBUG" additivity="false">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="APPLICATION_FILE"/>
    <appender-ref ref="ERROR_FILE"/>
</logger>
```

- **Package**: `com.cts` (your application package)
- **Level**: DEBUG (captures DEBUG, INFO, WARN, ERROR)
- **Additivity**: false (prevents log duplication)

## Logging Implementation in Code

### Logger Initialization
All controllers and services use the same pattern:

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
```

### Log Levels Used

1. **DEBUG**: Detailed information for diagnostic purposes
   - Method entry/exit
   - Batch processing details
   - Data retrieval confirmations

2. **INFO**: General informational messages
   - API request reception
   - Successful operations
   - Processing milestones
   - Upload statistics

3. **WARN**: Warning messages for potentially harmful situations
   - Validation failures
   - Data not found (but expected to exist)
   - Rejected records

4. **ERROR**: Error-level messages for error events
   - Exception details
   - Failed operations
   - Data processing errors

## Classes Updated with Logging

### Controllers
1. **CandidateController**
   - Logs candidate creation, retrieval, and Excel uploads
   - Tracks upload statistics

2. **AchievementController**
   - Logs achievement CRUD operations
   - Tracks candidate association

3. **CertificationController**
   - Logs certification registration, retrieval, update, delete
   - Tracks candidate references

4. **ProjectController**
   - Logs project CRUD operations
   - Tracks candidate associations

5. **SkillController**
   - Logs skills update operations
   - Tracks programming, tools, and frameworks updates

### Services
1. **CandidateService**
   - Logs candidate addition
   - Detailed Excel upload process tracking
   - Schema validation logs
   - Batch processing logs
   - Duplicate detection and merge logging

2. **AchievementService**
   - Logs CRUD operations
   - Field update tracking

3. **CertificationService**
   - Logs certification lifecycle
   - Status change tracking

4. **ProjectService**
   - Logs project lifecycle
   - Field update tracking

5. **SkillsService**
   - Logs skills updates
   - Skill category tracking (programming, tools, frameworks)

## Log Pattern Explanation

```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

- `%d{yyyy-MM-dd HH:mm:ss.SSS}`: Timestamp (Date and time with milliseconds)
- `[%thread]`: Thread name
- `%-5level`: Log level (padded to 5 chars)
- `%logger{36}`: Logger name (abbreviated to 36 chars max)
- `%msg`: Log message
- `%n`: New line

### Example Log Output
```
2025-01-15 10:30:45.123 [http-nio-8080-exec-1] INFO  com.cts.controller.CandidateController - Received request to add candidate with ID: 12345
2025-01-15 10:30:45.456 [http-nio-8080-exec-1] DEBUG com.cts.service.CandidateService - Candidate saved successfully with ID: 12345
```

## Rolling Policy Details

### How Date-Based Rolling Works
- Every day at midnight, a new log file is created with the date in the filename
- Example: `application-2025-01-15.log` → `application-2025-01-16.log`

### How Size-Based Rolling Works
- When a log file exceeds 10MB, it's rolled over
- Index is appended to maintain the same date: `application-2025-01-15.1.log`
- If same file grows further: `application-2025-01-15.2.log`

### Cleanup Policy
- Log files older than 30 days are automatically deleted
- Total size of all logs never exceeds 1GB (error logs: 500MB)
- Oldest files are deleted first to make room

## Configuration Properties

You can modify these properties in `logback.xml`:

```xml
<property name="LOG_FILE_PATH" value="logs"/>              <!-- Log directory -->
<property name="LOG_PATTERN" value="..."/>                 <!-- Log format -->
<property name="LOG_FILE_MAX_SIZE" value="10MB"/>          <!-- Max file size -->
<property name="LOG_FILE_MAX_HISTORY" value="30"/>         <!-- Days to retain -->
```

## Best Practices Implemented

1. ✅ **Structured Logging**: All messages follow a consistent format
2. ✅ **Contextual Information**: Include relevant IDs and operation names
3. ✅ **Appropriate Log Levels**: DEBUG for detailed, INFO for important events
4. ✅ **Exception Handling**: Full stack traces logged on errors
5. ✅ **Performance**: Lazy evaluation of log messages using parameterized format
6. ✅ **Organization**: Separate logs for different severity levels
7. ✅ **Automatic Rotation**: No manual log management needed
8. ✅ **Disk Space Management**: Automatic cleanup of old logs

## Example Log Scenarios

### Excel Upload Scenario
```
INFO  - Starting Excel upload process for file: candidates.xlsx
DEBUG - Validating Excel schema for file: candidates.xlsx
DEBUG - Schema validation passed for file: candidates.xlsx
DEBUG - Parsing candidates from Excel file: candidates.xlsx
INFO  - Parsed 150 candidates from file: candidates.xlsx
DEBUG - Starting batch processing of candidates
DEBUG - Adding new candidate ID: C001
DEBUG - Saving batch of 50 candidates
INFO  - Excel upload completed successfully. Total: 150, Saved: 120, Updated: 25, Rejected: 5
```

### Error Scenario
```
INFO  - Received request to fetch candidate with ID: 999
WARN  - Candidate not found with ID: 999
ERROR - Error occurred while fetching candidate with ID: 999, CandidateNotFoundException: ...
```

## Accessing Logs

### During Development
- Logs appear in console
- Check `logs/` directory for persistent files

### In Production
- Monitor `logs/application.log` for general operations
- Check `logs/application-error.log` for errors only
- Date-based files in format `application-YYYY-MM-DD.log`

## Troubleshooting

### No logs appearing
1. Check if `logback.xml` is in `src/main/resources/`
2. Verify `logs/` directory has write permissions
3. Check console output first

### Logs not rolling
1. Verify file size exceeds 10MB (for size-based rolling)
2. Check system clock for date-based rolling
3. Ensure disk space available for new log files

### Old logs not deleted
1. Check `maxHistory` property (should be 30)
2. Verify `totalSizeCap` isn't preventing deletion
3. Ensure log files follow naming convention

## Spring Boot Integration

This setup works seamlessly with Spring Boot because:
1. Spring Boot auto-configures Logback when it's on classpath
2. `logback.xml` is automatically discovered in `src/main/resources/`
3. No additional configuration needed in `application.properties`
4. SLF4J is already included in Spring Boot starter-web

## Summary

The logging system provides:
- ✅ Comprehensive tracking of all operations
- ✅ Automatic date and size-based log rotation
- ✅ Organized log files by severity
- ✅ Automatic cleanup of old logs
- ✅ Console output for development
- ✅ Professional log format for debugging
- ✅ Zero-configuration production readiness


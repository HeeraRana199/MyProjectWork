# Quick Start Guide - SLF4J Logging Implementation

## What Has Been Implemented

### 1. **Logback Configuration File**
- **Location**: `src/main/resources/logback.xml`
- **Features**:
  - Console appender for development
  - File appender with date-based rolling
  - Size-based rolling (10MB per file)
  - Error-only log file
  - Automatic cleanup after 30 days
  - Total size cap: 1GB

### 2. **Logging Added to All Controllers**
1. ✅ CandidateController - 5 log statements
2. ✅ AchievementController - 16 log statements  
3. ✅ CertificationController - 18 log statements
4. ✅ ProjectController - 18 log statements
5. ✅ SkillController - 7 log statements

### 3. **Logging Added to All Services**
1. ✅ CandidateService - 23 log statements (including Excel upload tracking)
2. ✅ AchievementService - 15 log statements
3. ✅ CertificationService - 25 log statements
4. ✅ ProjectService - 23 log statements
5. ✅ SkillsService - 16 log statements

## Log Files Generated

When you run the application, the following log files will be created in the `logs/` directory:

```
logs/
├── application.log              → Main application logs
├── application-app.log          → Application-specific logs
├── application-error.log        → Error logs only
├── application-2025-01-15.0.log → Date-based rolling (old files)
├── application-2025-01-15.1.log → Size overflow (10MB+)
└── application-error-2025-01-15.0.log → Error date-based
```

## Example Log Output

### POST /candidate (Add Candidate)
```
2025-01-15 14:35:22.145 [http-nio-8080-exec-1] INFO  com.cts.controller.CandidateController - Received request to add candidate with ID: 12345
2025-01-15 14:35:22.256 [http-nio-8080-exec-1] INFO  com.cts.service.CandidateService - Adding candidate with ID: 12345, Name: John Doe
2025-01-15 14:35:22.389 [http-nio-8080-exec-1] DEBUG com.cts.service.CandidateService - Candidate saved successfully with ID: 12345
2025-01-15 14:35:22.412 [http-nio-8080-exec-1] INFO  com.cts.controller.CandidateController - Successfully added candidate with ID: 12345
```

### POST /candidate/upload (Excel Upload with 100 candidates)
```
2025-01-15 14:40:15.234 [http-nio-8080-exec-2] INFO  com.cts.controller.CandidateController - Received Excel upload request with file: candidates.xlsx, size: 156234 bytes
2025-01-15 14:40:15.245 [http-nio-8080-exec-2] INFO  com.cts.service.CandidateService - Starting Excel upload process for file: candidates.xlsx
2025-01-15 14:40:15.256 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Validating Excel schema for file: candidates.xlsx
2025-01-15 14:40:15.267 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Schema validation passed for file: candidates.xlsx
2025-01-15 14:40:15.278 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Parsing candidates from Excel file: candidates.xlsx
2025-01-15 14:40:15.389 [http-nio-8080-exec-2] INFO  com.cts.service.CandidateService - Parsed 100 candidates from file: candidates.xlsx
2025-01-15 14:40:15.401 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Starting batch processing of candidates
2025-01-15 14:40:15.412 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Adding new candidate ID: C001
2025-01-15 14:40:15.423 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Adding new candidate ID: C002
...
2025-01-15 14:40:15.512 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Saving batch of 50 candidates
2025-01-15 14:40:15.634 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Merging changes for existing candidate ID: C051
2025-01-15 14:40:15.645 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Updating batch of 30 candidates
2025-01-15 14:40:15.756 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Rejecting duplicate candidate ID: C075
2025-01-15 14:40:15.812 [http-nio-8080-exec-2] DEBUG com.cts.service.CandidateService - Saving batch of 20 candidates
2025-01-15 14:40:15.923 [http-nio-8080-exec-2] INFO  com.cts.service.CandidateService - Excel upload completed successfully. Total: 100, Saved: 70, Updated: 20, Rejected: 10
2025-01-15 14:40:15.945 [http-nio-8080-exec-2] INFO  com.cts.controller.CandidateController - Excel file processed successfully. Total: 100, Saved: 70, Updated: 20, Rejected: 10
```

### GET /candidate?id=12345 (Fetch Candidate)
```
2025-01-15 14:45:30.123 [http-nio-8080-exec-3] INFO  com.cts.controller.CandidateController - Received request to fetch candidate with ID: 12345
2025-01-15 14:45:30.134 [http-nio-8080-exec-3] DEBUG com.cts.service.CandidateService - Fetching candidate with ID: 12345
2025-01-15 14:45:30.245 [http-nio-8080-exec-3] DEBUG com.cts.service.CandidateService - Successfully retrieved candidate with ID: 12345
2025-01-15 14:45:30.267 [http-nio-8080-exec-3] DEBUG com.cts.controller.CandidateController - Successfully fetched candidate with ID: 12345
```

### Error Scenario - Candidate Not Found
```
2025-01-15 14:50:45.234 [http-nio-8080-exec-4] INFO  com.cts.controller.CandidateController - Received request to fetch candidate with ID: 99999
2025-01-15 14:50:45.245 [http-nio-8080-exec-4] DEBUG com.cts.service.CandidateService - Fetching candidate with ID: 99999
2025-01-15 14:50:45.256 [http-nio-8080-exec-4] WARN  com.cts.service.CandidateService - Candidate not found with ID: 99999
2025-01-15 14:50:45.267 [http-nio-8080-exec-4] ERROR com.cts.controller.CandidateController - Error occurred while fetching candidate with ID: 99999, com.cts.exceptions.CandidateNotFoundException: Candidate not found
	at com.cts.service.CandidateService.getCandidateById(CandidateService.java:63)
	...
```

## How to View Logs

### While Application is Running
1. Check console output immediately (INFO and above)
2. In IDE: View console tab

### After Application Stops
Navigate to `logs/` folder:
```bash
# List all log files
ls logs/

# View main application log
cat logs/application.log

# View error logs only
cat logs/application-error.log

# Follow live log updates (if still running)
tail -f logs/application.log

# Search for specific errors
grep "ERROR" logs/application-error.log

# Search for specific operation
grep "Excel upload" logs/application-app.log
```

## Configuration Customization

Edit `src/main/resources/logback.xml` to customize:

### Change Log Directory
```xml
<property name="LOG_FILE_PATH" value="custom_logs"/>
```

### Change File Size Limit
```xml
<property name="LOG_FILE_MAX_SIZE" value="20MB"/>  <!-- Increase to 20MB -->
```

### Change Retention Period
```xml
<property name="LOG_FILE_MAX_HISTORY" value="60"/>  <!-- Keep 60 days -->
```

### Change Total Log Size Cap
```xml
<totalSizeCap>2GB</totalSizeCap>  <!-- Allow 2GB total -->
```

### Change Log Level
```xml
<logger name="com.cts" level="DEBUG"/>  <!-- Options: DEBUG, INFO, WARN, ERROR -->
```

## Performance Impact

✅ **Minimal Performance Impact**:
- Asynchronous logging (non-blocking)
- Parameterized log messages
- File I/O optimized
- No significant overhead on application

## Best Practices

1. **Use appropriate log levels**:
   - DEBUG: Detailed diagnostics (turn off in production)
   - INFO: Important events
   - WARN: Potentially harmful situations
   - ERROR: Error events

2. **Include context**:
   - Always log IDs for tracing
   - Include timestamps (automatic)
   - Log method entry/exit for complex operations

3. **Avoid logging sensitive data**:
   - Don't log passwords
   - Don't log credit card numbers
   - Don't log PII unnecessarily

4. **Monitor disk space**:
   - Logs can grow large
   - Automatic cleanup helps, but monitor it
   - Check `totalSizeCap` settings

## Troubleshooting

### 1. No logs appearing?
```
✓ Check: logs/ directory exists and is writable
✓ Check: logback.xml is in src/main/resources/
✓ Check: Application started successfully
✓ Check: Console shows Spring Boot startup message
```

### 2. Logs not rolling?
```
✓ For size-based: Check if files exceed 10MB
✓ For date-based: Check system date
✓ Check: Disk space available
✓ Check: File permissions
```

### 3. High disk usage?
```
✓ Reduce LOG_FILE_MAX_SIZE (e.g., 5MB instead of 10MB)
✓ Reduce LOG_FILE_MAX_HISTORY (e.g., 15 days instead of 30)
✓ Change log level to INFO (skip DEBUG logs)
✓ Reduce totalSizeCap
```

## Integration with IDEs

### IntelliJ IDEA
- View logs in "Services" tab → Application logs
- Configure in Run → Edit Configurations
- Set working directory to project root

### Eclipse
- View logs in Console tab
- Configure in Run Configurations
- Check "External Program Console"

### VS Code
- Use terminal to tail logs
- Install "Log File Highlighter" extension
- Command: `tail -f logs/application.log`

## Summary

Your logging system is now:
✅ Fully integrated with SLF4J and Logback
✅ Configured for production use
✅ Automatically rotating logs by date and size
✅ Tracking all operations in controllers and services
✅ Separating errors for easy monitoring
✅ Ready for monitoring and debugging


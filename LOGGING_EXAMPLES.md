# Logging Usage Examples

This document provides copy-paste examples for implementing logging in new controllers and services.

## Controller Logging Template

```java
package com.cts.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/your-endpoint")
public class YourController {

    // ✅ Always add this line
    private static final Logger logger = LoggerFactory.getLogger(YourController.class);
    
    private YourService yourService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody YourEntity entity) {
        // Log request reception
        logger.info("Received request to create entity with ID: {}", entity.getId());
        
        try {
            // Call service
            YourEntity result = yourService.create(entity);
            
            // Log success
            logger.info("Successfully created entity with ID: {}", result.getId());
            return new ResponseEntity<>(result, HttpStatus.CREATED);
            
        } catch (Exception e) {
            // Log error
            logger.error("Error occurred while creating entity: {}", entity.getId(), e);
            return ResponseEntity.internalServerError()
                .body("Error creating entity: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable int id) {
        logger.info("Received request to fetch entity with ID: {}", id);
        
        try {
            YourEntity result = yourService.getById(id);
            logger.debug("Successfully fetched entity with ID: {}", id);
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error occurred while fetching entity with ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body("Error fetching entity: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody YourEntity entity, @PathVariable int id) {
        logger.info("Received request to update entity with ID: {}", id);
        
        try {
            YourEntity result = yourService.update(entity, id);
            logger.info("Successfully updated entity with ID: {}", id);
            return new ResponseEntity<>(result, HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error occurred while updating entity with ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body("Error updating entity: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable int id) {
        logger.info("Received request to delete entity with ID: {}", id);
        
        try {
            yourService.delete(id);
            logger.info("Successfully deleted entity with ID: {}", id);
            return new ResponseEntity<>("Entity deleted successfully!", HttpStatus.OK);
            
        } catch (Exception e) {
            logger.error("Error occurred while deleting entity with ID: {}", id, e);
            return ResponseEntity.internalServerError()
                .body("Error deleting entity: " + e.getMessage());
        }
    }
}
```

## Service Logging Template

```java
package com.cts.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class YourService {

    // ✅ Always add this line
    private static final Logger logger = LoggerFactory.getLogger(YourService.class);
    
    private YourRepository yourRepository;

    public YourEntity create(YourEntity entity) {
        // Log operation start with details
        logger.info("Creating entity with ID: {}, Name: {}", entity.getId(), entity.getName());
        
        try {
            // Perform operation
            YourEntity saved = yourRepository.save(entity);
            
            // Log success
            logger.debug("Entity saved successfully with ID: {}", saved.getId());
            return saved;
            
        } catch (Exception e) {
            // Log error with context
            logger.error("Error while creating entity with ID: {}", entity.getId(), e);
            throw e;  // Re-throw for controller to handle
        }
    }

    @Transactional
    public YourEntity getById(int id) {
        // Log retrieval request
        logger.debug("Fetching entity with ID: {}", id);
        
        try {
            YourEntity entity = yourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            
            logger.debug("Successfully retrieved entity with ID: {}", id);
            return entity;
            
        } catch (EntityNotFoundException e) {
            // Log specific error condition
            logger.warn("Entity not found with ID: {}", id);
            throw e;
            
        } catch (Exception e) {
            logger.error("Error while fetching entity with ID: {}", id, e);
            throw e;
        }
    }

    public YourEntity update(YourEntity entity, int id) {
        logger.info("Updating entity with ID: {}", id);
        
        try {
            YourEntity existing = yourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            
            // Update fields
            if (entity.getName() != null) {
                logger.debug("Updating entity name for ID: {} to: {}", id, entity.getName());
                existing.setName(entity.getName());
            }
            
            if (entity.getDescription() != null) {
                logger.debug("Updating entity description for ID: {}", id);
                existing.setDescription(entity.getDescription());
            }
            
            YourEntity updated = yourRepository.save(existing);
            logger.info("Entity updated successfully with ID: {}", id);
            return updated;
            
        } catch (Exception e) {
            logger.error("Error while updating entity with ID: {}", id, e);
            throw e;
        }
    }

    public void delete(int id) {
        logger.info("Deleting entity with ID: {}", id);
        
        try {
            YourEntity entity = yourRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found"));
            
            yourRepository.delete(entity);
            logger.info("Entity deleted successfully with ID: {}", id);
            
        } catch (Exception e) {
            logger.error("Error while deleting entity with ID: {}", id, e);
            throw e;
        }
    }
}
```

## Logging Best Practices

### ✅ DO

```java
// ✅ Include relevant IDs for tracing
logger.info("Processing order ID: {} for customer ID: {}", orderId, customerId);

// ✅ Use appropriate log levels
logger.debug("Detailed diagnostic info");
logger.info("Important business event");
logger.warn("Something unexpected but recoverable");
logger.error("Error - operation failed", exception);

// ✅ Use parameterized logging (efficient)
logger.info("User {} logged in from {}", username, ipAddress);

// ✅ Include context in error logs
logger.error("Failed to save user with ID: {}", userId, e);

// ✅ Log batch operations
logger.info("Processing {} records", totalCount);
logger.debug("Processed {} records successfully", successCount);

// ✅ Log before and after state changes
logger.info("Starting data migration");
logger.info("Data migration completed: {} records migrated, {} failed", success, failed);
```

### ❌ DON'T

```java
// ❌ String concatenation (performance issue)
logger.info("User " + username + " logged in");  // Bad

// ❌ Logging sensitive data
logger.info("User password: {}", password);  // Bad

// ❌ Logging generic objects (unclear)
logger.info("Object: {}", complexObject);  // Bad

// ❌ Swallowing exceptions silently
try {
    doSomething();
} catch (Exception e) {
    logger.info("Error occurred");  // Bad - no stack trace
}

// ❌ Too verbose at INFO level (use DEBUG)
logger.info("Entering method");
logger.info("Setting field1");
logger.info("Setting field2");

// ❌ Not logging errors at all
if (result == null) {
    return null;  // Bad - silent failure
}
```

## Common Logging Scenarios

### Scenario 1: User Authentication
```java
logger.info("Authentication attempt for user: {}", username);

try {
    User user = userService.authenticate(username, password);
    logger.info("User {} authenticated successfully", username);
    return ResponseEntity.ok(user);
} catch (UnauthorizedException e) {
    logger.warn("Authentication failed for user: {}", username);
    return ResponseEntity.unauthorized().build();
} catch (Exception e) {
    logger.error("Error during authentication for user: {}", username, e);
    return ResponseEntity.internalServerError().build();
}
```

### Scenario 2: Data Processing with Batch
```java
logger.info("Starting batch processing for {} items", items.size());

int processed = 0;
int failed = 0;

for (Item item : items) {
    try {
        logger.debug("Processing item: {}", item.getId());
        processItem(item);
        processed++;
    } catch (Exception e) {
        logger.error("Failed to process item: {}", item.getId(), e);
        failed++;
    }
}

logger.info("Batch processing completed. Processed: {}, Failed: {}", processed, failed);
```

### Scenario 3: External API Call
```java
logger.info("Calling external API with request ID: {}", requestId);
long startTime = System.currentTimeMillis();

try {
    ApiResponse response = externalService.call(request);
    long duration = System.currentTimeMillis() - startTime;
    logger.info("External API call successful for request ID: {} in {}ms", requestId, duration);
    return response;
} catch (TimeoutException e) {
    logger.warn("External API call timeout for request ID: {} after 30s", requestId);
    throw e;
} catch (Exception e) {
    logger.error("External API call failed for request ID: {}", requestId, e);
    throw e;
}
```

### Scenario 4: Database Transaction
```java
logger.info("Starting database transaction for operation: {}", operationName);

try {
    beginTransaction();
    
    logger.debug("Performing operation: {}", operationName);
    performOperation();
    
    commit();
    logger.info("Database transaction committed successfully for: {}", operationName);
    
} catch (Exception e) {
    logger.error("Database transaction failed for: {}, rolling back", operationName, e);
    rollback();
    throw e;
}
```

### Scenario 5: Resource Cleanup
```java
logger.info("Initializing resource: {}", resourceName);

try {
    Resource resource = createResource();
    // Use resource
} catch (Exception e) {
    logger.error("Error using resource: {}", resourceName, e);
    throw e;
} finally {
    logger.debug("Cleaning up resource: {}", resourceName);
    closeResource();
    logger.info("Resource cleanup completed for: {}", resourceName);
}
```

## Log Message Formatting

### Good Log Messages
```
✓ "User login successful for user: alice from IP: 192.168.1.100"
✓ "Batch operation completed. Total: 100, Success: 95, Failed: 5"
✓ "Excel file processed in 1234ms"
✓ "Database connection pool created with 10 connections"
✓ "User account created: ID=12345, Email=user@example.com"
```

### Poor Log Messages
```
✗ "Something happened"
✗ "Error occurred"
✗ "Processing"
✗ "Done"
✗ "ID: 123"  (without context)
```

## Log Rotation Examples

### When logs rotate (automatic):
```
application.log          (started today, 5MB)
application.log          (grew to 10MB) → rotated
application-2025-01-15.0.log  (now current, renamed old)
application.log          (new current file, empty)
application.log          (grew to 10MB again) → rotated
application-2025-01-15.1.log  (second file from today)
application.log          (new current file)

# Next day:
application.log          (started fresh today)
application-2025-01-16.0.log  (yesterday's final state)
```

## Performance Optimization

### 1. Use Debug Level for Verbose Logs
```java
// ✅ Good - only logged when DEBUG is enabled
logger.debug("Detailed operation info: {}", detailedInfo);

// ❌ Bad - always evaluated
logger.info("Info: " + getExpensiveComputation());

// ✅ Good - lambda only executed if DEBUG enabled
logger.debug(() -> "Info: " + getExpensiveComputation());
```

### 2. Batch Large Operations
```java
logger.info("Starting import of {} records", recordCount);

final int BATCH_SIZE = 1000;
int processed = 0;

for (int i = 0; i < records.size(); i += BATCH_SIZE) {
    List<Record> batch = records.subList(i, Math.min(i + BATCH_SIZE, records.size()));
    logger.debug("Processing batch {} of {}", (i / BATCH_SIZE) + 1, 
                 (records.size() + BATCH_SIZE - 1) / BATCH_SIZE);
    processBatch(batch);
    processed += batch.size();
}

logger.info("Import completed. Total processed: {}", processed);
```

## Integration with Monitoring Tools

These logs can be integrated with:
- ELK Stack (Elasticsearch, Logstash, Kibana)
- Splunk
- DataDog
- New Relic
- CloudWatch (AWS)
- Azure Monitor

Simply configure these tools to read from the `logs/` directory.


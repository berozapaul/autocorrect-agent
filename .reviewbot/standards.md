# Java Coding Standards

> Based on the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html).
> Drop this file as `.reviewbot/standards.md` in your Java repository root.
> The PR Review Agent will automatically use it instead of the global standards.

---

## 1. Naming Conventions

| Identifier | Style | ✅ Correct | ❌ Wrong |
|-----------|-------|-----------|---------|
| Package | `lowercase` only | `com.zooplus.orders` | `com.zooplus.Orders` |
| Class / Interface | `UpperCamelCase` | `UserService` | `userService`, `User_Service` |
| Method | `lowerCamelCase` | `getUserById()` | `GetUserById()`, `get_user_by_id()` |
| Constant (`static final`) | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` | `maxRetryCount`, `MaxRetryCount` |
| Field (non-constant) | `lowerCamelCase` | `orderCount` | `OrderCount`, `m_orderCount`, `mOrderCount` |
| Parameter | `lowerCamelCase` | `userId` | `UserId`, `user_id` |
| Local variable | `lowerCamelCase` | `tempResult` | `TempResult`, `temp_result` |
| Type variable | Single capital or `NameT` | `T`, `E`, `RequestT` | `type`, `TYPE` |
| Test class | Ends with `Test` | `UserServiceTest` | `UserServiceTests`, `TestUserService` |

**Rules:**
- No special prefixes or suffixes: `mName`, `s_name`, `name_`, `kName` are all **forbidden**
- No single-character parameter names in public methods (except type variables like `T`, `E`)
- Acronyms follow camelCase: `XmlHttpRequest` not `XMLHTTPRequest`, `newCustomerId` not `newCustomerID`

---

## 2. Source File Structure

Every `.java` file must follow this order:
1. License/copyright (if applicable)
2. Package declaration
3. Imports
4. Exactly one top-level class

- File name must match the top-level class name exactly (case-sensitive)
- Encoding: UTF-8
- No tab characters — use spaces only

---

## 3. Imports

- **No wildcard imports** — `import java.util.*` is forbidden
- **No module imports** — `import module java.base` is forbidden
- **Order**: static imports first (one group), then non-static imports (one group), separated by one blank line
- No line-wrapping of import statements

```java
// ✅ Correct
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

// ❌ Wrong
import java.util.*;
import static org.junit.Assert.*;
```

---

## 4. Formatting

### 4.1 Indentation
- **2 spaces** per block level (not 4, not tabs)
- Continuation lines: **+4 spaces** minimum from the original line

### 4.2 Column Limit
- **100 characters** maximum per line
- Exceptions: long URLs in Javadoc, package/import declarations

### 4.3 Braces
- Always use braces for `if`, `else`, `for`, `do`, `while` — even for single-statement bodies
- K&R style: opening brace on same line

```java
// ✅ Correct
if (condition) {
  doSomething();
}

// ❌ Wrong — missing braces
if (condition)
  doSomething();
```

### 4.4 One Statement Per Line
```java
// ✅ Correct
int a = 1;
int b = 2;

// ❌ Wrong
int a = 1; int b = 2;
```

### 4.5 Variable Declarations
- One variable per declaration

```java
// ✅ Correct
int a;
int b;

// ❌ Wrong
int a, b;
```

### 4.6 Switch Statements
- Prefer new-style switch expressions with `->` arrows
- Old-style: mark fall-through with `// fall through` comment
- Every switch must be exhaustive — add `default` even if empty

```java
// ✅ New-style (preferred)
return switch (status) {
  case ACTIVE -> "active";
  case INACTIVE -> "inactive";
  default -> "unknown";
};
```

### 4.7 Modifier Order
```java
public protected private abstract default static final sealed non-sealed transient volatile synchronized native strictfp
```

---

## 5. Programming Practices

### 5.1 @Override
Always use `@Override` when overriding a method.
```java
// ✅ Correct
@Override
public String toString() { ... }

// ❌ Wrong — missing @Override
public String toString() { ... }
```

### 5.2 Exception Handling
Never silently swallow exceptions.
```java
// ✅ Correct
try {
  process();
} catch (IOException e) {
  logger.error("Failed to process", e);
  throw new ServiceException("Processing failed", e);
}

// ❌ Wrong — silent catch
try {
  process();
} catch (IOException e) {}

// ✅ Acceptable if intentional
try {
  process();
} catch (IOException e) {
  // Expected when file doesn't exist — caller handles null return
}
```

### 5.3 Static Members
Always qualify with the class name, not an instance.
```java
// ✅ Correct
Foo.aStaticMethod();

// ❌ Wrong
fooInstance.aStaticMethod();
```

### 5.4 Logging
Use SLF4J — never `System.out.println` or `System.err.println` in production code.
```java
// ✅ Correct
private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
logger.info("Processing order {}", orderId);

// ❌ Wrong
System.out.println("Processing order " + orderId);
```

### 5.5 Numeric Literals
Use uppercase `L` for long literals.
```java
// ✅ Correct
long timeout = 3000000000L;

// ❌ Wrong — lowercase l looks like 1
long timeout = 3000000000l;
```

### 5.6 No Finalizers
Do not override `Object.finalize()`.

---

## 6. Security

### 6.1 No Hardcoded Credentials
```java
// ❌ Wrong
String password = "secret123";
String apiKey = "sk-abc123";

// ✅ Correct — read from environment or secrets manager
String password = System.getenv("DB_PASSWORD");
```

### 6.2 SQL — No String Concatenation
Always use parameterized queries.
```java
// ❌ Wrong — SQL injection risk
String query = "SELECT * FROM users WHERE id = " + userId;

// ✅ Correct
PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?");
stmt.setLong(1, userId);
```

### 6.3 Input Validation
Validate all user-supplied inputs before processing.

---

## 7. Javadoc

Required on all `public` classes, methods, and fields (except self-explanatory getters/setters).

```java
// ✅ Correct
/**
 * Returns the user associated with the given ID.
 *
 * @param userId the unique identifier of the user
 * @return the user, or {@code null} if not found
 * @throws IllegalArgumentException if userId is negative
 */
public User getUserById(long userId) { ... }

// ❌ Wrong
/** @return the user */
public User getUserById(long userId) { ... }
```

Block tag order: `@param` → `@return` → `@throws` → `@deprecated`

---

## 8. Common Violations — Severity Reference

| Violation | Severity |
|-----------|----------|
| Wildcard import (`import java.util.*`) | 🟡 Medium |
| `System.out.println` in production code | 🟡 Medium |
| Silent catch block with no comment | 🟠 High |
| Hardcoded credentials or API keys | 🔴 Critical |
| SQL string concatenation (injection risk) | 🔴 Critical |
| Missing `@Override` annotation | 🔵 Low |
| `int a, b;` multiple variable declaration | 🔵 Low |
| Missing Javadoc on public method | 🔵 Low |
| Lowercase `l` suffix on long literal | 🔵 Low |
| Tab indentation instead of spaces | 🟡 Medium |
| Line exceeds 100 characters | 🔵 Low |
| `mName` or `name_` style field names | 🟡 Medium |
| No braces on single-line `if`/`for` | 🟡 Medium |
| Static method called on instance | 🔵 Low |

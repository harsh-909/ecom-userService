# OAuth2 Authorization Code Flow — Deep Dive

This document explains exactly how an OAuth2 access token is issued by this service,
which classes are involved at each step, and where failures happen and why.

---

## 1. Version Stack

| Layer | Library | Package Namespace |
|---|---|---|
| App framework | Spring Boot 4.0.4 | `org.springframework.boot` |
| Web | Spring MVC (`spring-boot-starter-webmvc`) | `org.springframework.web` |
| Security | Spring Security 7.x | `org.springframework.security` |
| OAuth2 server | Spring Authorization Server 2.x | `org.springframework.security.oauth2.server.authorization` |
| JSON | Jackson 3.x | **`tools.jackson`** (NOT `com.fasterxml.jackson`) |
| JSON annotations | Jackson Annotations 3.x | `tools.jackson.annotation` |
| Database | Spring Data JPA + MySQL | — |
| Password | BCrypt via Spring Security | — |

> **Critical**: Spring Boot 4.x ships Jackson 3.x. The package was renamed from
> `com.fasterxml.jackson` (Jackson 2.x, used by Spring Boot 3.x) to `tools.jackson`
> (Jackson 3.x). All `ObjectMapper`, `JsonMapper`, `TypeReference`, `@JsonDeserialize`
> etc. must use `tools.jackson.*` in this project.

---

## 2. The OAuth2 Authorization Code Flow — Step by Step

This is the exact sequence that happens when Postman requests a token using the
Authorization Code grant type.

```
Postman / Client                 Your Spring App                     MySQL DB
     |                                |                                   |
     |-- GET /oauth2/authorize ------>|                                   |
     |   ?client_id=...              |                                   |
     |   &response_type=code         |  [Step 1] Validate client        |
     |   &scope=openid               |  JpaRegisteredClientRepository    |
     |   &redirect_uri=...           |  .findByClientId() ─────────────>|
     |   &state=xyz                  |  reads `client` table <──────────|
     |                               |                                   |
     |<-- 302 Redirect to /login ----|                                   |
     |                               |                                   |
     |-- POST /login ---------------->|                                   |
     |   username=email              |  [Step 2] Authenticate user      |
     |   password=password           |  CustomUserDetailsService         |
     |                               |  .loadUserByUsername(email) ────>|
     |                               |  reads `user` table <────────────|
     |                               |  returns CustomUserDetails        |
     |                               |  Spring creates                   |
     |                               |  UsernamePasswordAuthenticationToken
     |                               |                                   |
     |<-- 302 Redirect to /oauth2/authorize                              |
     |                               |                                   |
     |-- GET /oauth2/authorize ------>|                                   |
     |   (now authenticated)         |  [Step 3] Save pending auth      |
     |                               |  JpaOAuth2AuthorizationService    |
     |                               |  .save(authorization) ──────────>|
     |                               |  writes `authorization` table     |
     |                               |  (state + Authentication object)  |
     |                               |                                   |
     |<-- 200 Consent Page -----------|                                   |
     |   (checkbox: allow ADMIN?)    |                                   |
     |                               |                                   |
     |-- POST /oauth2/authorize ----->|                                   |
     |   scope=ADMIN&consent=true    |  [Step 4] Save consent           |
     |                               |  JpaOAuth2AuthorizationConsentService
     |                               |  .save(consent) ────────────────>|
     |                               |  writes `authorization_consent`   |
     |                               |                                   |
     |                               |  [Step 5] Issue authorization code|
     |                               |  JpaOAuth2AuthorizationService    |
     |                               |  .findByToken(state, STATE) ────>|
     |                               |  reads row by state column <──────|
     |                               |  DESERIALIZES attributes JSON     |
     |                               |  (reconstructs Authentication)    |
     |                               |  .save(authorizationWithCode) ──>|
     |                               |  writes auth_code into same row   |
     |                               |                                   |
     |<-- 302 redirect_uri?code=ABC -|                                   |
     |                               |                                   |
     |-- POST /oauth2/token ---------->|                                   |
     |   grant_type=authorization_code|  [Step 6] Exchange code for token|
     |   code=ABC                    |  JpaOAuth2AuthorizationService    |
     |   client_id=...               |  .findByToken(ABC, CODE) ───────>|
     |   client_secret=...           |  reads row by auth_code column <─|
     |                               |  DESERIALIZES attributes JSON     |
     |                               |  (reconstructs Authentication)    |
     |                               |  Signs JWT with RSA key           |
     |                               |  .save(authorizationWithToken) ─>|
     |                               |                                   |
     |<-- 200 {"access_token": "..."}|                                   |
```

---

## 3. Class Reference — What Each Class Does

### Your Classes

| Class | Package | Role |
|---|---|---|
| `CustomUserDetailsService` | `service/` | Implements Spring's `UserDetailsService`. Called at login step. Loads user from DB by email, wraps it in `CustomUserDetails`. |
| `CustomUserDetails` | `models/` | Implements Spring's `UserDetails`. Holds the logged-in user's email, BCrypt password, and list of roles. This object gets serialized to JSON and stored in the DB. |
| `CustomGrantedAuthority` | `models/` | Implements Spring's `GrantedAuthority`. Wraps one role name (e.g., "ADMIN"). Stored as part of `CustomUserDetails`. |
| `JpaRegisteredClientRepository` | `security/repository/` | Implements `RegisteredClientRepository`. Reads/writes the `client` table. Converts between Spring's `RegisteredClient` and your `Client` JPA entity. |
| `JpaOAuth2AuthorizationService` | `security/service/` | Implements `OAuth2AuthorizationService`. **Most complex class.** Reads/writes the `authorization` table. Serializes the full OAuth2 state (including your principal) to JSON and back. |
| `JpaOAuth2AuthorizationConsentService` | `security/service/` | Implements `OAuth2AuthorizationConsentService`. Reads/writes the `authorization_consent` table. Stores which scopes the user approved. |

### Spring Framework Classes

| Class | What it does in your flow |
|---|---|
| `UsernamePasswordAuthenticationToken` | Spring's representation of a logged-in user. Has `principal` (your `CustomUserDetails`) and `authorities` (your `CustomGrantedAuthority` list). |
| `OAuth2Authorization` | Spring's in-memory representation of a full authorization record. Contains the state, auth code, access token, and a Map of attributes including the authentication. |
| `RegisteredClient` | Spring's in-memory representation of a registered OAuth2 client. Your `JpaRegisteredClientRepository` builds this from the `client` table. |
| `OAuth2AuthorizationConsent` | Represents which scopes the user approved for which client. |
| `SecurityJacksonModules` | Provides Jackson modules that know how to serialize/deserialize Spring Security objects like `UsernamePasswordAuthenticationToken`, `SimpleGrantedAuthority`, etc. |

### Database Tables

| Table | Managed by | Stores |
|---|---|---|
| `client` | `JpaRegisteredClientRepository` | Your registered OAuth2 client (client_id, secret, redirect_uri, scopes) |
| `authorization` | `JpaOAuth2AuthorizationService` | In-progress and completed authorizations. The `attributes` column is a JSON blob containing the full `Authentication` object (including your `CustomUserDetails`). |
| `authorization_consent` | `JpaOAuth2AuthorizationConsentService` | Which scopes user approved for which client |

---

## 4. The Exact Location of the Error

The error occurred inside **`JpaOAuth2AuthorizationService`**, specifically in the
**`toObject()` method**, which is called by both `findById()` and `findByToken()`.

```
Step 5 (consent submitted) or Step 6 (code exchanged):
  → JpaOAuth2AuthorizationService.findByToken(token, tokenType)
    → authorizationRepository.findByState(token)           [reads DB row]
    → toObject(entity)                                      [converts to Spring object]
      → parseMap(entity.getAttributes())                   [deserializes JSON]
        → objectMapper.readValue(json, Map<String,Object>) [Jackson reads JSON]
          → encounters key "java.security.Principal"
          → value has type tag: "org.ecom.userService.models.CustomUserDetails"
          → asks PolymorphicTypeValidator: is this safe?
            → DENIED  ← ERROR THROWN HERE
```

### Why It Was Denied

The `attributes` column JSON looks roughly like this (simplified):

```json
{
  "java.security.Principal": [
    "org.ecom.userService.models.CustomUserDetails",
    {
      "username": "user@example.com",
      "password": "$2a$...",
      "authorities": [...],
      "isEnabled": true
    }
  ]
}
```

When reading this back, Jackson sees:
- **Base type**: `java.lang.Object` (because `Map<String, Object>` — the value type is `Object`)
- **Candidate subtype**: `org.ecom.userService.models.CustomUserDetails`

It asks the `PolymorphicTypeValidator`: *"Should I trust this type?"*

The original validator was:
```java
BasicPolymorphicTypeValidator.builder()
    .allowIfSubType("org.ecom.userService")   // string prefix check
    .allowIfSubType("org.springframework.security")
    ...
    .build();
```

**Why this failed in Jackson 3.x:** String-prefix rules in `allowIfSubType(String)` only populate `_validSubTypePrefixes`. But in Jackson 3.x, when the base type is `java.lang.Object`, the validator still returns `INDETERMINATE` from `validateSubClassName`, then tries to load the class, then calls `validateSubType`. In `validateSubType`, string-prefix rules are NOT checked — only explicit class-based rules are. Since there are no class-based rules matching `CustomUserDetails`, the final result is `INDETERMINATE`, which Jackson treats as **denied**.

**Partial fix (not sufficient alone):**
```java
BasicPolymorphicTypeValidator.builder()
    .allowIfSubType(Object.class)   // allows ANY class (since everything is-a Object)
    .build();
```

`allowIfSubType(Object.class)` adds `Object.class` to `_validSubTypes`. In `validateSubType`,
it checks `Object.class.isAssignableFrom(CustomUserDetails.class)` → `true` → **ALLOWED**.

---

### 4b. Why Every `allowAll` Workaround Failed — The Real Root Cause (Verified by Bytecode)

Every attempted fix — `allowAll` custom validator, `polymorphicTypeValidator(allowAll)`, `activateDefaultTyping(allowAll)` — still produced `BasicPolymorphicTypeValidator denied`. The reason was discovered by decompiling `spring-security-core-7.0.4.jar`.

**What `SecurityJacksonModules.getModules(classLoader)` actually does:**

`getModules(classLoader)` delegates to `getModules(classLoader, null)` which:

1. Loads all Security modules (`CoreJacksonModule`, etc.)
2. Creates a fresh `BasicPolymorphicTypeValidator.Builder`
3. Calls `module.configurePolymorphicTypeValidator(builder)` on each `SecurityJacksonModule` — each adds only its own types (e.g. `UsernamePasswordAuthenticationToken.class`, `FactorGrantedAuthority.class`, etc.)
4. Appends an anonymous `SecurityJacksonModules$1` module to the list whose `setupModule` does:
   ```java
   ((MapperBuilder) context.getOwner())
       .activateDefaultTyping(ptvBuilder.build(), NON_FINAL, PROPERTY);
   ```

This anonymous module runs **last during `JsonMapper.builder().build()`**, directly modifying the `MapperBuilder` with a `BasicPolymorphicTypeValidator` containing only Spring Security types. It overwrites any `polymorphicTypeValidator` or `activateDefaultTyping` we placed on the builder. `CustomUserDetails` (`org.ecom.*`) is not in that list → denied.

Note also: `context.getOwner()` is cast to `MapperBuilder`, not `ObjectMapper`. Modules in Jackson 3.x run during build and modify the in-progress builder directly.

**The correct fix** — use the two-argument overload `getModules(classLoader, ptvBuilder)`:

```java
// Add your custom types to the shared builder before passing it to getModules.
// Each Security module then adds its own types via configurePolymorphicTypeValidator(builder).
// The combined validator is installed by the anonymous module's setupModule.
BasicPolymorphicTypeValidator.Builder ptvBuilder = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType(CustomUserDetails.class)
        .allowIfSubType(CustomGrantedAuthority.class);

ClassLoader classLoader = JpaOAuth2AuthorizationService.class.getClassLoader();
this.objectMapper = JsonMapper.builder()
        .addModules(SecurityJacksonModules.getModules(classLoader, ptvBuilder))
        .build();
```

`getModules(classLoader, ptvBuilder)` passes the same `ptvBuilder` through `configurePolymorphicTypeValidator` on every module. The anonymous module's `setupModule` then installs `ptvBuilder.build()` — now containing both Spring Security types AND our custom types — as the definitive validator. No `allowAll` hack needed.

---

## 5. Why `CustomUserDetails` Also Needed `@JsonAutoDetect`

`CustomUserDetails` has **no setters** — only getters (from `UserDetails` interface).

Jackson's default behavior is:
- **Serialize**: reads via public getters ✓ (works)
- **Deserialize**: writes via public setters ✗ (no setters → fails)

Without setters, Jackson couldn't reconstruct `CustomUserDetails` from the stored JSON.

The fix adds `@JsonAutoDetect(fieldVisibility = ANY)`, which tells Jackson:
> "Read and write private fields directly instead of using getters/setters."

This means Jackson can set `username`, `password`, `authorities`, etc. directly into
the object's private fields when deserializing. The no-arg constructor `CustomUserDetails(){}`
is used to create the empty instance first, then fields are populated.

---

## 6. Why `SecurityJacksonModules` Must Be Registered

`SecurityJacksonModules.getModules(classLoader)` returns a list of Jackson modules
that teach Jackson how to handle Spring Security's own types:

- `UsernamePasswordAuthenticationToken` — the logged-in user wrapper
- `SimpleGrantedAuthority` — Spring's built-in role class
- `Collections$UnmodifiableList` — Spring wraps authority lists in this
- `LinkedHashMap`, `HashMap` — used internally by Spring for claims
- Many other Spring Security internals

Without these modules, Jackson wouldn't know how to serialize/deserialize Spring's
`Authentication` objects, and the `attributes` column would fail to write or read.

---

## 7. Why the `@JsonDeserialize` Annotation Uses `tools.jackson`

```java
// CORRECT for Spring Boot 4.x (Jackson 3.x)
import tools.jackson.databind.annotation.JsonDeserialize;

// WRONG for Spring Boot 4.x — this is Jackson 2.x (Spring Boot 3.x)
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
```

The `@JsonDeserialize` annotation tells Jackson: *"This class is a valid deserialization
target."* Without it, when Jackson encounters the type tag in the JSON, it might not
attempt to deserialize into this class at all.

The `@JsonIgnoreProperties(ignoreUnknown = true)` annotation can stay as
`com.fasterxml.jackson.annotation` — the Jackson annotation module (`jackson-annotations`)
maintains backward-compat imports for `com.fasterxml.jackson.annotation.*`.

---

## 8. What Happens If You Don't Clear Stale DB Data

Every time you attempt the flow and it fails mid-way, a partial row gets written to
the `authorization` table. That row has:
- A `state` value (from the browser redirect)
- An `attributes` JSON blob containing your `CustomUserDetails`

If the code had a bug when that row was written, the stored JSON might be in the wrong
format. When you fix the code and retry, **Spring reads that old row** when you submit
consent, gets the malformed JSON, and throws the same error even though your code is
now correct.

**Fix**: After every code change, run this in MySQL before retrying:
```sql
TRUNCATE TABLE authorization;
TRUNCATE TABLE authorization_consent;
```

Also make sure to do a full rebuild:
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

---

## 9. Postman Setup Checklist

To get a token in Postman:

1. **Client must exist in DB** — insert a row into the `client` table manually or via a
   `CommandLineRunner`. The client needs: `id`, `client_id`, `client_secret` (BCrypt encoded),
   `authorization_grant_types` = `authorization_code`, `redirect_uris`, `scopes`,
   `client_settings` (JSON), `token_settings` (JSON).

2. **User must exist in DB** — sign up via `POST /users/signUp` with a role.

3. **Postman OAuth2 config**:
   - Grant Type: Authorization Code
   - Auth URL: `http://localhost:8080/oauth2/authorize`
   - Token URL: `http://localhost:8080/oauth2/token`
   - Client ID: matches `client_id` in DB
   - Client Secret: the plain-text secret (BCrypt encoding is done by Spring internally... actually for `client_secret_basic` the secret in DB must be stored as `{noop}secret` or BCrypt-encoded)
   - Scope: `openid` (or whatever you stored in the `client` table)
   - Redirect URI: must exactly match `redirect_uris` in DB

4. **After getting the token**, use it as `Authorization: Bearer <token>` on protected endpoints.

---

## 10. Full Error Chain Summary

```
Root cause:  Jackson 3.x (tools.jackson) PolymorphicTypeValidator  
             denied deserialization of CustomUserDetails

Why (layer 1): String-prefix allowIfSubType("org.ecom.userService") only
               affects validateSubClassName(), not validateSubType().
               In Jackson 3.x with Object base type, validateSubType()
               is the decisive check — and there was no matching class rule.

Why (layer 2): SecurityJacksonModules.getModules(classLoader) appends an
               anonymous SecurityJacksonModules$1 module that runs LAST during
               build(). Its setupModule casts context.getOwner() to MapperBuilder
               and calls mapperBuilder.activateDefaultTyping(ptvBuilder.build(),
               NON_FINAL, PROPERTY). This overwrites any polymorphicTypeValidator
               or activateDefaultTyping we set on the builder. The installed
               BasicPolymorphicTypeValidator only contains Spring Security types.
               CustomUserDetails (org.ecom.*) is not in it → denied.

Fix:           Use the two-argument overload:
               SecurityJacksonModules.getModules(classLoader, ptvBuilder)
               where ptvBuilder has .allowIfSubType(CustomUserDetails.class)
               and .allowIfSubType(CustomGrantedAuthority.class) added first.
               Each module then appends its own types to the same ptvBuilder.
               The anonymous module installs the combined validator.

Secondary:   CustomUserDetails had no setters and no @JsonCreator.
             Fix: @JsonAutoDetect(fieldVisibility = ANY) enables
             direct field access during deserialization.

Pre-req:     TRUNCATE authorization table before retesting after any fix,
             otherwise old malformed JSON rows trigger the same error.
```

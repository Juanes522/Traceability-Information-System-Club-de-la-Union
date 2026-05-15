# PWA Push Fix + pnpm Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix native browser push notifications (wrong payload format) and migrate the Angular frontend from npm to pnpm with Corepack enforcement.

**Architecture:** Two independent tasks. Task 1 is a one-line backend fix in `PushNotificationService.java` — wrapping the push payload under the `"notification"` key that Angular's ngsw expects. Tasks 2–6 migrate the Angular frontend to pnpm by updating `package.json`, creating `.npmrc`, generating `pnpm-lock.yaml`, and removing `package-lock.json`.

**Tech Stack:** Spring Boot (Java), Angular 16, pnpm 10+, Node.js Corepack

---

## Prerequisites (manual — user runs these ONCE before starting)

```powershell
# In any terminal (PowerShell or CMD) — run as administrator if needed
corepack enable
corepack prepare pnpm@latest --activate
pnpm --version    # must print something like 10.x.x
```

If `corepack` is not found, Node 18.20.4 includes it — just run `corepack enable`.

---

## Task 1: Fix PWA Push Notification Payload

**Files:**
- Modify: `TraceabilitySystemClubUnion/src/main/java/co/unbosque/service/PushNotificationService.java:42`

- [ ] **Step 1: Open the file and locate the payload line**

In `TraceabilitySystemClubUnion/src/main/java/co/unbosque/service/PushNotificationService.java`, find line 42:

```java
String payload = "{\"title\":\"" + escapeJson(title) + "\",\"body\":\"" + escapeJson(body) + "\"}";
```

- [ ] **Step 2: Replace with the ngsw-compatible payload format**

Replace that line with:

```java
String payload = "{\"notification\":{\"title\":\"" + escapeJson(title)
    + "\",\"body\":\"" + escapeJson(body)
    + "\",\"icon\":\"/assets/ClubIcon.png\"}}";
```

- [ ] **Step 3: Restart the backend and verify it compiles**

```powershell
cd TraceabilitySystemClubUnion
./mvnw spring-boot:run
```

Expected: Backend starts on port 8080 with no compilation errors.

- [ ] **Step 4: Build the Angular frontend for production**

```powershell
cd AngularTraceabilitySystem
npm run build
```

Expected: `dist/angular-traceability-system/` is created with no errors.

- [ ] **Step 5: Serve the production build**

```powershell
npx serve dist/angular-traceability-system -l 4200
```

Expected output:
```
Serving!
Local:    http://localhost:4200
```

- [ ] **Step 6: Test push notification end-to-end**

1. Open `http://localhost:4200` in Chrome (normal window)
2. Log in as a partner (ROLE_PARTNER)
3. When the browser prompts for notification permission → click **Permitir**
4. Open `http://localhost:4200` in a second Chrome window (incognito)
5. Log in as a manager (ROLE_MANAGER or ROLE_ADMIN)
6. Register a new consumption for that partner
7. Expected: A native OS popup notification appears in the partner's Chrome window with title "Nuevo cargo registrado"

- [ ] **Step 7: Commit the fix**

```powershell
cd ..
git add TraceabilitySystemClubUnion/src/main/java/co/unbosque/service/PushNotificationService.java
git commit -m "fix: wrap push payload in notification key for ngsw compatibility"
```

---

## Task 2: Add packageManager and engines to package.json

**Files:**
- Modify: `AngularTraceabilitySystem/package.json`

- [ ] **Step 1: Add `packageManager` and `engines` fields**

Open `AngularTraceabilitySystem/package.json`. After `"private": true,` add:

```json
"packageManager": "pnpm@10.12.1",
"engines": {
  "node": ">=18.20.4",
  "pnpm": ">=10"
},
```

The full top of the file must look like:

```json
{
  "name": "angular-traceability-system",
  "version": "0.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "watch": "ng build --watch --configuration development",
    "test": "ng test"
  },
  "private": true,
  "packageManager": "pnpm@10.12.1",
  "engines": {
    "node": ">=18.20.4",
    "pnpm": ">=10"
  },
  "dependencies": {
```

- [ ] **Step 2: Verify JSON is valid**

```powershell
cd AngularTraceabilitySystem
node -e "JSON.parse(require('fs').readFileSync('package.json','utf8')); console.log('JSON valid')"
```

Expected output: `JSON valid`

---

## Task 3: Create .npmrc

**Files:**
- Create: `AngularTraceabilitySystem/.npmrc`

- [ ] **Step 1: Create the file with two directives**

Create `AngularTraceabilitySystem/.npmrc` with this exact content:

```
engine-strict=true
shamefully-hoist=true
```

- `engine-strict=true` — install fails if the package manager or Node version doesn't match `engines`
- `shamefully-hoist=true` — hoists packages to `node_modules` root, required for Angular's build system

---

## Task 4: Generate pnpm lockfile and clean npm artifacts

**Files:**
- Create: `AngularTraceabilitySystem/pnpm-lock.yaml` (generated)
- Delete: `AngularTraceabilitySystem/package-lock.json`
- Delete: `AngularTraceabilitySystem/node_modules/` (will be regenerated)

- [ ] **Step 1: Convert package-lock.json to pnpm-lock.yaml**

```powershell
cd AngularTraceabilitySystem
pnpm import
```

Expected: `pnpm-lock.yaml` is created in the same directory. No errors.

- [ ] **Step 2: Remove npm artifacts**

```powershell
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json
```

- [ ] **Step 3: Install dependencies with pnpm**

```powershell
pnpm install
```

Expected: Dependencies install from `pnpm-lock.yaml`. Output shows packages installed, no `npm warn` lines. A `node_modules/` directory is recreated.

If you see peer dependency warnings, that is normal for Angular 16. Errors (not warnings) must be resolved before continuing.

---

## Task 5: Update .gitignore

**Files:**
- Modify: `AngularTraceabilitySystem/.gitignore`

- [ ] **Step 1: Add package-lock.json exclusion**

Open `AngularTraceabilitySystem/.gitignore` and add at the end:

```
# pnpm — exclude npm lockfile, track pnpm lockfile
package-lock.json
```

`pnpm-lock.yaml` does NOT need an explicit include line — git tracks all files not excluded.

---

## Task 6: Verify pnpm enforcement and run dev server

- [ ] **Step 1: Verify dev server starts**

```powershell
cd AngularTraceabilitySystem
pnpm start
```

Expected: Angular dev server starts on `http://localhost:4200`. No compilation errors.

- [ ] **Step 2: Verify production build works**

Stop the dev server (Ctrl+C), then:

```powershell
pnpm build
```

Expected: `dist/angular-traceability-system/` is generated. Output ends with `Build at:` line.

- [ ] **Step 3: Verify npm is blocked**

```powershell
npm install
```

Expected: Error similar to one of these (depending on Corepack version):
```
npm error: This project is configured to use pnpm
```
or:
```
ERR_PNPM_WRONG_PACKAGE_MANAGER_STRICT  The package manager constraint in the project's `engines.pnpm` field does not match...
```

Any error message that prevents `npm install` from succeeding is the correct outcome.

- [ ] **Step 4: Commit all pnpm migration files**

```powershell
cd ..
git add AngularTraceabilitySystem/package.json
git add AngularTraceabilitySystem/.npmrc
git add AngularTraceabilitySystem/.gitignore
git add AngularTraceabilitySystem/pnpm-lock.yaml
git commit -m "chore: migrate from npm to pnpm with Corepack enforcement"
```

---

## Summary of Changes

| File | Change |
|---|---|
| `TraceabilitySystemClubUnion/.../PushNotificationService.java` | Payload wrapped in `"notification"` key |
| `AngularTraceabilitySystem/package.json` | Added `packageManager` + `engines` |
| `AngularTraceabilitySystem/.npmrc` | Created — engine-strict + shamefully-hoist |
| `AngularTraceabilitySystem/.gitignore` | Added `package-lock.json` exclusion |
| `AngularTraceabilitySystem/pnpm-lock.yaml` | Created — new lockfile |
| `AngularTraceabilitySystem/package-lock.json` | Deleted |
| `AngularTraceabilitySystem/node_modules/` | Regenerated by pnpm |

# PWA Push Fix + pnpm Migration Design

**Date:** 2026-05-14
**Scope:** Two independent subsystems — fix browser push notifications and migrate from npm to pnpm with enforcement.

---

## Subsystem 1: PWA Push Notification Fix

### Problem

The Angular service worker (`ngsw-worker.js`) only renders native browser notifications when the Web Push payload follows a specific schema. The backend currently sends:

```json
{"title":"Nuevo cargo registrado","body":"Se registró un cargo de $100.00 en Rooftop · Mesa 5"}
```

Angular's ngsw expects:

```json
{
  "notification": {
    "title": "Nuevo cargo registrado",
    "body": "Se registró un cargo de $100.00 en Rooftop · Mesa 5",
    "icon": "/assets/ClubIcon.png"
  }
}
```

Without this wrapper, the service worker receives the push event but silently discards it — no native notification appears. The DB notification record is still created correctly (separate code path), which is why the in-app list works but the browser popup does not.

### Fix

**File:** `TraceabilitySystemClubUnion/src/main/java/co/unbosque/service/PushNotificationService.java`

Change the `sendToPartner` method to wrap the payload in a `"notification"` key and add the club icon:

```java
String payload = "{\"notification\":{\"title\":\"" + escapeJson(title)
    + "\",\"body\":\"" + escapeJson(body)
    + "\",\"icon\":\"/assets/ClubIcon.png\"}}";
```

### Verification

1. Run `ng build` and serve `dist/` with `npx serve dist/angular-traceability-system -l 4200`
2. Log in as a partner (ROLE_PARTNER) → allow notifications when prompted
3. Log in as a manager in an incognito tab
4. Register a consumption for that partner
5. A native browser notification popup must appear on the partner's browser

---

## Subsystem 2: npm → pnpm Migration

### Goal

Replace npm with pnpm as the package manager for the Angular frontend. Block accidental use of npm via Corepack enforcement and engine constraints.

### Why pnpm

- Faster installs (content-addressable store, hard links)
- Strict dependency isolation by default (no phantom dependencies)
- `only-allow` enforcement via Corepack `packageManager` field
- Actively maintained, no security incidents affecting pnpm's supply chain

### Files Changed

| File | Action | Purpose |
|---|---|---|
| `package.json` | Modify | Add `packageManager` and `engines` fields |
| `.npmrc` | Create | Angular compatibility + engine enforcement |
| `pnpm-lock.yaml` | Create | Generated lockfile for pnpm |
| `package-lock.json` | Delete | No longer used |
| `.gitignore` | Modify | Exclude `package-lock.json`, include `pnpm-lock.yaml` |

### Design Details

**`package.json` additions:**
```json
{
  "packageManager": "pnpm@10.12.1",
  "engines": {
    "node": ">=18.20.4",
    "pnpm": ">=10"
  }
}
```

The `packageManager` field is read by Node.js Corepack (built into Node 16.9+). When Corepack is active and a user runs `npm install`, Corepack intercepts and errors with: *"This project is configured to use pnpm"*.

**`.npmrc` contents:**
```
engine-strict=true
shamefully-hoist=true
```

- `engine-strict=true` — causes install to fail if the engine constraints in `engines` are not satisfied
- `shamefully-hoist=true` — required for Angular's build system, which expects packages hoisted to `node_modules` root similarly to npm's default behavior

**Migration steps (one-time, manual prerequisites):**
```bash
# 1. Enable Corepack (built into Node 16.9+)
corepack enable

# 2. Activate pnpm via Corepack
corepack prepare pnpm@latest --activate

# 3. Inside AngularTraceabilitySystem/
pnpm import          # converts package-lock.json → pnpm-lock.yaml
rm -rf node_modules package-lock.json
pnpm install         # install from pnpm-lock.yaml
```

**`.gitignore` changes:**
```
# Remove or comment out any pnpm-lock.yaml exclusion
# Add:
package-lock.json
```

### Enforcement Behavior After Migration

| Command | Result |
|---|---|
| `pnpm install` | Works normally |
| `npm install` | Corepack error: must use pnpm |
| `yarn install` | Corepack error: must use pnpm |
| `pnpm start` | Works (maps to `ng serve`) |
| `pnpm build` | Works (maps to `ng build`) |
| `pnpm test` | Works (maps to `ng test`) |

### Verification

```bash
pnpm install        # must complete without errors
pnpm start          # ng serve must start on :4200
pnpm build          # ng build must produce dist/
npm install         # must fail with Corepack/engine error
```

---

## Order of Execution

1. **PWA fix** (backend only, no reinstall required)
2. **pnpm migration** (frontend, requires manual Corepack setup by user first)

These are independent and can be done in either order, but PWA fix is faster and has no prerequisites.

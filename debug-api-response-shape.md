# Debug Session: API Response Shape

Status: [OPEN]

## Symptoms
- Supplier list throws `Cannot read properties of undefined (reading 'map')` at `mockApi.ts`.
- Dashboard throws `result.map is not a function` at `dashboard.ts`.
- Vue mounted hooks report unhandled errors in `SupplierListView` and `DashboardView`.

## Hypotheses
1. Backend pageable responses use a wrapper shape different from the frontend assumption, such as `{ records }` vs `{ list }` or raw array.
2. Dashboard endpoints return object-shaped data rather than arrays, causing direct `.map()` calls to fail.
3. Axios interceptor unwraps `Result.data`, but some endpoints return binary, empty, or nested response bodies that are not normalized.
4. Component mounted hooks lack defensive `try/finally`, so one API shape mismatch becomes an unhandled mounted error.
5. Real backend endpoints may not match the selected API document version, requiring compatibility normalization.

## Instrumentation Plan
- Add temporary network-report instrumentation in request and API adapter boundaries.
- Collect actual response shapes for list and dashboard endpoints.
- Confirm which response shape causes `.map()` failures before changing normalization logic.

## Pre-fix Evidence
- `.dbg/trae-debug-log-api-response-shape.ndjson` lines 1-3, 5-7: dashboard endpoints return the Vite `index.html` string instead of JSON arrays.
- `.dbg/trae-debug-log-api-response-shape.ndjson` line 8: supplier page response is also HTML text, so `result.records` is undefined and `result.records.map` fails.

## Evidence Conclusion
- Confirmed root cause: Vite dev server is serving SPA fallback HTML for `/api/**` because no development proxy is configured, so Axios receives HTML strings rather than backend JSON.
- Secondary issue: API adapter code assumes arrays/pages without defensive normalization, so malformed responses surface as `.map` errors instead of actionable API errors.

## Fix Applied
- Added Vite dev server proxy for `/api` to `http://localhost:8080`.
- Added `request.ts` detection for HTML responses to surface a clear proxy/backend error instead of `.map` failures.
- Added API response normalization helpers for array and page responses.
- Updated dashboard, supplier, order, logistics, quality, settlement, notification, attachment, import/export and compatibility adapter paths to normalize before mapping.

## Build Verification
- `npm run build` completed successfully after the fix.

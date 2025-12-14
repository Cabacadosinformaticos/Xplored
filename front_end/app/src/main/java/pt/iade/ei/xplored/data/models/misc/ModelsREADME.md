# Xplored Kotlin Models (DB mirrors)

These Kotlin data classes live under `pt.iade.ei.xplored.models` and mirror the SQL schema from **TODO.txt**.
They intentionally use `String` for IDs and date/times to avoid forcing extra libraries right now. Later you can:
- switch IDs to `UUID`,
- switch timestamps/datetimes to `Instant`/`LocalDateTime`,
- annotate with your JSON library (@Serializable, Moshi, Gson).

All database defaults are represented via Kotlin default values where it makes sense (e.g., `active = true`, `status = PENDING`).

**Tables covered:**
users, user_settings, password_reset_tokens, categories, places, business_accounts, reviews, reactions,
photos, visits, receipts, points_ledger, coupons, redemptions, pedipapers, route_stops, route_participations,
moderation_requests, support_tickets, problem_reports.

**Enums covered:**
UserRole, PrivacyLevel, PlaceStatus, ReviewStatus, PhotoStatus, VisitStatus, ReceiptStatus, ModerationStatus,
ReactionType, VisitVerificationType, LedgerSourceType, DiscountType, CodeType, TicketStatus, TicketPriority,
ReportType, ModerationEntityType.

You can copy the folder into your Android Studio project at `app/src/main/java/pt/iade/ei/xplored/models`.

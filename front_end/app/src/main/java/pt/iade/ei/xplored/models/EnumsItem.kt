package pt.iade.ei.xplored.models

enum class UserRole { USER, BUSINESS, ADMIN }
enum class PrivacyLevel { PUBLIC, FRIENDS, PRIVATE }

enum class PlaceStatus { PENDING, APPROVED, REJECTED }
enum class ReviewStatus { PENDING, APPROVED, REJECTED }
enum class PhotoStatus { PENDING, APPROVED, REJECTED }   // <- keep this one
enum class VisitStatus { PENDING, APPROVED, REJECTED }
enum class ReceiptStatus { PENDING, APPROVED, REJECTED }
enum class ModerationStatus { PENDING, APPROVED, REJECTED }

enum class ReactionType { USEFUL, NOT_USEFUL }
enum class VisitVerificationType { PHOTO, RECEIPT, PEDIPAPER }
enum class LedgerSourceType { VISIT, RECEIPT, PEDIPAPER, REDEMPTION, ADMIN_ADJUST }

enum class DiscountType { PERCENT, FIXED }
enum class CodeType { NUMERIC, QR, BARCODE }

enum class TicketStatus { OPEN, IN_PROGRESS, CLOSED }
enum class TicketPriority { LOW, MEDIUM, HIGH }

enum class ReportType { MAP, LOCATION, PHOTO, REVIEW, OTHER }
enum class ModerationEntityType { PLACE, REVIEW, PHOTO, RECEIPT }

// NEW: centralize the discriminator here too
enum class PhotoKind { AVATAR, GALLERY }

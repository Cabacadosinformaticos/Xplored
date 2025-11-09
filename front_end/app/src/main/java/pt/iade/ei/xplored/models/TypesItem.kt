package pt.iade.ei.xplored.models

/**
 * Simple aliases to make intent clearer.
 * We keep IDs/dates as String for now to avoid forcing java.time or other libs.
 * In the future we can migrate to UUID/Instant with JSON adapters.
 */
typealias IdString = String
typealias TimestampString = String   // e.g., "2025-10-22T11:00:00Z"
typealias DateString = String        // e.g., "2025-12-31"

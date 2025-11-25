# Dev UI - Curated Data Management Tool

Development mode application for managing curated data in the humanitarian-logistics system. 
**Identical UI to main app, but with duplicate Facebook link detection for data collection.**

## Features

### 1. **Data Entry Tab** (✏️ Data Entry)
Manual data collection with identical UI to main app:
- **Post Content**: Text content of the post
- **Author**: Person/account that created the post
- **Disaster Keyword**: Disaster type/category (e.g., #yagi, #matmo, #flood)
- **Relief Category**: Type of relief (FOOD, WATER, MEDICAL, SHELTER, CLOTHING, ENERGY, OTHER)
- **Sentiment**: Emotional tone (POSITIVE, NEGATIVE, NEUTRAL)
- **Confidence**: Certainty score (0.0-1.0)

All data is saved to `humanitarian_logistics_curated.db`

### 2. **Web Crawler Tab** (🌐 Web Crawler)
Crawl data from Facebook URLs with **duplicate link detection**:
- **Post URLs**: Paste Facebook post links (1 per line)
- **Disaster Type**: Select disaster category (YAGI, MATMO, FLOOD, DISASTER)
- **Duplicate Check**: ✅ Automatically prevents adding same link twice
  - Shows "❌ DUPLICATE - Link already in database" if URL exists
  - Continues with next URL if duplicate found

All successful crawls are saved to `humanitarian_logistics_curated.db`

## Database

- **File Location**: `../humanitarian-logistics/humanitarian_linguistics_curated.db`
- **Purpose**: Stores pre-curated data for the main application
- **Schema**: SQLite with unique constraint on facebook_link

## Usage

### Run the Application

```bash
# Option 1: Using run.sh
./run.sh

# Option 2: Build and run manually
mvn clean package -DskipTests
java -jar target/dev-ui.jar
```

### Add Data Manually

1. Click **Data Entry** tab
2. Fill in all required fields
3. Click **✏️  Add Post**
4. Entry is saved to curated database

### Crawl from Facebook URLs

1. Click **Web Crawler** tab
2. Paste Facebook post URLs (one per line)
3. Select disaster type
4. Click **🌐 Crawl Posts from URLs**
5. System automatically:
   - Checks each URL for duplicates
   - Shows "✓ Success" or "❌ DUPLICATE"
   - Adds only new URLs to database

### Example Output
```
[1/3] Processing URL:
  https://www.facebook.com/post/12345...
  ✓ Success - Added to curated DB

[2/3] Processing URL:
  https://www.facebook.com/post/67890...
  ❌ DUPLICATE - Link already in database

SUMMARY:
  ✓ Success: 1
  ❌ Duplicates: 1
```

## Key Differences from Main App

| Feature | Main App | Dev UI |
|---------|----------|--------|
| UI Layout | Same | **Identical** |
| Data Collection | Manual + crawler | Manual + crawler |
| Write Access | User data only | Direct curated DB |
| Duplicate Check | No | ✅ Yes (prevents) |
| Database Used | `humanitarian_logistics_user.db` | `humanitarian_logistics_curated.db` |

## Technical Details

### File Structure
```
dev-ui/
├── pom.xml                                      # Maven config
├── run.sh                                       # Launcher
├── src/main/java/com/humanitarian/devui/
│   ├── DevUIApp.java                           # Entry point
│   ├── database/
│   │   └── CuratedDatabaseManager.java         # DB with duplicate detection
│   ├── model/
│   │   └── DataEntry.java                      # Data model
│   └── ui/
│       ├── DevView.java                        # Main frame
│       ├── DevDataCollectionPanel.java         # Data entry (same as main app)
│       └── DevCrawlControlPanel.java           # Crawler with link check
└── target/
    └── dev-ui.jar                               # JAR (9.3 MB)
```

### Database Schema
```sql
CREATE TABLE posts (
    post_id TEXT PRIMARY KEY,
    content TEXT,
    author TEXT,
    source TEXT,
    created_at TEXT,
    sentiment TEXT,
    confidence REAL,
    relief_category TEXT,
    disaster_keyword TEXT,
    facebook_link TEXT UNIQUE         -- Enforced duplicate prevention
);
```

## How Duplicate Detection Works

1. User pastes Facebook URL in crawler
2. CuratedDatabaseManager checks database
3. If found → Show "❌ DUPLICATE - Link already in database"
4. If not found → Create entry and save

Simple, automatic, no user configuration needed.

## Development

### Build
```bash
mvn clean package -DskipTests
```

### Run
```bash
java -jar target/dev-ui.jar
```

### Modify UI
Files in `src/main/java/com/humanitarian/devui/ui/`

## Integration

Data added in Dev UI flows to main app:
- Dev UI → humanitarian_logistics_curated.db
- Main app loads curated data
- Users see curated data in main app (read-only)

## License

Part of Humanitarian Logistics Analysis System

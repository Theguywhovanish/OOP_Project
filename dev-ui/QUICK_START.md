# Dev UI Quick Start Guide

## What is Dev UI?

Dev UI is a development tool for managing **curated data** in the humanitarian-logistics system.

**Key Point**: Dev UI has the **same interface as the main app**, but:
- ✅ Adds duplicate Facebook link detection
- ✅ Requires specifying disaster type/keyword when adding data
- ✅ Writes directly to `humanitarian_logistics_curated.db`

## Quick Start

### 1. Run Dev UI
```bash
cd /Users/hieunguyen/OOP_Project/dev-ui
./run.sh
```

The application will start with 2 tabs:

### 2. Tab 1: Data Entry (✏️ Data Entry)
- **Identical to main app's data collection panel**
- Fill in:
  - Post Content
  - Author
  - **Disaster Keyword** (required - e.g., #yagi, #matmo)
  - Relief Category
  - Sentiment
  - Confidence
- Click **✏️  Add Post**
- Data saved to `humanitarian_logistics_curated.db`

### 3. Tab 2: Web Crawler (🌐 Web Crawler)
- Paste Facebook post URLs (one per line)
- **Select Disaster Type** (required: YAGI, MATMO, FLOOD, DISASTER)
- Click **🌐 Crawl Posts from URLs**
- System automatically:
  - ✅ Checks each URL for duplicates
  - Shows "✓ Success" if URL is new
  - Shows "❌ DUPLICATE" if URL already exists
  - Only adds new URLs to database

## Example: Adding Data from Crawl

```
Paste URL:
https://www.facebook.com/post/12345

Select Disaster: YAGI

Click Crawl → Result:
[1/1] Processing URL:
  https://www.facebook.com/post/12345...
  ✓ Success - Added to curated DB
```

If you crawl the same URL again:
```
[1/1] Processing URL:
  https://www.facebook.com/post/12345...
  ❌ DUPLICATE - Link already in database

SUMMARY:
  ✓ Success: 0
  ❌ Duplicates: 1
```

## Key Features

| Feature | Details |
|---------|---------|
| **Interface** | Identical to main app - same fields, same layout |
| **Duplicate Detection** | ✅ Automatic - checks Facebook links |
| **Disaster Required** | ✅ Yes - must specify when adding data |
| **Database** | `humanitarian_logistics_curated.db` |
| **Data Visibility** | Shows in main app as read-only curated data |

## Database File

- **Location**: `../humanitarian-logistics/humanitarian_logistics_curated.db`
- **Purpose**: Pre-curated data for main app
- **Unique Constraint**: Facebook links must be unique
- **Size**: Grows as you add data

## Integration with Main App

1. Open **Dev UI** → Add/crawl data
2. Data saved to `humanitarian_logistics_curated.db`
3. Open **Main App** → Data appears (read-only)
4. Users can see and analyze curated data

## Troubleshooting

### Q: "DUPLICATE - Link already in database"
A: This link was already added. Use a different URL or delete from database.

### Q: Database not found
A: Make sure `humanitarian-logistics/` folder exists in parent directory.

### Q: Fields not matching main app
A: Dev UI uses same fields as main app (Content, Author, Keyword, Category, Sentiment, Confidence).

## File Structure

```
dev-ui/
├── run.sh                                 # Start Dev UI
├── test.sh                                # Test installation
├── pom.xml                                # Maven config
├── target/dev-ui.jar                      # Compiled app (9.3 MB)
└── src/main/java/...                      # Source code
```

## Environment

- **Java**: 11+
- **Database**: SQLite (file-based, no server)
- **Build**: Maven 3.6+

## Next Steps

1. Run: `./run.sh`
2. Switch between tabs
3. Try Data Entry first (simpler)
4. Then try Web Crawler with duplicate detection
5. Check main app to see curated data appear

## Questions?

- Check README.md for detailed documentation
- Review source code in `src/main/java/com/humanitarian/devui/`
- Check database schema in `CuratedDatabaseManager.java`

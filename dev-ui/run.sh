#!/bin/bash

# Dev UI - Data Collection Tool for Curated Database
# Crawls from Facebook, checks for duplicate links, saves to humanitarian_logistics_curated.db

cd "$(dirname "$0")" || exit

echo "🚀 Starting Dev UI - Data Collection Mode..."
echo ""
echo "Features:"
echo "  🌐 Web Crawler - Crawl from Facebook links and hashtags"
echo "  ✏️  Data Entry - Manually add curated data"
echo "  ✅ Duplicate Link Detection - Prevents adding same link twice"
echo "  📊 Analysis - View data statistics and patterns"
echo ""
echo "Database: humanitarian_logistics_curated.db"
echo ""

# Build if not exists
if [ ! -f "target/dev-ui.jar" ]; then
    echo "Building Dev UI..."
    mvn clean package -DskipTests -q
fi

# Run
java -jar target/dev-ui.jar "$@"

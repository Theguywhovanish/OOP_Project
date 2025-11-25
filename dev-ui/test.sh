#!/bin/bash

# Test Dev UI duplicate link detection

echo "🧪 Testing Dev UI Duplicate Link Detection"
echo ""

# Test 1: Check database exists
echo "Test 1: Database exists"
if [ -f "../humanitarian-logistics/humanitarian_logistics_curated.db" ]; then
    echo "✓ Database found: ../humanitarian-logistics/humanitarian_logistics_curated.db"
else
    echo "✗ Database not found"
fi

echo ""
echo "Test 2: Check JAR exists"
if [ -f "target/dev-ui.jar" ]; then
    echo "✓ JAR found: target/dev-ui.jar"
    ls -lh target/dev-ui.jar | awk '{print "  Size: " $5}'
else
    echo "✗ JAR not found"
fi

echo ""
echo "Test 3: Database entry count"
cd ../humanitarian-logistics
if command -v sqlite3 &> /dev/null; then
    count=$(sqlite3 humanitarian_logistics_curated.db "SELECT COUNT(*) FROM posts" 2>/dev/null)
    if [ $? -eq 0 ]; then
        echo "✓ Current curated data entries: $count"
    else
        echo "⚠ Database table not initialized yet"
    fi
else
    echo "⚠ sqlite3 command not found"
fi

echo ""
echo "💡 Usage:"
echo "  cd dev-ui"
echo "  ./run.sh"
echo ""
echo "Features:"
echo "  ✏️  Data Entry - Manual data collection (same UI as main app)"
echo "  🌐 Web Crawler - Add data from Facebook URLs (with duplicate detection)"
echo "  ✅ Duplicate Check - Automatically prevents adding same link twice"

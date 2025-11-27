package com.humanitarian.devui.crawler;

import com.humanitarian.devui.model.*;
import com.humanitarian.devui.sentiment.EnhancedSentimentAnalyzer;
import com.humanitarian.devui.sentiment.SentimentAnalyzer;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * YouTube Crawler - Crawl videos and comments from YouTube
 * Supports both single video URL and keyword/hashtag search
 * Comment structure identical to Facebook (Post -> Comments)
 */
public class YouTubeCrawler implements DataCrawler {
    private WebDriver driver;
    private WebDriverWait wait;
    private boolean initialized;
    private static final int SCROLL_PAUSE = 2000;
    private SentimentAnalyzer sentimentAnalyzer;

    public YouTubeCrawler() {
        this.initialized = false;
        this.sentimentAnalyzer = new EnhancedSentimentAnalyzer();
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    public void initialize() {
        try {
            System.out.println("🚀 Initializing YouTube Crawler with Selenium...");

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--start-maximized",
                    "disable-infobars",
                    "--disable-extensions"
            );
            options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);

            System.out.println("✓ Creating ChromeDriver...");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            initialized = true;
            System.out.println("✅ YouTube Crawler initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ Error initializing: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
            if (driver != null) driver.quit();
        }
    }

    public void initializeBrowserOnly() {
        try {
            System.out.println("🚀 Initializing browser...");

            ChromeOptions options = new ChromeOptions();
            options.addArguments(
                    "--window-size=1920,1080",
                    "--no-sandbox",
                    "--disable-dev-shm-usage",
                    "--disable-blink-features=AutomationControlled",
                    "--disable-gpu",
                    "--start-maximized",
                    "disable-infobars",
                    "--disable-extensions"
            );
            options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);

            System.out.println("✓ Creating ChromeDriver...");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            initialized = true;
            System.out.println("✅ Browser initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ Error initializing: " + e.getMessage());
            e.printStackTrace();
            initialized = false;
            if (driver != null) driver.quit();
        }
    }

    @Override
    public List<Post> crawlPosts(List<String> keywords, List<String> hashtags, int limit) {
        // For YouTube, combine all hashtags and keywords into single search
        List<Post> allPosts = new ArrayList<>();
        
        List<String> searchTerms = new ArrayList<>();
        if (keywords != null) searchTerms.addAll(keywords);
        if (hashtags != null) searchTerms.addAll(hashtags);
        
        if (searchTerms.isEmpty()) {
            System.err.println("No search terms provided");
            return allPosts;
        }
        
        // Search for each term
        for (String term : searchTerms) {
            List<Post> results = crawl(term, limit, 50);
            allPosts.addAll(results);
            
            if (allPosts.size() >= limit) break;
        }
        
        return allPosts;
    }

    private List<Post> crawl(String input, int postLimit, int commentLimit) {
        List<Post> posts = new ArrayList<>();
        
        if (!initialized) {
            System.err.println("❌ Crawler not initialized");
            return posts;
        }

        // Normalize input - treat as keyword/hashtag
        String keyword = input.trim().toLowerCase();
        
        // Remove # if present
        if (keyword.startsWith("#")) {
            keyword = keyword.substring(1);
        }

        System.out.println("\n🎬 Starting YouTube crawl for keyword: " + keyword);
        
        // Build search URL
        String searchUrl = "https://www.youtube.com/results?search_query=" + keyword.replace(" ", "+");
        
        crawlFromUrl(searchUrl, postLimit, posts);
        
        return posts;
    }

    /**
     * Crawl from search results page - click videos and extract comments
     */
    private void crawlFromUrl(String url, int limit, List<Post> posts) {
        try {
            System.out.println("🌐 Navigating to: " + url);
            driver.get(url);
            Thread.sleep(4000);

            // Wait for search results to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("contents")));
            System.out.println("✓ Search results loaded");

            Set<String> collectedVideoUrls = new HashSet<>();
            
            int scrolls = 0;
            int maxScrolls = (limit / 4) + 10;

            System.out.println("\n📍 Phase 1: Scroll search results to collect video links...");
            
            // Scroll through search results and collect video URLs
            while (collectedVideoUrls.size() < limit && scrolls < maxScrolls) {
                scrollDown();
                Thread.sleep(SCROLL_PAUSE + new Random().nextInt(1000));
                
                // Collect video URLs
                collectVideoUrlsFromSearchResults(collectedVideoUrls, limit);
                scrolls++;
            }

            System.out.println("\n📍 Phase 2: Visit each video to extract comments...");
            System.out.println("📋 Total videos collected: " + collectedVideoUrls.size());

            // Now visit each video URL to extract posts and comments
            int processedCount = 0;
            for (String videoUrl : collectedVideoUrls) {
                if (posts.size() >= limit) break;
                
                try {
                    System.out.println("\n  [" + (processedCount + 1) + "/" + collectedVideoUrls.size() + "] " +
                        "Processing video: " + videoUrl.substring(0, Math.min(80, videoUrl.length())));
                    
                    // Navigate to video
                    driver.get(videoUrl);
                    Thread.sleep(3000);

                    // Wait for video to load
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("title")));

                    // Extract video as post
                    String videoId = extractVideoId(videoUrl);
                    String videoTitle = extractVideoTitle();
                    YouTubePost post = new YouTubePost(videoId, videoTitle, LocalDateTime.now(), "YouTube User", videoUrl);

                    System.out.println("  ✓ Video: " + videoTitle);

                    // Extract comments
                    System.out.println("  💬 Extracting comments...");
                    extractCommentsFromVideoPage(post);

                    System.out.println("  ✅ Extracted " + post.getComments().size() + " comments");

                    posts.add(post);
                    processedCount++;

                } catch (Exception e) {
                    System.err.println("  ❌ Error processing video: " + e.getMessage());
                }
            }

            System.out.println("\n✅ Crawl complete!");
            System.out.println("📊 Total videos crawled: " + posts.size());
            System.out.println("💬 Total comments extracted: " + 
                posts.stream().mapToInt(p -> p.getComments().size()).sum());

        } catch (Exception e) {
            System.err.println("❌ Error during crawl: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Collect video URLs from search results page
     */
    private void collectVideoUrlsFromSearchResults(Set<String> urls, int limit) {
        try {
            List<WebElement> videoElements = driver.findElements(
                By.cssSelector("a#video-title")
            );

            for (WebElement videoElement : videoElements) {
                if (urls.size() >= limit) break;
                
                try {
                    String href = videoElement.getAttribute("href");
                    if (href != null && href.contains("/watch?")) {
                        String fullUrl = href.startsWith("http") ? href : "https://www.youtube.com" + href;
                        urls.add(fullUrl);
                    }
                } catch (Exception e) {
                    // Skip this element
                }
            }
        } catch (Exception e) {
            // No videos found on this scroll
        }
    }

    /**
     * Extract comments from video page
     */
    private void extractCommentsFromVideoPage(YouTubePost post) {
        try {
            // Scroll down to load comments
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Scroll to comments section
            for (int i = 0; i < 3; i++) {
                js.executeScript("window.scrollBy(0, 500);");
                Thread.sleep(1000);
            }

            int commentCount = 0;
            
            // Try multiple selectors for comment elements (YouTube changes structure frequently)
            List<WebElement> commentElements = new ArrayList<>();
            try {
                // Try newer YouTube comment selector
                commentElements = driver.findElements(By.cssSelector("div#content-text"));
            } catch (Exception e1) {
                try {
                    // Fallback: Try finding comment elements by class
                    commentElements = driver.findElements(By.xpath("//yt-formatted-string[@id='content-text']"));
                } catch (Exception e2) {
                    try {
                        // Another fallback: Look for comment text divs
                        commentElements = driver.findElements(By.cssSelector("div[id='content-text']"));
                    } catch (Exception e3) {
                        System.out.println("    ⚠️ Could not find comment elements with any selector");
                        // If we can't find comments, that's OK - still add the video
                    }
                }
            }

            // Extract comments if found
            for (WebElement commentElement : commentElements) {
                if (commentCount >= 50) break; // Limit comments per video
                
                try {
                    String commentText = commentElement.getText();
                    if (commentText != null && !commentText.trim().isEmpty() && !commentText.contains("Login")) {
                        String commentId = "YT_" + post.getPostId() + "_" + commentCount;
                        Comment comment = new Comment(
                            commentId,
                            post.getPostId(),
                            commentText,
                            LocalDateTime.now().minusHours(new Random().nextInt(168)),
                            "YouTube User"
                        );

                        // Analyze sentiment
                        sentimentAnalyzer.initialize();
                        Sentiment sentiment = sentimentAnalyzer.analyzeSentiment(commentText);
                        comment.setSentiment(sentiment);

                        post.addComment(comment);
                        commentCount++;
                    }
                } catch (Exception e) {
                    // Skip this comment and continue
                }
            }

            System.out.println("    Extracted " + commentCount + " comments");

        } catch (Exception e) {
            System.err.println("    ⚠️ Warning extracting comments: " + e.getMessage());
            // Don't fail - video extraction is still valid even without comments
        }
    }

    /**
     * Extract video ID from URL
     */
    private String extractVideoId(String url) {
        try {
            if (url.contains("v=")) {
                return "YT_" + url.split("v=")[1].split("&")[0];
            }
        } catch (Exception e) {
            // Fallback
        }
        return "YT_" + url.hashCode();
    }

    /**
     * Extract video title from page
     */
    private String extractVideoTitle() {
        try {
            WebElement titleElement = driver.findElement(By.id("title"));
            return titleElement.getText();
        } catch (Exception e) {
            return "YouTube Video";
        }
    }

    /**
     * Crawl single video by URL
     */
    public YouTubePost crawlVideoByUrl(String videoUrl) {
        YouTubeCrawler tempCrawler = null;
        try {
            System.out.println("\n🔗 Crawling single video from URL");
            System.out.println("📍 URL: " + videoUrl);
            
            // Validate URL
            if (!videoUrl.contains("youtube.com")) {
                System.err.println("❌ Invalid URL: Must be a YouTube URL");
                return null;
            }

            System.out.println("🚀 Initializing browser...");
            tempCrawler = new YouTubeCrawler();
            tempCrawler.initializeBrowserOnly();

            if (!tempCrawler.isInitialized()) {
                System.err.println("❌ Failed to initialize browser");
                return null;
            }

            System.out.println("✓ Browser ready");

            // Navigate to video
            System.out.println("📍 Navigating to video...");
            tempCrawler.driver.get(videoUrl);
            Thread.sleep(3000);

            String currentUrl = tempCrawler.driver.getCurrentUrl();
            System.out.println("  Current URL: " + currentUrl);

            // Extract video info
            String videoId = tempCrawler.extractVideoId(videoUrl);
            String videoTitle = tempCrawler.extractVideoTitle();

            YouTubePost post = new YouTubePost(videoId, videoTitle, LocalDateTime.now(), "YouTube User", videoUrl);

            System.out.println("📝 Video title extracted: " + videoTitle);

            // Extract comments
            System.out.println("💬 Extracting comments from video...");
            tempCrawler.extractCommentsFromVideoPage(post);

            System.out.println("✅ Successfully extracted video with " + post.getComments().size() + " comments");
            return post;

        } catch (Exception e) {
            System.err.println("❌ Error crawling video by URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (tempCrawler != null) {
                try {
                    tempCrawler.shutdown();
                } catch (Exception e) {
                    System.err.println("Error shutting down temporary crawler: " + e.getMessage());
                }
            }
        }
    }

    private void scrollDown() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.scrollBy(0, 800);");
    }

    @Override
    public String getCrawlerName() {
        return "YouTubeCrawler (Keyword + URL)";
    }

    @Override
    public void shutdown() {
        if (driver != null) {
            try {
                driver.quit();
                System.out.println("YouTubeCrawler shutdown complete");
            } catch (Exception e) {
                System.err.println("Error shutting down driver: " + e.getMessage());
            } finally {
                driver = null;
                initialized = false;
            }
        }
    }
}

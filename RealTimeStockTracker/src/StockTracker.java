import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockTracker {

    // Free Stock APIs - No API key required!
    private static final String YAHOO_FINANCE_URL = "https://query1.finance.yahoo.com/v8/finance/chart/";
    private static final String FINNHUB_QUOTE_URL = "https://finnhub.io/api/v1/quote";
    private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query";
    private static final String TWELVE_DATA_URL = "https://api.twelvedata.com/price";

    // ANSI color codes for console output
    private static final String RESET = "\033[0m";
    private static final String BOLD = "\033[1m";
    private static final String GREEN = "\033[32m";
    private static final String RED = "\033[31m";
    private static final String BLUE = "\033[34m";
    private static final String YELLOW = "\033[33m";
    private static final String CYAN = "\033[36m";
    private static final String PURPLE = "\033[35m";
    private static final String WHITE = "\033[37m";

    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final Map<String, StockData> watchlist = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private static volatile boolean isAutoRefreshEnabled = false;

    public static void main(String[] args) {
        // Shutdown hook for clean exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scheduler.shutdown();
            System.out.println(GREEN + "\n👋 Thank you for using Stock Tracker! Happy Trading!" + RESET);
        }));

        printWelcomeBanner();

        while (true) {
            try {
                showMainMenu();
                int choice = getMenuChoice();

                switch (choice) {
                    case 1:
                        getStockQuote();
                        break;
                    case 2:
                        addToWatchlist();
                        break;
                    case 3:
                        viewWatchlist();
                        break;
                    case 4:
                        removeFromWatchlist();
                        break;
                    case 5:
                        searchStock();
                        break;
                    case 6:
                        getMarketSummary();
                        break;
                    case 7:
                        toggleAutoRefresh();
                        break;
                    case 8:
                        showTopMovers();
                        break;
                    case 9:
                        showCryptoQuotes();
                        break;
                    case 0:
                        System.out.println(GREEN + "Exiting Stock Tracker. Happy Trading! 📈" + RESET);
                        scheduler.shutdown();
                        return;
                    default:
                        System.out.println(RED + "❌ Invalid choice! Please try again." + RESET);
                }

                if (choice != 3 && choice != 7) { // Don't pause for watchlist or auto-refresh
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                }

            } catch (Exception e) {
                System.out.println(RED + "❌ An error occurred: " + e.getMessage() + RESET);
                e.printStackTrace();
            }
        }
    }

    private static void printWelcomeBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║        📈 REAL-TIME STOCK TRACKER 📈       ║");
        System.out.println("║         Live Market Data & Analysis       ║");
        System.out.println("║          No API Key Required! 🎉         ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println(RESET);
        System.out.println(YELLOW + "💡 Tip: Use symbols like AAPL, GOOGL, MSFT, TSLA, etc." + RESET);
    }

    private static void showMainMenu() {
        clearScreen();
        System.out.println(BLUE + "\n" + "=".repeat(50));
        System.out.println(BOLD + "                MAIN MENU" + RESET + BLUE);
        System.out.println("=".repeat(50) + RESET);

        System.out.println("1. 📊 Get Stock Quote");
        System.out.println("2. ➕ Add to Watchlist");
        System.out.println("3. 👀 View Watchlist" + (isAutoRefreshEnabled ? " (Auto-Refresh ON)" : ""));
        System.out.println("4. ➖ Remove from Watchlist");
        System.out.println("5. 🔍 Search Stock Symbol");
        System.out.println("6. 📈 Market Summary (Major Indices)");
        System.out.println("7. 🔄 Toggle Auto-Refresh " + (isAutoRefreshEnabled ? "(ON)" : "(OFF)"));
        System.out.println("8. 🚀 Top Movers (Gainers/Losers)");
        System.out.println("9. 🪙 Cryptocurrency Quotes");
        System.out.println("0. 🚪 Exit");

        System.out.print(YELLOW + "\nEnter your choice (0-9): " + RESET);
    }

    private static void clearScreen() {
        // Clear screen for better UX
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static int getMenuChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void getStockQuote() throws IOException {
        System.out.print(CYAN + "\n📊 Enter stock symbol (e.g., AAPL, GOOGL): " + RESET);
        String symbol = scanner.nextLine().trim().toUpperCase();

        if (symbol.isEmpty()) {
            System.out.println(RED + "❌ Stock symbol cannot be empty!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Fetching real-time data for " + symbol + "..." + RESET);

        StockData stock = fetchStockData(symbol);
        if (stock != null) {
            displayStockQuote(stock);
        } else {
            System.out.println(RED + "❌ Unable to fetch data for " + symbol + ". Please check the symbol." + RESET);
        }
    }

    private static void addToWatchlist() throws IOException {
        System.out.print(CYAN + "\n➕ Enter stock symbol to add to watchlist: " + RESET);
        String symbol = scanner.nextLine().trim().toUpperCase();

        if (symbol.isEmpty()) {
            System.out.println(RED + "❌ Stock symbol cannot be empty!" + RESET);
            return;
        }

        if (watchlist.containsKey(symbol)) {
            System.out.println(YELLOW + "⚠️ " + symbol + " is already in your watchlist!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Adding " + symbol + " to watchlist..." + RESET);

        StockData stock = fetchStockData(symbol);
        if (stock != null) {
            watchlist.put(symbol, stock);
            System.out.println(GREEN + "✅ " + symbol + " added to watchlist successfully!" + RESET);
            System.out.println(WHITE + "Current Price: " + formatPrice(stock.currentPrice) +
                    " (" + formatChange(stock.change, stock.changePercent) + ")" + RESET);
        } else {
            System.out.println(RED + "❌ Unable to add " + symbol + ". Please check the symbol." + RESET);
        }
    }

    private static void viewWatchlist() {
        if (watchlist.isEmpty()) {
            System.out.println(YELLOW + "\n📝 Your watchlist is empty. Add some stocks first!" + RESET);
            return;
        }

        displayWatchlist();

        if (!isAutoRefreshEnabled) {
            System.out.println(CYAN + "\n💡 Enable auto-refresh (option 7) for real-time updates!" + RESET);
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void displayWatchlist() {
        System.out.println(GREEN + "\n" + "=".repeat(80));
        System.out.println(BOLD + "                          📋 YOUR WATCHLIST");
        System.out.println(GREEN + "=".repeat(80) + RESET);
        System.out.println(BLUE + "Last Updated: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + RESET);
        System.out.println();

        System.out.printf("%-8s %-25s %-12s %-15s %-12s %-10s\n",
                "SYMBOL", "COMPANY", "PRICE", "CHANGE", "CHANGE%", "VOLUME");
        System.out.println("-".repeat(80));

        for (Map.Entry<String, StockData> entry : watchlist.entrySet()) {
            StockData stock = entry.getValue();
            String changeColor = stock.change >= 0 ? GREEN : RED;
            String changeSymbol = stock.change >= 0 ? "▲" : "▼";

            System.out.printf("%-8s %-25s %s%-12s%s %s%-15s%s %s%-12s%s %-10s\n",
                    stock.symbol,
                    truncateString(stock.companyName, 24),
                    BOLD, formatPrice(stock.currentPrice), RESET,
                    changeColor, changeSymbol + " " + formatPrice(Math.abs(stock.change)), RESET,
                    changeColor, changeSymbol + " " + df.format(Math.abs(stock.changePercent)) + "%", RESET,
                    formatVolume(stock.volume));
        }

        System.out.println(GREEN + "=".repeat(80) + RESET);
    }

    private static void removeFromWatchlist() {
        if (watchlist.isEmpty()) {
            System.out.println(YELLOW + "\n📝 Your watchlist is empty!" + RESET);
            return;
        }

        System.out.println(CYAN + "\nCurrent watchlist:" + RESET);
        for (String symbol : watchlist.keySet()) {
            System.out.println("• " + symbol);
        }

        System.out.print(CYAN + "\n➖ Enter symbol to remove: " + RESET);
        String symbol = scanner.nextLine().trim().toUpperCase();

        if (watchlist.remove(symbol) != null) {
            System.out.println(GREEN + "✅ " + symbol + " removed from watchlist!" + RESET);
        } else {
            System.out.println(RED + "❌ " + symbol + " not found in watchlist!" + RESET);
        }
    }

    private static void searchStock() throws IOException {
        System.out.print(CYAN + "\n🔍 Enter company name or partial symbol: " + RESET);
        String query = scanner.nextLine().trim();

        if (query.isEmpty()) {
            System.out.println(RED + "❌ Search query cannot be empty!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Searching for: " + query + "..." + RESET);

        // Try common stock symbols based on query
        String[] commonStocks = {
                "AAPL:Apple Inc", "GOOGL:Alphabet Inc", "MSFT:Microsoft Corp",
                "AMZN:Amazon.com Inc", "TSLA:Tesla Inc", "META:Meta Platforms",
                "NVDA:NVIDIA Corp", "NFLX:Netflix Inc", "AMD:Advanced Micro Devices",
                "INTC:Intel Corp", "CRM:Salesforce Inc", "ORCL:Oracle Corp",
                "IBM:International Business Machines", "CSCO:Cisco Systems",
                "V:Visa Inc", "MA:Mastercard Inc", "JPM:JPMorgan Chase",
                "BAC:Bank of America", "WMT:Walmart Inc", "PG:Procter & Gamble",
                "JNJ:Johnson & Johnson", "UNH:UnitedHealth Group", "HD:Home Depot",
                "PFE:Pfizer Inc", "VZ:Verizon Communications", "T:AT&T Inc",
                "KO:Coca-Cola Co", "PEP:PepsiCo Inc", "MCD:McDonald's Corp"
        };

        System.out.println(GREEN + "\n🔍 Search Results:" + RESET);
        System.out.println("-".repeat(50));

        boolean found = false;
        for (String stock : commonStocks) {
            String[] parts = stock.split(":");
            String symbol = parts[0];
            String company = parts[1];

            if (symbol.toLowerCase().contains(query.toLowerCase()) ||
                    company.toLowerCase().contains(query.toLowerCase())) {
                System.out.printf("%-6s - %s\n", symbol, company);
                found = true;
            }
        }

        if (!found) {
            System.out.println(YELLOW + "❓ No matches found. Try searching for:" + RESET);
            System.out.println("• Popular symbols: AAPL, GOOGL, MSFT, AMZN, TSLA");
            System.out.println("• Use exact stock symbols for best results");
        }

        System.out.println("-".repeat(50));
    }

    private static void getMarketSummary() throws IOException {
        System.out.println(YELLOW + "🔄 Fetching market summary..." + RESET);

        String[] majorIndices = {"^GSPC", "^DJI", "^IXIC"}; // S&P 500, Dow Jones, NASDAQ
        String[] indexNames = {"S&P 500", "Dow Jones", "NASDAQ"};

        System.out.println(BLUE + "\n" + "=".repeat(70));
        System.out.println(BOLD + "                    📈 MARKET SUMMARY");
        System.out.println(BLUE + "=".repeat(70) + RESET);
        System.out.println(CYAN + "Updated: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + RESET);
        System.out.println();

        for (int i = 0; i < majorIndices.length; i++) {
            StockData indexData = fetchStockData(majorIndices[i]);
            if (indexData != null) {
                String changeColor = indexData.change >= 0 ? GREEN : RED;
                String changeSymbol = indexData.change >= 0 ? "▲" : "▼";

                System.out.printf("%-12s %s%-12s%s %s%s %.2f (%.2f%%)%s\n",
                        indexNames[i],
                        BOLD, formatPrice(indexData.currentPrice), RESET,
                        changeColor, changeSymbol, Math.abs(indexData.change),
                        Math.abs(indexData.changePercent), RESET);
            }
        }

        System.out.println(BLUE + "=".repeat(70) + RESET);
    }

    private static void toggleAutoRefresh() {
        if (isAutoRefreshEnabled) {
            // Stop auto-refresh
            isAutoRefreshEnabled = false;
            scheduler.shutdownNow();
            System.out.println(YELLOW + "🔄 Auto-refresh disabled" + RESET);
        } else {
            // Start auto-refresh
            if (watchlist.isEmpty()) {
                System.out.println(YELLOW + "⚠️ Add stocks to watchlist first!" + RESET);
                return;
            }

            isAutoRefreshEnabled = true;
            startAutoRefresh();
            System.out.println(GREEN + "🔄 Auto-refresh enabled! Updating every 30 seconds..." + RESET);
            System.out.println(CYAN + "💡 Press Ctrl+C to stop or use menu option 7" + RESET);

            // Keep showing the watchlist while auto-refresh is active
            while (isAutoRefreshEnabled) {
                try {
                    displayWatchlist();
                    Thread.sleep(30000); // Wait 30 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    private static void startAutoRefresh() {
        scheduler.scheduleAtFixedRate(() -> {
            if (isAutoRefreshEnabled && !watchlist.isEmpty()) {
                try {
                    // Update all stocks in watchlist
                    for (String symbol : watchlist.keySet()) {
                        StockData updatedStock = fetchStockData(symbol);
                        if (updatedStock != null) {
                            watchlist.put(symbol, updatedStock);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error updating watchlist: " + e.getMessage());
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    private static void showTopMovers() throws IOException {
        System.out.println(YELLOW + "🔄 Fetching top movers..." + RESET);

        // Popular stocks to check for movers
        String[] popularStocks = {
                "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA",
                "NFLX", "AMD", "INTC", "CRM", "ORCL", "V", "MA"
        };

        List<StockData> gainers = new ArrayList<>();
        List<StockData> losers = new ArrayList<>();

        for (String symbol : popularStocks) {
            StockData stock = fetchStockData(symbol);
            if (stock != null) {
                if (stock.changePercent > 2.0) {
                    gainers.add(stock);
                } else if (stock.changePercent < -2.0) {
                    losers.add(stock);
                }
            }
            // Small delay to avoid overwhelming the API
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        }

        // Sort by change percentage
        gainers.sort((a, b) -> Double.compare(b.changePercent, a.changePercent));
        losers.sort((a, b) -> Double.compare(a.changePercent, b.changePercent));

        System.out.println(GREEN + "\n🚀 TOP GAINERS (>2%)" + RESET);
        System.out.println("-".repeat(50));
        if (gainers.isEmpty()) {
            System.out.println("No significant gainers found today");
        } else {
            for (StockData stock : gainers) {
                System.out.printf("%-6s %s%-10s%s %s▲ +%.2f%%  (+%s)%s\n",
                        stock.symbol,
                        BOLD, formatPrice(stock.currentPrice), RESET,
                        GREEN, stock.changePercent, formatPrice(stock.change), RESET);
            }
        }

        System.out.println(RED + "\n📉 TOP LOSERS (<-2%)" + RESET);
        System.out.println("-".repeat(50));
        if (losers.isEmpty()) {
            System.out.println("No significant losers found today");
        } else {
            for (StockData stock : losers) {
                System.out.printf("%-6s %s%-10s%s %s▼ %.2f%%  (%s)%s\n",
                        stock.symbol,
                        BOLD, formatPrice(stock.currentPrice), RESET,
                        RED, stock.changePercent, formatPrice(stock.change), RESET);
            }
        }
    }

    private static void showCryptoQuotes() throws IOException {
        System.out.println(YELLOW + "🔄 Fetching cryptocurrency data..." + RESET);

        String[] cryptos = {"BTC-USD", "ETH-USD", "ADA-USD", "DOT-USD", "DOGE-USD"};
        String[] cryptoNames = {"Bitcoin", "Ethereum", "Cardano", "Polkadot", "Dogecoin"};

        System.out.println(PURPLE + "\n" + "=".repeat(70));
        System.out.println(BOLD + "                    🪙 CRYPTOCURRENCY QUOTES");
        System.out.println(PURPLE + "=".repeat(70) + RESET);

        for (int i = 0; i < cryptos.length; i++) {
            StockData crypto = fetchStockData(cryptos[i]);
            if (crypto != null) {
                String changeColor = crypto.change >= 0 ? GREEN : RED;
                String changeSymbol = crypto.change >= 0 ? "▲" : "▼";

                System.out.printf("%-12s %s$%-12s%s %s%s $%.2f (%.2f%%)%s\n",
                        cryptoNames[i],
                        BOLD, formatPrice(crypto.currentPrice), RESET,
                        changeColor, changeSymbol, Math.abs(crypto.change),
                        Math.abs(crypto.changePercent), RESET);
            }
        }

        System.out.println(PURPLE + "=".repeat(70) + RESET);
    }

    private static StockData fetchStockData(String symbol) throws IOException {
        try {
            // Use Yahoo Finance API (free, no key required)
            String urlString = YAHOO_FINANCE_URL + symbol;
            String jsonResponse = makeApiCall(urlString);

            if (jsonResponse != null && jsonResponse.contains("regularMarketPrice")) {
                return parseYahooFinanceResponse(jsonResponse, symbol);
            }
        } catch (Exception e) {
            // Fallback or error handling
        }

        return null;
    }

    private static String makeApiCall(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();

            } else {
                return null;
            }

        } catch (IOException e) {
            throw e;
        }
    }

    private static StockData parseYahooFinanceResponse(String jsonResponse, String symbol) {
        try {
            // Parse JSON manually using regex (avoiding external libraries)
            double currentPrice = parseJsonDouble(jsonResponse, "regularMarketPrice");
            double previousClose = parseJsonDouble(jsonResponse, "previousClose");
            long volume = (long) parseJsonDouble(jsonResponse, "regularMarketVolume");
            double dayHigh = parseJsonDouble(jsonResponse, "regularMarketDayHigh");
            double dayLow = parseJsonDouble(jsonResponse, "regularMarketDayLow");

            String companyName = parseJsonString(jsonResponse, "longName");
            if (companyName.isEmpty()) {
                companyName = parseJsonString(jsonResponse, "shortName");
            }
            if (companyName.isEmpty()) {
                companyName = symbol; // Fallback to symbol
            }

            double change = currentPrice - previousClose;
            double changePercent = (change / previousClose) * 100;

            return new StockData(symbol, companyName, currentPrice, change, changePercent,
                    volume, dayHigh, dayLow, previousClose);

        } catch (Exception e) {
            return null;
        }
    }

    private static double parseJsonDouble(String json, String key) {
        try {
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.-]+)");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            // Return 0 if parsing fails
        }
        return 0.0;
    }

    private static String parseJsonString(String json, String key) {
        try {
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            // Return empty string if parsing fails
        }
        return "";
    }

    private static void displayStockQuote(StockData stock) {
        String changeColor = stock.change >= 0 ? GREEN : RED;
        String changeSymbol = stock.change >= 0 ? "▲" : "▼";

        System.out.println(BLUE + "\n" + "=".repeat(70));
        System.out.println(BOLD + "                   📊 STOCK QUOTE");
        System.out.println(BLUE + "=".repeat(70) + RESET);

        System.out.println(CYAN + BOLD + "📈 Symbol: " + RESET + stock.symbol);
        System.out.println(CYAN + "🏢 Company: " + RESET + stock.companyName);
        System.out.println(BLUE + "🕒 Last Updated: " + RESET +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println();

        System.out.println(YELLOW + BOLD + "💰 Current Price: " + RESET + BOLD + "$" + formatPrice(stock.currentPrice) + RESET);
        System.out.printf("%s%s Change: %s%s $%.2f (%.2f%%)%s\n",
                changeColor, changeSymbol, changeSymbol, RESET + changeColor,
                Math.abs(stock.change), Math.abs(stock.changePercent), RESET);
        System.out.println();

        System.out.println(GREEN + "📊 Trading Information:" + RESET);
        System.out.println("   Previous Close: $" + formatPrice(stock.previousClose));
        System.out.println("   Day High: $" + formatPrice(stock.dayHigh));
        System.out.println("   Day Low: $" + formatPrice(stock.dayLow));
        System.out.println("   Volume: " + formatVolume(stock.volume));

        System.out.println(BLUE + "=".repeat(70) + RESET);
    }

    private static String formatPrice(double price) {
        return df.format(price);
    }

    private static String formatChange(double change, double changePercent) {
        String color = change >= 0 ? GREEN : RED;
        String symbol = change >= 0 ? "▲" : "▼";
        return color + symbol + " " + formatPrice(Math.abs(change)) +
                " (" + df.format(Math.abs(changePercent)) + "%)" + RESET;
    }

    private static String formatVolume(long volume) {
        if (volume >= 1_000_000_000) {
            return df.format(volume / 1_000_000_000.0) + "B";
        } else if (volume >= 1_000_000) {
            return df.format(volume / 1_000_000.0) + "M";
        } else if (volume >= 1_000) {
            return df.format(volume / 1_000.0) + "K";
        } else {
            return String.valueOf(volume);
        }
    }

    private static String truncateString(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    // Stock data class
    static class StockData {
        String symbol;
        String companyName;
        double currentPrice;
        double change;
        double changePercent;
        long volume;
        double dayHigh;
        double dayLow;
        double previousClose;

        public StockData(String symbol, String companyName, double currentPrice,
                         double change, double changePercent, long volume,
                         double dayHigh, double dayLow, double previousClose) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.currentPrice = currentPrice;
            this.change = change;
            this.changePercent = changePercent;
            this.volume = volume;
            this.dayHigh = dayHigh;
            this.dayLow = dayLow;
            this.previousClose = previousClose;
        }
    }
}
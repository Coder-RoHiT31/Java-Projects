import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherConsoleApp {

    // Free weather APIs - No API key required!
    private static final String WTTR_BASE_URL = "http://wttr.in/";
    private static final String OPEN_METEO_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search";

    // ANSI color codes for better console output
    private static final String RESET = "\033[0m";
    private static final String BLUE = "\033[34m";
    private static final String GREEN = "\033[32m";
    private static final String YELLOW = "\033[33m";
    private static final String RED = "\033[31m";
    private static final String CYAN = "\033[36m";
    private static final String BOLD = "\033[1m";
    private static final String PURPLE = "\033[35m";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printWelcomeBanner();

        while (true) {
            try {
                showMainMenu();
                int choice = getMenuChoice();

                switch (choice) {
                    case 1:
                        getCurrentWeather();
                        break;
                    case 2:
                        getForecast();
                        break;
                    case 3:
                        getSimpleWeather();
                        break;
                    case 4:
                        compareWeather();
                        break;
                    case 5:
                        System.out.println(GREEN + "Thank you for using Weather Console App! 🌤️" + RESET);
                        return;
                    default:
                        System.out.println(RED + "Invalid choice! Please try again." + RESET);
                }

                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();

            } catch (Exception e) {
                System.out.println(RED + "An error occurred: " + e.getMessage() + RESET);
            }
        }
    }

    private static void printWelcomeBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║        🌦️  WEATHER CONSOLE APP  🌦️        ║");
        System.out.println("║     Free Weather Information         ║");
        System.out.println("║     No Dependencies Required! 🎉     ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println(RESET);
    }

    private static void showMainMenu() {
        System.out.println(BLUE + "\n" + "=".repeat(40));
        System.out.println("           MAIN MENU");
        System.out.println("=".repeat(40) + RESET);
        System.out.println("1. 🌡️  Get Detailed Weather (Open-Meteo)");
        System.out.println("2. 📅 Get 7-Day Forecast");
        System.out.println("3. 🌤️  Get Simple Weather (wttr.in)");
        System.out.println("4. 🏙️  Compare Two Cities");
        System.out.println("5. 🚪 Exit");
        System.out.print(YELLOW + "\nEnter your choice (1-5): " + RESET);
    }

    private static int getMenuChoice() {
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            return choice;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void getCurrentWeather() throws IOException {
        System.out.print(CYAN + "\nEnter city name: " + RESET);
        String city = scanner.nextLine().trim();

        if (city.isEmpty()) {
            System.out.println(RED + "City name cannot be empty!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Fetching detailed weather data for " + city + "..." + RESET);

        // First, get coordinates for the city
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String geoUrl = GEOCODING_URL + "?name=" + encodedCity + "&count=1&language=en&format=json";

        String geoResponse = makeApiCall(geoUrl);
        if (geoResponse == null) return;

        // Parse coordinates using simple string parsing
        double lat = parseJsonDouble(geoResponse, "latitude");
        double lon = parseJsonDouble(geoResponse, "longitude");
        String actualCityName = parseJsonString(geoResponse, "name");
        String countryCode = parseJsonString(geoResponse, "country_code");

        if (lat == Double.NaN || lon == Double.NaN) {
            System.out.println(RED + "❌ City not found! Please check the spelling." + RESET);
            return;
        }

        // Get weather data
        String weatherUrl = OPEN_METEO_URL +
                "?latitude=" + lat +
                "&longitude=" + lon +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,cloud_cover,pressure_msl,wind_speed_10m,wind_direction_10m" +
                "&timezone=auto";

        String weatherResponse = makeApiCall(weatherUrl);
        if (weatherResponse != null) {
            displayDetailedWeather(weatherResponse, actualCityName, countryCode);
        }
    }

    private static void getForecast() throws IOException {
        System.out.print(CYAN + "\nEnter city name: " + RESET);
        String city = scanner.nextLine().trim();

        if (city.isEmpty()) {
            System.out.println(RED + "City name cannot be empty!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Fetching 7-day forecast for " + city + "..." + RESET);

        // Get coordinates
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String geoUrl = GEOCODING_URL + "?name=" + encodedCity + "&count=1&language=en&format=json";

        String geoResponse = makeApiCall(geoUrl);
        if (geoResponse == null) return;

        double lat = parseJsonDouble(geoResponse, "latitude");
        double lon = parseJsonDouble(geoResponse, "longitude");
        String actualCityName = parseJsonString(geoResponse, "name");
        String countryCode = parseJsonString(geoResponse, "country_code");

        if (lat == Double.NaN || lon == Double.NaN) {
            System.out.println(RED + "❌ City not found! Please check the spelling." + RESET);
            return;
        }

        // Get forecast data
        String forecastUrl = OPEN_METEO_URL +
                "?latitude=" + lat +
                "&longitude=" + lon +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max" +
                "&timezone=auto";

        String forecastResponse = makeApiCall(forecastUrl);
        if (forecastResponse != null) {
            displayForecast(forecastResponse, actualCityName, countryCode);
        }
    }

    private static void getSimpleWeather() throws IOException {
        System.out.print(CYAN + "\nEnter city name: " + RESET);
        String city = scanner.nextLine().trim();

        if (city.isEmpty()) {
            System.out.println(RED + "City name cannot be empty!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Fetching simple weather for " + city + "..." + RESET);

        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String wttrUrl = WTTR_BASE_URL + encodedCity + "?format=j1";

        String weatherResponse = makeApiCall(wttrUrl);
        if (weatherResponse != null) {
            displaySimpleWeather(weatherResponse, city);
        }
    }

    private static void compareWeather() throws IOException {
        System.out.print(CYAN + "\nEnter first city name: " + RESET);
        String city1 = scanner.nextLine().trim();

        System.out.print(CYAN + "Enter second city name: " + RESET);
        String city2 = scanner.nextLine().trim();

        if (city1.isEmpty() || city2.isEmpty()) {
            System.out.println(RED + "Both city names are required!" + RESET);
            return;
        }

        System.out.println(YELLOW + "🔄 Comparing weather between " + city1 + " and " + city2 + "..." + RESET);

        // Get data for both cities
        WeatherData data1 = getCityWeatherData(city1);
        WeatherData data2 = getCityWeatherData(city2);

        if (data1 != null && data2 != null) {
            displayWeatherComparison(data1, data2);
        }
    }

    private static WeatherData getCityWeatherData(String city) throws IOException {
        String encodedCity = URLEncoder.encode(city, "UTF-8");
        String geoUrl = GEOCODING_URL + "?name=" + encodedCity + "&count=1&language=en&format=json";

        String geoResponse = makeApiCall(geoUrl);
        if (geoResponse == null) return null;

        double lat = parseJsonDouble(geoResponse, "latitude");
        double lon = parseJsonDouble(geoResponse, "longitude");
        String actualCityName = parseJsonString(geoResponse, "name");
        String countryCode = parseJsonString(geoResponse, "country_code");

        if (lat == Double.NaN || lon == Double.NaN) {
            System.out.println(RED + "❌ City '" + city + "' not found!" + RESET);
            return null;
        }

        String weatherUrl = OPEN_METEO_URL +
                "?latitude=" + lat +
                "&longitude=" + lon +
                "&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m" +
                "&timezone=auto";

        String weatherResponse = makeApiCall(weatherUrl);
        if (weatherResponse == null) return null;

        // Parse weather data
        double temperature = parseJsonDoubleFromCurrent(weatherResponse, "temperature_2m");
        int humidity = (int) parseJsonDoubleFromCurrent(weatherResponse, "relative_humidity_2m");
        int weatherCode = (int) parseJsonDoubleFromCurrent(weatherResponse, "weather_code");
        double windSpeed = parseJsonDoubleFromCurrent(weatherResponse, "wind_speed_10m");

        return new WeatherData(actualCityName, countryCode, temperature, humidity, weatherCode, windSpeed);
    }

    private static String makeApiCall(String urlString) throws IOException {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "WeatherConsoleApp/1.0");

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
                System.out.println(RED + "❌ Error: HTTP " + responseCode + RESET);
                return null;
            }

        } catch (IOException e) {
            System.out.println(RED + "❌ Network error: " + e.getMessage() + RESET);
            return null;
        }
    }

    // Simple JSON parsing without external libraries
    private static double parseJsonDouble(String json, String key) {
        try {
            Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.-]+)");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        } catch (NumberFormatException e) {
            // Return NaN if parsing fails
        }
        return Double.NaN;
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

    private static double parseJsonDoubleFromCurrent(String json, String key) {
        try {
            // Find the "current" object first
            Pattern currentPattern = Pattern.compile("\"current\"\\s*:\\s*\\{([^}]+)\\}");
            Matcher currentMatcher = currentPattern.matcher(json);
            if (currentMatcher.find()) {
                String currentObject = currentMatcher.group(1);
                Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*([\\d.-]+)");
                Matcher matcher = pattern.matcher(currentObject);
                if (matcher.find()) {
                    return Double.parseDouble(matcher.group(1));
                }
            }
        } catch (NumberFormatException e) {
            // Return 0 if parsing fails
        }
        return 0.0;
    }

    private static String[] parseJsonArray(String json, String arrayKey) {
        try {
            Pattern pattern = Pattern.compile("\"" + arrayKey + "\"\\s*:\\s*\\[([^\\]]+)\\]");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                String arrayContent = matcher.group(1);
                // Remove quotes and split by commas
                String[] items = arrayContent.replace("\"", "").split(",");
                for (int i = 0; i < items.length; i++) {
                    items[i] = items[i].trim();
                }
                return items;
            }
        } catch (Exception e) {
            // Return empty array if parsing fails
        }
        return new String[0];
    }

    private static void displayDetailedWeather(String jsonResponse, String cityName, String countryCode) {
        double temp = parseJsonDoubleFromCurrent(jsonResponse, "temperature_2m");
        double feelsLike = parseJsonDoubleFromCurrent(jsonResponse, "apparent_temperature");
        int humidity = (int) parseJsonDoubleFromCurrent(jsonResponse, "relative_humidity_2m");
        double pressure = parseJsonDoubleFromCurrent(jsonResponse, "pressure_msl");
        int weatherCode = (int) parseJsonDoubleFromCurrent(jsonResponse, "weather_code");
        double windSpeed = parseJsonDoubleFromCurrent(jsonResponse, "wind_speed_10m");
        int windDirection = (int) parseJsonDoubleFromCurrent(jsonResponse, "wind_direction_10m");
        double precipitation = parseJsonDoubleFromCurrent(jsonResponse, "precipitation");
        int cloudCover = (int) parseJsonDoubleFromCurrent(jsonResponse, "cloud_cover");
        int isDay = (int) parseJsonDoubleFromCurrent(jsonResponse, "is_day");

        String description = getWeatherDescription(weatherCode);
        String emoji = getWeatherEmoji(weatherCode, isDay == 1);

        System.out.println(GREEN + "\n" + "=".repeat(55));
        System.out.println(BOLD + "           DETAILED WEATHER REPORT");
        System.out.println("=".repeat(55) + RESET);

        System.out.println(CYAN + BOLD + "📍 Location: " + RESET + cityName +
                (countryCode.isEmpty() ? "" : ", " + countryCode.toUpperCase()));
        System.out.println(BLUE + "🕒 Time: " + RESET +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println();

        System.out.println(YELLOW + emoji + " Weather: " + RESET + description);
        System.out.println(RED + "🌡️  Temperature: " + RESET + String.format("%.1f°C", temp));
        System.out.println(GREEN + "🤚 Feels like: " + RESET + String.format("%.1f°C", feelsLike));
        System.out.println(BLUE + "💧 Humidity: " + RESET + humidity + "%");
        System.out.println(CYAN + "🌬️  Wind: " + RESET + String.format("%.1f km/h %s", windSpeed, getWindDirection(windDirection)));
        System.out.println(YELLOW + "🔽 Pressure: " + RESET + String.format("%.1f hPa", pressure));
        System.out.println(PURPLE + "☁️  Cloud Cover: " + RESET + cloudCover + "%");
        if (precipitation > 0) {
            System.out.println(BLUE + "🌧️  Precipitation: " + RESET + String.format("%.1f mm", precipitation));
        }

        System.out.println(GREEN + "=".repeat(55) + RESET);
    }

    private static void displayForecast(String jsonResponse, String cityName, String countryCode) {
        // Extract daily forecast arrays using simple parsing
        String[] dates = parseJsonArray(jsonResponse, "time");

        System.out.println(GREEN + "\n" + "=".repeat(65));
        System.out.println(BOLD + "              7-DAY WEATHER FORECAST");
        System.out.println("=".repeat(65) + RESET);
        System.out.println(CYAN + BOLD + "📍 Location: " + RESET + cityName +
                (countryCode.isEmpty() ? "" : ", " + countryCode.toUpperCase()));
        System.out.println();

        // Parse arrays manually for forecast data
        Pattern maxTempPattern = Pattern.compile("\"temperature_2m_max\"\\s*:\\s*\\[([^\\]]+)\\]");
        Pattern minTempPattern = Pattern.compile("\"temperature_2m_min\"\\s*:\\s*\\[([^\\]]+)\\]");
        Pattern weatherCodePattern = Pattern.compile("\"weather_code\"\\s*:\\s*\\[([^\\]]+)\\]");
        Pattern precipPattern = Pattern.compile("\"precipitation_sum\"\\s*:\\s*\\[([^\\]]+)\\]");
        Pattern windPattern = Pattern.compile("\"wind_speed_10m_max\"\\s*:\\s*\\[([^\\]]+)\\]");

        String[] maxTemps = extractArrayValues(jsonResponse, maxTempPattern);
        String[] minTemps = extractArrayValues(jsonResponse, minTempPattern);
        String[] weatherCodes = extractArrayValues(jsonResponse, weatherCodePattern);
        String[] precipitation = extractArrayValues(jsonResponse, precipPattern);
        String[] windSpeeds = extractArrayValues(jsonResponse, windPattern);

        int daysToShow = Math.min(7, Math.min(dates.length, maxTemps.length));

        for (int i = 0; i < daysToShow; i++) {
            try {
                String date = dates[i];
                double maxTemp = Double.parseDouble(maxTemps[i]);
                double minTemp = Double.parseDouble(minTemps[i]);
                int weatherCode = (int) Double.parseDouble(weatherCodes[i]);
                double precip = i < precipitation.length ? Double.parseDouble(precipitation[i]) : 0;
                double wind = i < windSpeeds.length ? Double.parseDouble(windSpeeds[i]) : 0;

                String description = getWeatherDescription(weatherCode);
                String emoji = getWeatherEmoji(weatherCode, true);

                System.out.printf("%s📅 %s%s\n", BLUE, date, RESET);
                System.out.printf("   %s %s\n", emoji, description);
                System.out.printf("   🌡️  %.1f°C - %.1f°C", minTemp, maxTemp);
                if (precip > 0) {
                    System.out.printf(" | 🌧️  %.1fmm", precip);
                }
                if (wind > 0) {
                    System.out.printf(" | 🌬️  %.1fkm/h", wind);
                }
                System.out.println("\n");
            } catch (Exception e) {
                // Skip this day if parsing fails
                continue;
            }
        }

        System.out.println(GREEN + "=".repeat(65) + RESET);
    }

    private static String[] extractArrayValues(String json, Pattern pattern) {
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            return arrayContent.split(",");
        }
        return new String[0];
    }

    private static void displaySimpleWeather(String jsonResponse, String cityName) {
        // Parse wttr.in JSON response
        try {
            double temp = parseJsonDouble(jsonResponse, "temp_C");
            double feelsLike = parseJsonDouble(jsonResponse, "FeelsLikeC");
            int humidity = (int) parseJsonDouble(jsonResponse, "humidity");
            int windSpeed = (int) parseJsonDouble(jsonResponse, "windspeedKmph");

            String description = parseJsonString(jsonResponse, "value");
            String windDir = parseJsonString(jsonResponse, "winddir16Point");

            // Try to extract area name
            String areaName = cityName;
            String country = "";

            Pattern areaPattern = Pattern.compile("\"areaName\".*?\"value\"\\s*:\\s*\"([^\"]+)\"");
            Pattern countryPattern = Pattern.compile("\"country\".*?\"value\"\\s*:\\s*\"([^\"]+)\"");

            Matcher areaMatcher = areaPattern.matcher(jsonResponse);
            Matcher countryMatcher = countryPattern.matcher(jsonResponse);

            if (areaMatcher.find()) {
                areaName = areaMatcher.group(1);
            }
            if (countryMatcher.find()) {
                country = countryMatcher.group(1);
            }

            System.out.println(GREEN + "\n" + "=".repeat(50));
            System.out.println(BOLD + "        SIMPLE WEATHER REPORT");
            System.out.println("=".repeat(50) + RESET);

            System.out.println(CYAN + BOLD + "📍 Location: " + RESET + areaName +
                    (country.isEmpty() ? "" : ", " + country));
            System.out.println();

            System.out.println(YELLOW + "🌤️  Weather: " + RESET + description);
            System.out.println(RED + "🌡️  Temperature: " + RESET + (int)temp + "°C");
            System.out.println(GREEN + "🤚 Feels like: " + RESET + (int)feelsLike + "°C");
            System.out.println(BLUE + "💧 Humidity: " + RESET + humidity + "%");
            System.out.println(CYAN + "🌬️  Wind: " + RESET + windSpeed + " km/h " + windDir);

        } catch (Exception e) {
            System.out.println(RED + "❌ Error parsing weather data" + RESET);
        }

        System.out.println(GREEN + "=".repeat(50) + RESET);
    }

    private static void displayWeatherComparison(WeatherData city1, WeatherData city2) {
        System.out.println(GREEN + "\n" + "=".repeat(65));
        System.out.println(BOLD + "              WEATHER COMPARISON");
        System.out.println("=".repeat(65) + RESET);

        System.out.printf("%-25s | %-25s\n",
                CYAN + BOLD + city1.name + ", " + city1.country + RESET,
                CYAN + BOLD + city2.name + ", " + city2.country + RESET);
        System.out.println("-".repeat(65));

        String emoji1 = getWeatherEmoji(city1.weatherCode, true);
        String emoji2 = getWeatherEmoji(city2.weatherCode, true);
        String desc1 = getWeatherDescription(city1.weatherCode);
        String desc2 = getWeatherDescription(city2.weatherCode);

        System.out.printf("%-25s | %-25s\n", emoji1 + " " + desc1, emoji2 + " " + desc2);
        System.out.printf("🌡️  %-22.1f°C | 🌡️  %-22.1f°C\n", city1.temperature, city2.temperature);
        System.out.printf("💧 %-23d%% | 💧 %-23d%%\n", city1.humidity, city2.humidity);
        System.out.printf("🌬️  %-20.1fkm/h | 🌬️  %-20.1fkm/h\n", city1.windSpeed, city2.windSpeed);

        System.out.println("\n📊 Analysis:");
        double tempDiff = Math.abs(city1.temperature - city2.temperature);
        if (city1.temperature > city2.temperature) {
            System.out.printf("   %s is %.1f°C warmer than %s\n", city1.name, tempDiff, city2.name);
        } else if (city2.temperature > city1.temperature) {
            System.out.printf("   %s is %.1f°C warmer than %s\n", city2.name, tempDiff, city1.name);
        } else {
            System.out.println("   Both cities have the same temperature!");
        }

        System.out.println(GREEN + "=".repeat(65) + RESET);
    }

    // Weather code mapping for Open-Meteo API
    private static String getWeatherDescription(int weatherCode) {
        switch (weatherCode) {
            case 0: return "Clear sky";
            case 1: return "Mainly clear";
            case 2: return "Partly cloudy";
            case 3: return "Overcast";
            case 45: return "Fog";
            case 48: return "Depositing rime fog";
            case 51: return "Light drizzle";
            case 53: return "Moderate drizzle";
            case 55: return "Dense drizzle";
            case 61: return "Light rain";
            case 63: return "Moderate rain";
            case 65: return "Heavy rain";
            case 71: return "Light snow";
            case 73: return "Moderate snow";
            case 75: return "Heavy snow";
            case 80: return "Light rain showers";
            case 81: return "Moderate rain showers";
            case 82: return "Heavy rain showers";
            case 85: return "Light snow showers";
            case 86: return "Heavy snow showers";
            case 95: return "Thunderstorm";
            case 96: return "Thunderstorm with light hail";
            case 99: return "Thunderstorm with heavy hail";
            default: return "Unknown";
        }
    }

    private static String getWeatherEmoji(int weatherCode, boolean isDay) {
        switch (weatherCode) {
            case 0: return isDay ? "☀️" : "🌙";
            case 1: return isDay ? "🌤️" : "🌙";
            case 2:
            case 3: return "☁️";
            case 45:
            case 48: return "🌫️";
            case 51:
            case 53:
            case 55: return "🌦️";
            case 61:
            case 63:
            case 65:
            case 80:
            case 81:
            case 82: return "🌧️";
            case 71:
            case 73:
            case 75:
            case 85:
            case 86: return "❄️";
            case 95:
            case 96:
            case 99: return "⛈️";
            default: return "🌤️";
        }
    }

    private static String getWindDirection(int degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(degrees / 22.5) % 16;
        return directions[index];
    }

    // Helper class for weather data
    static class WeatherData {
        String name;
        String country;
        double temperature;
        int humidity;
        int weatherCode;
        double windSpeed;

        public WeatherData(String name, String country, double temperature, int humidity, int weatherCode, double windSpeed) {
            this.name = name;
            this.country = country;
            this.temperature = temperature;
            this.humidity = humidity;
            this.weatherCode = weatherCode;
            this.windSpeed = windSpeed;
        }
    }
}
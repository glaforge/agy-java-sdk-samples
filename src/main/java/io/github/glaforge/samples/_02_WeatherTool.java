package io.github.glaforge.samples;

import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;
import io.github.glaforge.antigravity.tools.Param;
import io.github.glaforge.antigravity.tools.Tool;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.cyan;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;
import static io.github.glaforge.ansiren.Ansi.yellow;

/**
 * Sample 02: Demonstrates local Java tools and strongly-typed Structured Output.
 *
 * <p>1. The LLM discovers and invokes the local @Tool get_weather to fetch data.
 * <p>2. The agent returns its final decision formatted according to a JSON Schema derived
 * automatically from a Java record (WeatherAdvisory) via finishToolSchema(Class).
 * <p>3. The response is deserialized into the record using response.getStructuredOutput(Class).
 */
public class _02_WeatherTool {

    /**
     * Tool return data: Raw weather conditions returned by the local tool.
     */
    public record WeatherReport(String city, String condition, int temperatureCelsius, int humidityPercent) {}

    /**
     * Agent structured output: Strongly-typed response schema enforced by the agent.
     */
    public record WeatherAdvisory(
            String city,
            int temperatureCelsius,
            String condition,
            String clothingRecommendation,
            boolean umbrellaNeeded,
            List<String> suggestedActivities
    ) {}

    /**
     * Local tool class containing the tool method.
     */
    public static class WeatherTools {

        @Tool(name = "get_weather", description = "Get current weather conditions and temperature for a given city.")
        public WeatherReport getWeather(
                @Param(name = "city", description = "The name of the city, e.g. Paris, Tokyo, London") String city
        ) {
            // Visual indicator on stdout when the tool is invoked by the LLM
            System.out.println(bold(yellow("⚡ [Local Tool Invoked] "))
                    + yellow("get_weather(city=\"" + city + "\")"));

            if (city != null && city.toLowerCase().contains("paris")) {
                return new WeatherReport("Paris", "Sunny with mild breeze", 22, 55);
            } else if (city != null && city.toLowerCase().contains("london")) {
                return new WeatherReport("London", "Light drizzle", 15, 82);
            } else if (city != null && city.toLowerCase().contains("tokyo")) {
                return new WeatherReport("Tokyo", "Clear sky", 26, 60);
            } else {
                return new WeatherReport(city, "Partly cloudy", 19, 65);
            }
        }
    }

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 02 Local Weather Tool (Structured Output) ===\n")
                .reset());

        // Register our WeatherTools and configure the structured output schema
        AgentConfig config = AgentConfig.builder()
                .instructions("""
                        You are a helpful weather assistant with access to local tools.
                        Always use the get_weather tool when asked about the weather before advising the user.
                        """)
                .addTool(new WeatherTools())
                .finishToolSchema(WeatherAdvisory.class) // <-- Enforces structured output matching Java record
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "What is the current weather in Paris? Give me clothing advice and things to do.";
            System.out.println(bold(blue("User: ")) + prompt);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response = agent.chat(prompt).get(120, TimeUnit.SECONDS);

            // 1. Raw JSON produced by the model
            System.out.println(ready().faint().cyan().append("\n[Raw JSON Response]").reset());
            System.out.println(ready().faint().append(response.text() + "\n").reset());

            // 2. Deserialize directly into strongly-typed Java record
            WeatherAdvisory advisory = response.getStructuredOutput(WeatherAdvisory.class);

            System.out.println(bold(green("Parsed Structured Output (Java Record):")));
            System.out.println("  • City:          " + bold(advisory.city()));
            System.out.println("  • Temperature:   " + advisory.temperatureCelsius() + "°C");
            System.out.println("  • Conditions:    " + advisory.condition());
            System.out.println("  • Clothing:      " + advisory.clothingRecommendation());
            System.out.println("  • Umbrella:      " + (advisory.umbrellaNeeded() ? "Yes ☂️" : "No ☀️"));
            System.out.println("  • Activities:    " + String.join(", ", advisory.suggestedActivities()));

        } catch (Exception e) {
            System.err.println(red("Error running agent: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}

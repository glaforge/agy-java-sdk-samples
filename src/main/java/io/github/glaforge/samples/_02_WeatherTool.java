package io.github.glaforge.samples;

import io.github.glaforge.ansiren.MarkdownRenderer;
import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;
import io.github.glaforge.antigravity.tools.Param;
import io.github.glaforge.antigravity.tools.Tool;

import java.util.concurrent.TimeUnit;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;
import static io.github.glaforge.ansiren.Ansi.yellow;

/**
 * Sample 02: Demonstrates how to register a local Java tool using @Tool and @Param annotations.
 * The LLM autonomously discovers and invokes the tool to retrieve weather data for Paris.
 */
public class _02_WeatherTool {

    /**
     * Data carrier record representing the weather report.
     * Jackson automatically serializes this to JSON for the LLM.
     */
    public record WeatherReport(String city, String condition, int temperatureCelsius, int humidityPercent) {}

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
                .append("\n=== Antigravity Java SDK - 02 Local Weather Tool ===\n")
                .reset());

        // Register our WeatherTools with the agent configuration
        AgentConfig config = AgentConfig.builder()
                .instructions("""
                        You are a helpful assistant with access to local tools.
                        Always use the get_weather tool when asked about the weather.
                        """)
                .addTool(new WeatherTools())
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "What is the current weather in Paris? Please include the temperature and conditions.";
            System.out.println(bold(blue("User: ")) + prompt);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response = agent.chat(prompt).get(120, TimeUnit.SECONDS);

            System.out.println(bold(green("Agent:")));
            MarkdownRenderer markdownRenderer = new MarkdownRenderer();
            System.out.println(markdownRenderer.render(response.text()));
        } catch (Exception e) {
            System.err.println(red("Error running agent: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}

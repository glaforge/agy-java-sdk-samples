/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.glaforge.samples;

import io.github.glaforge.ansiren.MarkdownRenderer;
import io.github.glaforge.antigravity.Agent;
import io.github.glaforge.antigravity.AgentConfig;
import io.github.glaforge.antigravity.AgentResponse;
import io.github.glaforge.antigravity.ToolContext;
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
 * Sample 06: Demonstrates multi-turn conversation memory and state management.
 * Shows conversation history continuity across turns and ToolContext injection
 * for storing and retrieving session-scoped state without polluting tool schemas.
 */
public class _06_MultiTurnChat {

    public static class ProfileTools {

        @Tool(name = "save_preference", description = "Save a user preference key-value pair into the active session.")
        public String savePreference(
                @Param(name = "key", description = "Preference key, e.g. favorite_color, coffee_roast") String key,
                @Param(name = "value", description = "Preference value") String value,
                ToolContext context // Automatically injected by SDK runtime
        ) {
            context.setState(key, value);
            System.out.println(bold(yellow("⚡ [ToolContext Updated] "))
                    + yellow(key + " = " + value + " (Conversation: " + context.getConversationId() + ")"));
            return "Stored " + key + " = " + value + " in session state.";
        }

        @Tool(name = "get_preference", description = "Retrieve a user preference from the active session.")
        public String getPreference(
                @Param(name = "key", description = "Preference key to retrieve") String key,
                ToolContext context // Automatically injected by SDK runtime
        ) {
            Object val = context.getState(key, "Unknown");
            System.out.println(bold(yellow("⚡ [ToolContext Retrieved] "))
                    + yellow(key + " -> " + val));
            return "Stored value for " + key + ": " + val;
        }
    }

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 06 Multi-Turn Chat & ToolContext ===\n")
                .reset());

        AgentConfig config = AgentConfig.builder()
                .instructions("""
                        You are a personalized assistant with access to user profile tools.
                        Save preferences when told, and consult preferences when relevant.
                        """)
                .addTool(new ProfileTools())
                .build();

        try (Agent agent = new Agent(config)) {
            MarkdownRenderer renderer = new MarkdownRenderer();

            // Turn 1: User introduces themselves and sets a preference
            System.out.println(bold(yellow("--- Turn 1: Introducing and Storing Preferences ---")));
            String prompt1 = "Hello! My name is Guillaume. Please remember that my favorite coffee is an Ethiopian dark roast.";
            System.out.println(bold(blue("User: ")) + prompt1);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response1 = agent.chat(prompt1).get(120, TimeUnit.SECONDS);
            System.out.println(bold(green("Agent:")));
            System.out.println(renderer.render(response1.text()));

            // Turn 2: Follow-up question relying on memory and ToolContext
            System.out.println(bold(yellow("\n--- Turn 2: Follow-up Question in the Same Session ---")));
            String prompt2 = "Can you recommend a morning beverage for me based on my preferences?";
            System.out.println(bold(blue("User: ")) + prompt2);
            System.out.println(ready().faint().italic().append("Waiting for agent response...\n").reset());

            AgentResponse response2 = agent.chat(prompt2).get(120, TimeUnit.SECONDS);
            System.out.println(bold(green("Agent:")));
            System.out.println(renderer.render(response2.text()));

            System.out.println(ready().faint().append("\nConversation ID: " + agent.getConversationId() + "\n").reset());

        } catch (Exception e) {
            System.err.println(red("Error running agent: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}

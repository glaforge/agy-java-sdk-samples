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

import java.util.concurrent.TimeUnit;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;

/**
 * Sample 01: A simple Hello World demonstrating how to create and chat with an Agent
 * using the unofficial Antigravity SDK for Java, styled with Ansiren for ANSI colors
 * and terminal Markdown rendering.
 */
public class _01_HelloWorld {

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 01 Hello World ===\n")
                .reset());

        // Configure the agent with system instructions
        AgentConfig config = AgentConfig.builder()
                .instructions("You are a helpful and concise AI assistant.")
                .build();

        // Always wrap Agent in try-with-resources to ensure proper cleanup of the Go harness process
        try (Agent agent = new Agent(config)) {
            String prompt = "Hello! Please introduce yourself in two short bullet points.";
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

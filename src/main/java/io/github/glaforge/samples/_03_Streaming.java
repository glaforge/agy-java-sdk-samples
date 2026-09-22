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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.github.glaforge.ansiren.Ansi.blue;
import static io.github.glaforge.ansiren.Ansi.bold;
import static io.github.glaforge.ansiren.Ansi.cyan;
import static io.github.glaforge.ansiren.Ansi.green;
import static io.github.glaforge.ansiren.Ansi.ready;
import static io.github.glaforge.ansiren.Ansi.red;

/**
 * Sample 03: Demonstrates token-by-token streaming using chatStream().
 * Shows real-time response rendering and separates model thinking from final text.
 */
public class _03_Streaming {

    public static void main(String[] args) {
        System.out.println(ready()
                .bold().brightCyan()
                .append("\n=== Antigravity Java SDK - 03 Streaming Responses ===\n")
                .reset());

        AgentConfig config = AgentConfig.builder()
                .instructions("You are a creative and poetic AI assistant.")
                .build();

        try (Agent agent = new Agent(config)) {
            String prompt = "Write a sonnet (4, 4, 3, 3 stanzas) about an astronaut enjoying a morning espresso on Mars.";
            System.out.println(bold(blue("User: ")) + prompt);
            System.out.println(ready().faint().italic().append("Streaming response in real-time...\n").reset());

            System.out.println(bold(green("Agent:")));

            AtomicBoolean firstThought = new AtomicBoolean(true);
            AtomicBoolean firstText = new AtomicBoolean(true);

            CompletableFuture<AgentResponse> future = agent.chatStream(prompt, chunk -> {
                // Stream model thoughts (if any) in faint italic
                if (!chunk.thoughtsDelta().isEmpty()) {
                    if (firstThought.compareAndSet(true, false)) {
                        System.out.print(ready().faint().italic().cyan().append("[Thinking] ").reset());
                    }
                    System.out.print(ready().faint().italic().append(chunk.thoughtsDelta()).reset());
                }

                // Stream response text tokens in real time
                if (!chunk.textDelta().isEmpty()) {
                    if (firstText.compareAndSet(true, false) && !firstThought.get()) {
                        System.out.println("\n");
                    }
                    System.out.print(chunk.textDelta());
                }
            });

            AgentResponse response = future.get(120, TimeUnit.SECONDS);

            System.out.println("\n");
            if (response.usageMetadata() != null) {
                System.out.println(ready().faint().append(String.format(
                        "--- (Tokens: %d prompt, %d candidates, %d total) ---",
                        response.usageMetadata().promptTokenCount(),
                        response.usageMetadata().candidatesTokenCount(),
                        response.usageMetadata().totalTokenCount()
                )).reset());
            }
        } catch (Exception e) {
            System.err.println(red("Error running agent: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}

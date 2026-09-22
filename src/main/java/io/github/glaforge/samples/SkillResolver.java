package io.github.glaforge.samples;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Utility to resolve Agent Skill directories robustly across CLI runs, IDEs, and JAR packaging.
 *
 * <p>Because the underlying Go harness binary (localharness) runs as a separate OS process,
 * it requires an absolute directory path on the local filesystem. This utility:
 * <ol>
 *   <li>Checks if the path exists on the filesystem, resolving it to an absolute path.</li>
 *   <li>Checks if the path is packaged as a classpath resource (e.g., in src/main/resources or target/classes).</li>
 *   <li>If on the classpath as an exploded directory (standard IDE execution), returns its absolute path.</li>
 *   <li>If on the classpath inside a JAR, extracts the skill files to a temporary directory.</li>
 * </ol>
 */
public final class SkillResolver {

    private SkillResolver() {}

    /**
     * Resolves a skill directory path to an absolute filesystem path that localharness can access.
     *
     * @param skillPath relative filesystem path or classpath resource path (e.g. "skills/antigravity-sdk-java")
     * @return an absolute filesystem path pointing to the skill directory
     */
    public static String resolveSkillPath(String skillPath) {
        // 1. Check if the path exists directly on the filesystem (e.g. CLI run from project root)
        Path directPath = Path.of(skillPath);
        if (Files.exists(directPath)) {
            return directPath.toAbsolutePath().toString();
        }

        // 2. Check if it's on the classpath (e.g. src/main/resources, target/classes)
        String normalizedResource = skillPath.startsWith("/") ? skillPath.substring(1) : skillPath;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = SkillResolver.class.getClassLoader();
        }

        URL resource = cl.getResource(normalizedResource);
        if (resource == null && !normalizedResource.startsWith("skills/")) {
            resource = cl.getResource("skills/" + normalizedResource);
        }

        if (resource != null) {
            if ("file".equalsIgnoreCase(resource.getProtocol())) {
                try {
                    return Path.of(resource.toURI()).toAbsolutePath().toString();
                } catch (Exception ignored) {
                }
            } else if ("jar".equalsIgnoreCase(resource.getProtocol())) {
                try {
                    return extractSkillFromJar(resource).toAbsolutePath().toString();
                } catch (Exception e) {
                    System.err.println("Warning: failed to extract skill from JAR: " + e.getMessage());
                }
            }
        }

        // 3. Check relative to code source directory (e.g. project root when running from an IDE)
        try {
            var codeSource = SkillResolver.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                URI uri = codeSource.getLocation().toURI();
                if ("file".equalsIgnoreCase(uri.getScheme())) {
                    Path candidate = Path.of(uri);
                    while (candidate != null) {
                        Path check = candidate.resolve(skillPath);
                        if (Files.exists(check)) {
                            return check.toAbsolutePath().toString();
                        }
                        candidate = candidate.getParent();
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // 4. Fallback: try user.dir relative check if user.dir was different
        Path workingDirRelative = Path.of(System.getProperty("user.dir", "."), skillPath);
        if (Files.exists(workingDirRelative)) {
            return workingDirRelative.toAbsolutePath().toString();
        }

        // Fallback to absolute path of given argument
        return directPath.toAbsolutePath().toString();
    }

    private static Path extractSkillFromJar(URL resource) throws IOException {
        Path tempDir = Files.createTempDirectory("antigravity-skill-");
        tempDir.toFile().deleteOnExit();

        URI uri = URI.create(resource.toString());
        String[] parts = uri.toString().split("!");
        String jarUri = parts[0];
        String internalPath = parts[1];

        try (FileSystem fs = FileSystems.newFileSystem(URI.create(jarUri), Collections.emptyMap())) {
            Path source = fs.getPath(internalPath);
            try (Stream<Path> walk = Files.walk(source)) {
                walk.forEach(src -> {
                    try {
                        Path rel = source.relativize(src);
                        Path dest = tempDir.resolve(rel.toString());
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
        return tempDir;
    }
}

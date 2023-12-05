package de.verdox.mccreativelab;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class PluginLoader implements io.papermc.paper.plugin.loader.PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(new DefaultArtifact("io.vertx:vertx-core:4.5.0"), null));
        resolver.addRepository(new RemoteRepository.Builder("maven","default","https://repo.maven.apache.org/maven2/.").build());
        classpathBuilder.addLibrary(resolver);
    }
}

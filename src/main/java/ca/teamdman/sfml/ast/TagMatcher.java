package ca.teamdman.sfml.ast;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TagMatcher implements Predicate<Object>, ASTNode {
    public final String namespacePattern;
    public final List<String> pathElementPatterns;
    private final Predicate<String> namespacePredicate;
    private final List<Predicate<String>> pathElementPredicates;

    @Override
    public String toString() {
        return namespacePattern + ":" + String.join("/", pathElementPatterns);
    }

    private TagMatcher(
            String namespacePattern,
            Collection<String> pathElementPatterns
    ) {
        this.namespacePattern = namespacePattern;
        this.pathElementPatterns = new ArrayList<>(pathElementPatterns);
        this.namespacePredicate = RegexCache.buildPredicate(namespacePattern);
        this.pathElementPredicates = this.pathElementPatterns.stream().map(RegexCache::buildPredicate).collect(Collectors.toList());
    }

    public static TagMatcher fromNamespaceAndPath(String namespace, Collection<String> path) {
        return new TagMatcher(namespace, new ArrayList<>(path));
    }

    public static TagMatcher fromPath(Collection<String> pathElements) {
        return new TagMatcher(".*", pathElements);
    }

    @Override
    public boolean test(Object o) {
        if (o instanceof ResourceLocation) {
            return testResourceLocation((ResourceLocation)o);
        } else if (o instanceof String ) {
            return testString((String)o);
        }
        return false;
    }

    public boolean testResourceLocation(ResourceLocation resourceLocation) {
        return testPath(resourceLocation.getNamespace(), resourceLocation.getPath().split("/"));
    }

    public boolean testString(String string) {
        String[] chunks = string.split(":");
        if (chunks.length == 0) {
            return false;
        } else if (chunks.length == 1) {
            return testPath("", string.split("/"));
        } else {
            return testPath(chunks[0], chunks[1].split("/"));
        }
    }

    private boolean testPath(
            String checkNamespace,
            String[] checkPathElements
    ) {
        if (checkPathElements.length < this.pathElementPatterns.size()) {
            return false;
        }
        if (!namespacePredicate.test(checkNamespace)) {
            return false;
        }
        for (int i = 0; i < checkPathElements.length; i++) {
            // fail if path has more elements than pattern
            if (i >= this.pathElementPatterns.size()) {
                return false;
            }
            // succeed when pattern is deep-match-all
            if (this.pathElementPatterns.get(i).equals(".*.*")) {
                return true;
            }
            // fail if path element does not match pattern
            if (!pathElementPredicates.get(i).test(checkPathElements[i])) {
                return false;
            }
        }
        return true;
    }
}

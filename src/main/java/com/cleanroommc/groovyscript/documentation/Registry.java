package com.cleanroommc.groovyscript.documentation;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.compat.mods.ModPropertyContainer;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.documentation.annotations.*;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.google.common.collect.ComparisonChain;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;


public class Registry {

    private final ModSupport.Container<? extends ModPropertyContainer> mod;
    private final VirtualizedRegistry<?> registry;
    private final String baseTranslationKey;
    private final String reference;
    private final Class<?> registryClass;
    private final RegistryDescription description;
    private final List<Method> recipeBuilderMethods;
    private final EnumMap<MethodDescription.Type, List<Method>> methods = new EnumMap<>(MethodDescription.Type.class);
    private final List<String> imports;

    public Registry(ModSupport.Container<? extends ModPropertyContainer> mod, VirtualizedRegistry<?> registry) {
        this.mod = mod;
        this.registry = registry;
        this.baseTranslationKey = String.format("groovyscript.wiki.%s.%s", mod.getId(), registry.getName());
        this.reference = String.format("mods.%s.%s", mod.getId(), registry.getName());
        this.registryClass = registry.getClass();
        this.description = registryClass.getAnnotation(RegistryDescription.class);

        List<Method> recipeBuilderMethods = new ArrayList<>();
        EnumMap<MethodDescription.Type, List<Method>> methods = new EnumMap<>(MethodDescription.Type.class);
        for (MethodDescription.Type value : MethodDescription.Type.values()) methods.put(value, new ArrayList<>());
        List<String> imports = new ArrayList<>();

        for (Method method : registryClass.getMethods()) {
            if (method.isAnnotationPresent(GroovyBlacklist.class)) continue;
            if (method.isAnnotationPresent(RecipeBuilderDescription.class)) {
                recipeBuilderMethods.add(method);
                for (Example example : method.getAnnotation(RecipeBuilderDescription.class).example()) {
                    Collections.addAll(imports, example.imports());
                }
            }
            if (method.isAnnotationPresent(MethodDescription.class)) {
                MethodDescription description = method.getAnnotation(MethodDescription.class);
                methods.get(description.type()).add(method);
                for (Example example : description.example()) {
                    Collections.addAll(imports, example.imports());
                }
            }
        }

        this.recipeBuilderMethods = sortGrSRecipeBuilderDescriptionMethods(recipeBuilderMethods);
        methods.forEach((k, v) -> this.methods.put(k, sortGrSMethodDescriptionMethods(v)));
        this.imports = imports;
    }

    private static List<Method> sortGrSRecipeBuilderDescriptionMethods(List<Method> methods) {
        methods.sort((left, right) -> ComparisonChain.start()
                .compareFalseFirst(left.isAnnotationPresent(RecipeBuilderDescription.class), right.isAnnotationPresent(RecipeBuilderDescription.class))
                .compare(left.getAnnotation(RecipeBuilderDescription.class).priority(), right.getAnnotation(RecipeBuilderDescription.class).priority())
                .compare(left.getName(), right.getName(), String::compareToIgnoreCase)
                .compare(Exporter.simpleName(left), Exporter.simpleName(right), String::compareToIgnoreCase)
                .result());
        return methods;
    }

    private static List<Method> sortGrSMethodDescriptionMethods(List<Method> methods) {
        methods.sort((left, right) -> ComparisonChain.start()
                .compareFalseFirst(left.isAnnotationPresent(MethodDescription.class), right.isAnnotationPresent(MethodDescription.class))
                .compare(left.getAnnotation(MethodDescription.class).priority(), right.getAnnotation(MethodDescription.class).priority())
                .compare(left.getName(), right.getName(), String::compareToIgnoreCase)
                .result());
        return methods;
    }

    private static List<Example> getExamples(Method method) {
        return method.isAnnotationPresent(MethodDescription.class)
               ? new ArrayList<>(Arrays.asList(method.getAnnotation(MethodDescription.class).example()))
               : new ArrayList<>();
    }

    private static List<Example> sortExamples(List<Example> examples) {
        examples.sort((left, right) -> ComparisonChain.start()
                .compare(left.priority(), right.priority())
                .compareFalseFirst(left.commented(), right.commented())
                .compare(left.value().length(), right.value().length())
                .compare(left.value(), right.value())
                .result());
        return examples;
    }

    public List<String> getImports() {
        return this.imports;
    }

    public String getFileSourceCodeLink() {
        return LinkGenerator.convert(description.linkGenerator(), registryClass);
    }

    public String getTitle() {
        return I18n.format(description.title().isEmpty() ? String.format("%s.title", baseTranslationKey) : description.title());
    }

    public String getDescription() {
        return I18n.format(description.description().isEmpty() ? String.format("%s.description", baseTranslationKey) : description.description());
    }

    public String exampleBlock() {
        StringBuilder out = new StringBuilder();
        out.append("// ").append(getTitle()).append(":").append("\n");
        out.append("// ").append(WordUtils.wrap(getDescription(), Documentation.MAX_LINE_LENGTH, "\n// ", false)).append("\n\n");
        for (Method method : methods.get(MethodDescription.Type.VALUE)) out.append(examples(method));
        if (!methods.get(MethodDescription.Type.VALUE).isEmpty()) out.append("\n");
        for (Method method : methods.get(MethodDescription.Type.REMOVAL)) out.append(examples(method));
        if (!methods.get(MethodDescription.Type.REMOVAL).isEmpty()) out.append("\n");
        for (Method method : recipeBuilderMethods) out.append(new Builder(method, reference, baseTranslationKey).builderExampleFile()).append("\n");
        if (!recipeBuilderMethods.isEmpty()) out.append("\n");
        for (Method method : methods.get(MethodDescription.Type.ADDITION)) out.append(examples(method));
        if (!methods.get(MethodDescription.Type.ADDITION).isEmpty()) out.append("\n");
        return out.toString();
    }

    private String generateHeader() {
        StringBuilder out = new StringBuilder();
        out.append("---\n").append("title: \"").append(getTitle()).append("\"\n");
        out.append("description: \"").append(getDescription()).append("\"\n");
        String link = getFileSourceCodeLink();
        if (!link.isEmpty()) out.append("source_code_link: \"").append(link).append("\"\n");
        out.append("---\n\n");
        return out.toString();
    }

    private String generateTitle() {
        return String.format("# %s (%s)\n\n", getTitle(), mod);
    }

    private String generateDescription() {
        StringBuilder out = new StringBuilder();
        out.append("## ").append(I18n.format("groovyscript.wiki.description")).append("\n\n");
        out.append(I18n.format(getDescription())).append("\n\n");

        Admonition[] admonition = description.admonition();
        for (Admonition note : admonition) {
            out.append(new AdmonitionBuilder()
                               .type(note.type())
                               .title(note.title())
                               .hasTitle(note.hasTitle())
                               .format(note.format())
                               .note(I18n.format(note.value()))
                               .generate())
                    .append("\n\n");
        }
        return out.toString();
    }

    private String generateIdentifier() {
        StringBuilder out = new StringBuilder();
        out.append("## ").append(I18n.format("groovyscript.wiki.identifier")).append("\n\n").append(I18n.format("groovyscript.wiki.import_instructions")).append("\n\n");

        List<String> packages = mod.getAliases().stream()
                .flatMap(modID -> registry.getAliases().stream().map(alias -> String.format("mods.%s.%s", modID, alias)))
                .collect(Collectors.toList());

        int target = packages.indexOf(reference);
        packages.set(target, reference + "/*()!*/");

        out.append(new CodeBlockBuilder()
                           .line(packages)
                           .annotation(I18n.format("groovyscript.wiki.defaultPackage"))
                           // Highlighting is based on the line count, and is 1-indexed
                           .highlight(String.valueOf(1 + target))
                           .toString());
        return out.toString();
    }

    private String recipeBuilder() {
        StringBuilder out = new StringBuilder();

        out.append("### ").append(I18n.format("groovyscript.wiki.recipe_builder")).append("\n\n")
                .append(I18n.format("groovyscript.wiki.uses_recipe_builder", I18n.format(getTitle()))).append("\n\n")
                .append(I18n.format("groovyscript.wiki.recipe_builder_note")).append("\n\n");

        for (Method method : recipeBuilderMethods) {
            Builder builder = new Builder(method, reference, baseTranslationKey);
            out.append(new AdmonitionBuilder()
                               .type(Admonition.Type.ABSTRACT)
                               .hasTitle(true)
                               .title(methodExample(method))
                               .note(builder.documentMethods().split("\n"))
                               .note("\n")
                               .note(builder.builderAdmonition().split("\n"))
                               .generate())
                    .append("\n\n");
        }
        return out.toString();
    }

    public String documentationBlock() {
        StringBuilder out = new StringBuilder();

        out.append(generateHeader());
        out.append(generateTitle());
        out.append(generateDescription());
        out.append(generateIdentifier());

        if (!methods.get(MethodDescription.Type.VALUE).isEmpty()) {
            out.append("## ").append(I18n.format("groovyscript.wiki.editing_values")).append("\n\n").append(documentMethods(methods.get(MethodDescription.Type.VALUE))).append("\n");
        }
        if (!methods.get(MethodDescription.Type.ADDITION).isEmpty() || !recipeBuilderMethods.isEmpty()) {
            out.append("## ").append(I18n.format(description.category().adding())).append("\n\n");
            if (!methods.get(MethodDescription.Type.ADDITION).isEmpty()) {
                out.append(documentMethods(methods.get(MethodDescription.Type.ADDITION))).append("\n");
            }
            if (!recipeBuilderMethods.isEmpty()) {
                out.append(recipeBuilder()).append("\n\n");
            }
        }
        if (!methods.get(MethodDescription.Type.REMOVAL).isEmpty()) {
            out.append("## ").append(I18n.format(description.category().removing())).append("\n\n").append(documentMethods(methods.get(MethodDescription.Type.REMOVAL))).append("\n");
        }
        if (!methods.get(MethodDescription.Type.QUERY).isEmpty()) {
            out.append("## ").append(I18n.format(description.category().query())).append("\n\n").append(documentMethods(methods.get(MethodDescription.Type.QUERY), true)).append("\n");
        }
        out.append("\n");

        return out.toString();
    }

    private String documentMethods(List<Method> methods) {
        return documentMethods(methods, false);
    }

    private String documentMethods(List<Method> methods, boolean preventExamples) {
        StringBuilder out = new StringBuilder();
        List<String> exampleLines = new ArrayList<>();
        List<String> annotations = new ArrayList<>();

        for (Method method : methods) {
            out.append(methodDescription(method));
            if (method.getAnnotation(MethodDescription.class).example().length > 0 && Arrays.stream(method.getAnnotation(MethodDescription.class).example()).anyMatch(x -> !x.value().isEmpty())) {
                exampleLines.addAll(Arrays.stream(method.getAnnotation(MethodDescription.class).example()).flatMap(example -> methodExample(method, example.value()).stream()).collect(Collectors.toList()));
            } else if (method.getParameterTypes().length == 0) {
                exampleLines.add(methodExample(method));
            }
            Arrays.stream(method.getAnnotation(MethodDescription.class).example()).map(Example::annotations).flatMap(Arrays::stream).forEach(annotations::add);
        }

        if (!exampleLines.isEmpty() && !preventExamples) {
            out.append(new AdmonitionBuilder()
                               .type(Admonition.Type.EXAMPLE)
                               .note(new CodeBlockBuilder()
                                             .line(exampleLines)
                                             .annotation(annotations)
                                             .generate())
                               .generate());
        }

        return out.toString();
    }

    private String methodDescription(Method method) {
        String desc = method.getAnnotation(MethodDescription.class).description();
        String lang = desc.isEmpty() ? String.format("%s.%s", baseTranslationKey, method.getName()) : desc;

        return String.format("- %s:\n\n%s",
                             I18n.format(lang),
                             new CodeBlockBuilder()
                                     .line(methodExample(method, Exporter.simpleName(method)))
                                     .indentation(1)
                                     .toString());
    }

    private List<String> methodExample(Method method, String example) {
        if (method.getParameterTypes().length == 0) return Collections.singletonList(methodExample(method));
        return Collections.singletonList(String.format("%s.%s(%s)", reference, method.getName(), example));
    }

    private String methodExample(Method method) {
        return String.format("%s.%s()", reference, method.getName());
    }

    private String examples(Method method) {
        StringBuilder out = new StringBuilder();
        for (Example example : sortExamples(getExamples(method))) {
            if (example.commented()) out.append("// ");
            if (!example.def().isEmpty()) out.append("def ").append(example.def()).append(" = ");
            out.append(reference).append(".").append(method.getName());
            if (example.value().isEmpty()) out.append("()");
            else out.append("(").append(example.value()).append(")");
            out.append("\n");
        }
        return out.toString();
    }

}

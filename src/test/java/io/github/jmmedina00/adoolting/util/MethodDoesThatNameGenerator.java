package io.github.jmmedina00.adoolting.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayNameGenerator;

public class MethodDoesThatNameGenerator implements DisplayNameGenerator {

  @Override
  public String generateDisplayNameForClass(Class<?> testClass) {
    return testClass.getSimpleName().replace("Test", "");
  }

  @Override
  public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
    return generateDisplayNameForClass(nestedClass);
  }

  @Override
  public String generateDisplayNameForMethod(
    Class<?> testClass,
    Method testMethod
  ) {
    try {
      return describeMethodProperly(testClass, testMethod);
    } catch (ClassNotFoundException e) {
      return testMethod.getName();
    }
  }

  private Class<?> getTestedClass(Class<?> testClass)
    throws ClassNotFoundException {
    return Class.forName(testClass.getName().replace("Test", ""));
  }

  private String describeMethodProperly(Class<?> testClass, Method testMethod)
    throws ClassNotFoundException {
    Class<?> testedClass = getTestedClass(testClass);
    ArrayList<String> arguments = new ArrayList<>();

    String testName = testMethod.getName();

    List<String> classMethodNames = Arrays
      .stream(testedClass.getMethods())
      .map(method -> method.getName())
      .sorted((a, b) -> -(a.length() - b.length()))
      .toList();

    for (String methodName : classMethodNames) {
      if (testName.contains(methodName)) {
        arguments.add(methodName);
        testName = testName.replaceFirst(methodName, "%s");
      }
    }

    String finalName = testName;

    IntStream uppercases = IntStream
      .range(0, finalName.length())
      .filter(i -> Character.isUpperCase(finalName.charAt(i)));

    ArrayList<Integer> indexes = new ArrayList<>(uppercases.boxed().toList());
    indexes.add(0, 0);
    indexes.add(finalName.length());

    List<String> slices = IntStream
      .range(0, indexes.size() - 1)
      .boxed()
      .map(i -> finalName.substring(indexes.get(i), indexes.get(i + 1)))
      .toList();
    String message = String
      .join(" ", slices.toArray(new String[] {  }))
      .toLowerCase();

    return String.format(message, arguments.toArray());
  }
}

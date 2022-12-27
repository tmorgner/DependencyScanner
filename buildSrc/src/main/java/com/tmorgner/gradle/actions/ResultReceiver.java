package com.tmorgner.gradle.actions;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * Gradle wont let me create ListProperties as parameters for worker tasks, but I need to return data from those
 * workers.
 */
public class ResultReceiver<T> {
  private static final HashMap<Class<?>, ResultReceiver<?>> instances = new HashMap<>();
  private final Class<T> typeIndicator;
  private final Supplier<T> factoryMethod;
  private final HashMap<Integer, T> resultTasks;
  private int counter;

  public ResultReceiver(Class<T> typeIndicator) {
    this(typeIndicator, lookupConstructor(typeIndicator));
  }

  public ResultReceiver(Class<T> typeIndicator, Supplier<T> factoryMethod) {
    this.factoryMethod = factoryMethod;
    this.typeIndicator = typeIndicator;
    resultTasks = new HashMap<>();
  }

  static <TSupplier> Supplier<TSupplier> lookupConstructor(Class<TSupplier> typeIndicator) {
    return () -> {
      try {
        return typeIndicator.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
  }


  @SuppressWarnings("unchecked")
  public static <T> ResultReceiver<T> getInstance(Class<T> typeIndicator) {
    ResultReceiver<?> raw = instances.computeIfAbsent(typeIndicator, ResultReceiver::new);
    return (ResultReceiver<T>) raw;
  }

  public synchronized int prepareResult() {
    int result = counter;
    resultTasks.put(counter, factoryMethod.get());
    counter += 1;
    return result;
  }

  public synchronized void freeResult(int ticket) {
    resultTasks.remove(ticket);
  }

  public synchronized T getResult(int ticket) {
    final T result = resultTasks.get(ticket);
    if (result == null) {
      throw new IllegalArgumentException("Invalid ticket " + ticket + " while trying to retrieve " + typeIndicator);
    }

    return result;
  }


}

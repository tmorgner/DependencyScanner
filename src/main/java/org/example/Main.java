package org.example;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.Transient;
import java.util.ArrayList;
import java.util.function.Predicate;

@SuppressWarnings("unused")
class SomeOuterClass extends ArrayList<Integer> {

}

@SuppressWarnings("ALL")
@ClassAnnotation(value = "Test", extraData = 1)
@TypeAnnotation(SomeOuterClass.class)
public class Main {

  @Deprecated
  int field1;

  String field2;

  class InnerClass {
    public InnerClass() {
    }
  }

  static class StaticInnerClass {
    class NestedInnerClass {

    }
  }

  @MethodAnnotation
  public Main() {

    Predicate<String> p = this::ALambda;
    Predicate<String> p2 = s -> true;

    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {

      }
    });
  }

  public String aaMethod(Integer a, Double[] b, ArrayList<String> c) {
    return "";
  }

  public <T> T genericMethod(T t, Class<T> c) {
    return t;
  }

  @Transient
  public void addActionListener(ActionListener l) {

  }

  boolean ALambda(String s) {
    return false;
  }

  public static void main(String[] args) {
    System.out.println("Hello world!");
  }
}
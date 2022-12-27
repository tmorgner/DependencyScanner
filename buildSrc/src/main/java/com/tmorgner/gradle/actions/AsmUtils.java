package com.tmorgner.gradle.actions;

import org.objectweb.asm.Type;

public class AsmUtils {

  private AsmUtils() {
  }

  public static String getMethodName(String name, Type[] args){
    final StringBuilder b = new StringBuilder();
    b.append(name);
    b.append("(");
    for (int i = 0, argsLength = args.length; i < argsLength; i++) {
      final Type arg = args[i];
      if (i != 0) {
        b.append(",");
      }
      b.append(arg.getClassName());
    }
    b.append(")");
    return b.toString();
  }

  public static String getBaseName(Type t) {
    if (t.getSort() == Type.ARRAY) {
      return getBaseName(t.getElementType());
    }
    return t.getClassName();
  }

}

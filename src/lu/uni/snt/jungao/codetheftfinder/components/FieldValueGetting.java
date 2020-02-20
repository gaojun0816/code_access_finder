/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;

/**
 * @author jun.gao
 *
 */
public class FieldValueGetting extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.reflect.Field";
  private static final String[] SIG = {"java.lang.Object get(java.lang.Object)",
      "boolean getBoolean(java.lang.Object)", "byte getByte(java.lang.Object)","char getChar(java.lang.Object)",
      "double getDouble(java.lang.Object)","float getFloat(java.lang.Object)","int getInt(java.lang.Object)",
      "long getLong(java.lang.Object)","short getShort(java.lang.Object)"};
  
  private Value classRef;

  /**
   * @param stmt
   * @param container
   */
  public FieldValueGetting(Stmt stmt, SootMethod container) {
    super(stmt, container);
    classRef = getInvokeExprArg(0);
    tracker.add(this);
  }
  
  /**
   * Check if the field is a possible static field. It is decided by whether the setting is based on an instance.
   * Note: there are still possibilities that the field is not a static field while this method return true and vice versa.
   * 
   * @return
   */
  public Boolean isStaticFieldSetting() {
    return classRef instanceof NullConstant;
  }
  
  public Value getClassRef() {
    return classRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Field Value Getting -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Possible static field getting: ");
    if (isStaticFieldSetting()) {
      sb.append("Yes");
    } else {
      sb.append("No");
    }
    sb.append("\n---------------------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isFieldValueGetting(Stmt stmt) {
    Boolean itis = false;
    for (String sig : SIG) {
      if (StaticUtils.isIt(stmt, CLASS, sig))
        itis = true;
    }
    return itis;
  }
}

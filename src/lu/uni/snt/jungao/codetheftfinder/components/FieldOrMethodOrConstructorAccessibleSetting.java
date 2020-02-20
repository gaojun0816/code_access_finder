/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;

/**
 * @author jun.gao
 *
 */
public class FieldOrMethodOrConstructorAccessibleSetting extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS[] = {"java.lang.reflect.Field", "java.lang.reflect.Method", "java.lang.reflect.Constructor"};
  private static final String SIG = "void setAccessible(boolean)";
  
  private Value accessibilityRef;

  /**
   * @param stmt
   * @param container
   */
  public FieldOrMethodOrConstructorAccessibleSetting(Stmt stmt, SootMethod container) {
    super(stmt, container);
    accessibilityRef = getInvokeExprArg(0);
    tracker.add(this);
  }
  
  public List<String> getConstantAccessibility() {
    List<String> accessibility = null;
    if (accessibilityRef instanceof Constant) {
      accessibility = new ArrayList<>();
      accessibility.add(accessibilityRef.toString());
    } else {
      // TODO use constant propagation for further check.
    }
    return accessibility;
  }
  
  public Value getAcceccibilityRef() {
    return accessibilityRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Accessible Setting -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Invoker type: ");
    sb.append(invokerRef.getType().toString());
    sb.append("\n\t");
    sb.append("Accessibility: ");
    List<String> accessibility = getConstantAccessibility();
    if (accessibility != null) {
      sb.append(accessibility.toString());
    } else {
      sb.append("unkown");
    }
    sb.append("\n---------------------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isFieldOrMethodOrConstructorAccessibilitySetting(Stmt stmt) {
    Boolean itis = false;
    for (String cls : CLASS) {
      if (StaticUtils.isIt(stmt, cls, SIG)) {
        itis = true;
        break;
      }
    }
    return itis;
  }
}

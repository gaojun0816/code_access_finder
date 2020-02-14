/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;

/**
 * @author jun.gao
 *
 */
public class FieldValueSetting extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.reflect.Field";
  private static final String SIG = "void set(java.lang.Object,java.lang.Object)";
  
  private Value classRef, valueRef;

  /**
   * @param stmt
   * @param container
   */
  public FieldValueSetting(Stmt stmt, SootMethod container) {
    super(stmt, container);
    classRef = getInvokeExprArg(0);
    valueRef = getInvokeExprArg(1);
    tracker.add(this);
  }
  
  /**
   * Find possible constant values set to the field.
   * 
   * @return return all possible constant values in a list. If not found, null will be returned.
   */
  public List<String> getConstantValue() {
    List<String> value = null;
    if (valueRef instanceof Constant) {
      value = new ArrayList<>();
      value.add(valueRef.toString());
    } else {
      // TODO use constant propagation for further check.
    }
    return value;
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
  
  public Value getValueRef() {
    return valueRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Field Value Setting -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Set Value: ");
    List<String> value = getConstantValue();
    if (value != null) {
      sb.append(value.toString());
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Possible static field setting: ");
    if (isStaticFieldSetting()) {
      sb.append("Yes");
    } else {
      sb.append("No");
    }
    sb.append("\n---------------------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isFieldValueSetting(Stmt stmt) {
    return StaticUtils.isIt(stmt, CLASS, SIG);
  }
}

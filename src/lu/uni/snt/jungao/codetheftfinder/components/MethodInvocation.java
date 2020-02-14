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
public class MethodInvocation extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.reflect.Method";
  private static final String SIG = "java.lang.Object invoke(java.lang.Object,java.lang.Object[])";
  
//  private Value methodRef, returnValRef;
  private Value classRef, argsRef;
  
  /**
   * @param stmt
   * @param container
   */
  public MethodInvocation(Stmt stmt, SootMethod container) {
    super(stmt, container);
    classRef = getInvokeExprArg(0);
    argsRef = getInvokeExprArg(1);
    tracker.add(this);
  }
  
  public Value getClassRef() {
    return classRef;
  }
  
  public Value getArgsRef() {
    return argsRef;
  }
  
  /**
   * Return whether it is a static method call. This is decided by whether the first argument is null or not.
   * Note: this cannot make sure that the method is a static method, since the developer could used it wrong.
   * 
   * @return
   */
  public Boolean isStaticInvocation() {
    return classRef instanceof NullConstant;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Method Invocation ------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Possible static invocation: ");
    if (isStaticInvocation()) {
      sb.append("Yes");
    } else {
      sb.append("No");
    }
    sb.append("\n-----------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isMethodInvocation(Stmt stmt) {
    return StaticUtils.isIt(stmt, CLASS, SIG);
  }
}

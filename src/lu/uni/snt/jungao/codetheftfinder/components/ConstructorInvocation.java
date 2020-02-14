/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;

/**
 * @author jun.gao
 *
 */
public class ConstructorInvocation extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.reflect.Constructor";
  private static final String SIG = "java.lang.Object newInstance(java.lang.Object[])";
  
  private Value argsRef;

  /**
   * @param stmt
   * @param container
   */
  public ConstructorInvocation(Stmt stmt, SootMethod container) {
    super(stmt, container);
    argsRef = getInvokeExprArg(0); 
    tracker.add(this);
  }

  public Value getArgsRef() {
    return argsRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Constructor Invocation ------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n-----------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isConstructorInvocation(Stmt stmt) {
    return StaticUtils.isIt(stmt, CLASS, SIG);
  }
}

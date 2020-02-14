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
public class LoadedClass extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();
  
  private static final String CLASS = "java.lang.ClassLoader";
  private static final String SIG = "java.lang.Class loadClass(java.lang.String)";
  
  private Value classNameRef;
  
  public LoadedClass(Stmt stmt, SootMethod container) {
    super(stmt, container);
    classNameRef = getInvokeExprArg(0);
    tracker.add(this);
  }
  
  /**
   * Get all the possible application names for which the contexts are created by this creator.
   * 
   * @return
   */
   public List<String> getConstantClassName() {
     List<String> names = null;
     Value theArg = stmt.getInvokeExpr().getArg(0);
     if (theArg instanceof Constant){
       names = new ArrayList<>();
       names.add(theArg.toString().replace("\"", ""));
     } else {
       //TODO use constant propagation to further check
     }
     return names;
   }
   
  public Value getClassNameRef() {
    return classNameRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Class -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Class name: ");
    List<String> name = getConstantClassName();
    if (name != null) {
      sb.append(name.toString());
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Class references: ");
    sb.append(theRefs.toString());
//    showPointsToSet(sb, loaderRef, "Loader");
//    showPointsToSet(sb, classRef, "Class");
    sb.append("\n-----------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isLoadingClass(Stmt stmt) {
    return StaticUtils.isIt(stmt, CLASS, SIG);
  }
}

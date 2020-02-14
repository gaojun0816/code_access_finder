/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.jimple.Stmt;

import lu.uni.snt.jungao.codetheftfinder.utils.StaticUtils;

/**
 * @author jun.gao
 *
 */
public class LoadedClassLoader extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();

  private static final String CLASS = "android.content.Context";
  private static final String SIG = "java.lang.ClassLoader getClassLoader()";
  
  public LoadedClassLoader(Stmt stmt, SootMethod container) {
    super(stmt, container);
    tracker.add(this);
  }
  
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Class Loader -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Loader References: ");
    sb.append(theRefs.toString());
//    showPointsToSet(sb, creatorRef, "Creator");
//    showPointsToSet(sb, loaderRef, "Looader");
    sb.append("\n------------------------------------------------------------\n");
    return sb.toString();
  }
  
  public static Boolean isLoadingClassLoader(Stmt stmt) {
    return StaticUtils.isIt(stmt, CLASS, SIG);
  }
}

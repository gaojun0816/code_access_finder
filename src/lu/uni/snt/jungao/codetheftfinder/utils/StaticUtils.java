/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.utils;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootClass;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

/**
 * This class provide only static utility functions which could be used in the analysis.
 * 
 * @author jun.gao
 */
public class StaticUtils {
  
  /**
   * Check if a class is the descendant of certain class.
   * Note: the class itself will be consider as its descendant as well.
   * 
   * @param theClass the class to check.
   * @param superClassName the string of the full name of the superclass.
   * @return if it is the descendant.
   */
  public static Boolean isDescendant(SootClass theClass, String superClassName) {
    Boolean itis = false;
    SootClass sc = theClass;
    while (sc != null) {
      if (sc.getName().equals(superClassName)) {
        itis = true;
        break;
      }
      sc = sc.hasSuperclass() ? sc.getSuperclass() : null;
    }
    return itis;
  }

  /**
   * Check if the statement invokes the createPackageContext method to get the context of another application.
   * Check if the statement invokes the method specified by the class and the method signature.
   * 
   * @param stmt the Jimple statement to be judged.
   * @param cls the full class name of the method belonging to.
   * @param signature the signature of the method. e.g., android.content.Context createPackageContext(java.lang.String,int)
   * @return Boolean indicate if it contains the invocation
   */
  public static Boolean isIt(Stmt stmt, String cls, String signature) {
    Boolean itis = true;
    if (stmt.containsInvokeExpr()) {
      InvokeExpr ie = stmt.getInvokeExpr();
      if (!StaticUtils.isDescendant(ie.getMethod().getDeclaringClass(), cls)) {
        itis = false;
      } else {
        if (!ie.getMethod().getSubSignature().equals(signature)) {
          itis = false;
        }
      }
    } else {
      itis = false;
    }
    return itis;
  }
  
  /**
   * Check whether 2 local variables  possibly point to a same object by utilizing the points-to analysis of Soot.
   * 
   * @param var1 1st of the 2 local variables to be check.
   * @param var2 2nd of the 2 local variables to be check.
   * @return true if they possibly points to a same object. Otherwise false.
   */
  public static Boolean isIntersected(Local var1, Local var2) {
    Boolean itis = false;
    PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
    PointsToSet ps1 = pta.reachingObjects(var1);
    PointsToSet ps2 = pta.reachingObjects(var2);

    if (ps1.hasNonEmptyIntersection(ps2)) {
      itis = true;
    }
    return itis;
  }
}

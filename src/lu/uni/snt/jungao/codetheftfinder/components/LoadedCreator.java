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
 * The class stands for the invocation of the "createPackageContext" method.
 * This method is the first step to obtain the access to the code of other apps.
 * 
 * @author jun.gao
 *
 */
public class LoadedCreator extends LoadedComponent {
  public static List<LoadedComponent> tracker = new ArrayList<>();

  private final static String CLASS = "android.content.Context";
  private final static String SIG = "android.content.Context createPackageContext(java.lang.String,int)";
  private final static String FLAG = "3";  // the value of "Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY"
  
  private Value appNameRef, flagRef;
  
  public LoadedCreator(Stmt stmt, SootMethod container) {
    super(stmt, container);
    appNameRef = getInvokeExprArg(0);
    flagRef = getInvokeExprArg(1);
    tracker.add(this);
  }
  
 /**
  * Obtain all the possible application names for which the contexts are created by this creator.
  * 
  * @return
  */
  public List<String> getConstantAppName() {
    List<String> name = null;
    if (appNameRef instanceof Constant){
      name = new ArrayList<>();
      name.add(appNameRef.toString().replace("\"", ""));
    } else {
      //TODO use constant propagation to further check
    }
    return name;
  }
  
 /**
  * Obtain all the possible flag values which are used to obtain the context of the other applications.
  * 
  * @return
  */
  public List<String> getConstantFlag() {
    List<String> flag = null;
    if (flagRef instanceof Constant) {
      flag = new ArrayList<>();
      flag.add(flagRef.toString());
    } else {
      //TODO use constant propagation to further check
    }
    return flag;
  }
 
 /**
  * Check are there any flags set to "Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY".
  * Since only this flag setting provide the possibility for accessing the application code.
  *
  * @return true if there is at least one of the possibly flag set to
  *         "Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY".
  */
  public Boolean isFlagRight() {
    Boolean itis = false;
    List<String> flag = getConstantFlag();
    if (flag != null) {
      for (String v : flag) {
        if (v.equals(FLAG)) {
          itis = true;
          break;
        }
      }
    }
    return itis;
  }
  
  public Value getAppNameRef() {
    return appNameRef;
  }
  
  public Value getFlagRef() {
    return flagRef;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("----------------------- Creator -----------------------\n");
    sb.append(container.getDeclaringClass().getName());
    sb.append(" ");
    sb.append(container.getSubSignature());
    sb.append("\n\t");
    sb.append("Statement: ");
    sb.append(stmt.toString());
    sb.append("\n\t");
    sb.append("Loaded app names: ");
    List<String> name = getConstantAppName();
    if (name != null) {
      sb.append(name.toString());
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Flag values: ");
    List<String> flag = getConstantFlag();
    if (flag != null) {
      sb.append(flag.toString());
    } else {
      sb.append("unknown");
    }
    sb.append("\n\t");
    sb.append("Creator references: ");
    sb.append(theRefs.toString());
//    showPointsToSet(sb, creatorRefs, "Creator");
    sb.append("\n------------------------------------------------------------\n");
    return sb.toString();
  }
  
  /**
   * Check if the statement invokes the createPackageContext method to get the context of another application.
   * It will check the method signature and the declaring class of the method. 
   * Before new a Creator object, always use this method to make sure that it can be done.
   * 
   * @param stmt the Jimple statement to be judged.
   * @return Boolean indicate if it contains the invocation
   */
  public static Boolean isLoadingCreator(Stmt stmt) {
    return StaticUtils.isIt(stmt, CLASS, SIG);
  }
}

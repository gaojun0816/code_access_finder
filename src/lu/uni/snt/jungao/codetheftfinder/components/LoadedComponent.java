/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.Local;
import soot.PointsToAnalysis;
import soot.PointsToSet;
import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

/**
 * This is the base class of all other components.
 * 
 * @author jun.gao
 *
 */
public class LoadedComponent {
  protected Stmt stmt; 
  protected SootMethod container;
  protected Set<Value> theRefs;
  protected Value invokerRef;
  protected List<LoadedComponent> predecessorComponents, successorComponents;
  
  public LoadedComponent(Stmt stmt, SootMethod container) {
    this.stmt = stmt;
    this.container = container;
    theRefs = new HashSet<>();
    theRefs.add(getAssignee());
    invokerRef = getInvoker();
  }
  
  /**
   * Common getter of the statement.
   * 
   * @return
   */
  public Stmt getStmt() {
    return stmt;
  }
  
  /**
   * Common getter of the container which is the SootMethod instance of the statement.
   * 
   * @return
   */
  public SootMethod getContainer() {
    return container;
  }
  
  /**
   * Return a set of the references refer to current component.
   * 
   * @return
   */
  public Set<Value> getTheRefs() {
    return theRefs;
  }
  
  /**
   * Return the reference of the invoker.
   * 
   * @return
   */
  public Value getInvokerRef() {
    return invokerRef;
  }
  
  public List<LoadedComponent> getPredecessorComponents() {
    return predecessorComponents;
  }
  
  public List<LoadedComponent> getSuccessorComponents() {
    return successorComponents;
  }

  /**
   * Add a new reference of current component to the reference set.
   * 
   * @param ref
   */
  public void addTheRef(Value ref) {
    theRefs.add(ref);
  }
  
  /**
   * Add a component as the predecessor of current component.
   * 
   * @param predecessor
   */
  public void addPredecessorComponent(LoadedComponent predecessor) {
    if (predecessorComponents == null) {
      predecessorComponents = new ArrayList<>();
    }
    predecessorComponents.add(predecessor);
  }
  
  /**
   * Add a component as the successor of current component.
   * 
   * @param predecessor
   */
  public void addSuccessorComponent(LoadedComponent successor) {
    if (successorComponents == null) {
      successorComponents = new ArrayList<>();
    }
    successorComponents.add(successor);
  }
  
  /**
   * If the statement is an assignment statement, return the assignee (i.e., the Soot local variable received the assigned
   * value).
   * 
   * @return the Soot local variable which receives the assigned value. If not assignment statement, null will be returned.
   */
  public Value getAssignee() {
    Value ref = null;
    if (stmt instanceof AssignStmt) {
      AssignStmt astmt = (AssignStmt) stmt;
      ref = astmt.getLeftOp();
    }
    return ref;
  }
  
  /**
   * If the statement contains invocation expression, return the wanted argument of the invoked method.
   * 
   * @param index the index of the argument.
   * @return a Soot Value instance of the argument. If the statement do not contain invocation expression,
   *          null will be returned.
   */
  protected Value getInvokeExprArg(int index) {
    Value arg = null;
    if (stmt.containsInvokeExpr()) {
      arg = stmt.getInvokeExpr().getArg(index);
    }
    return arg;
  }
  
  /**
   * If the statement contains instance invocation expression, return the Soot local variable referring to the instance.
   * 
   * @return the Soot local variable instance referring to the invoker instance. If the statement does not contain a such
   *          invocation, null will be returned.
   */
  protected Value getInvoker() {
    Value invoker = null;
    if (stmt.containsInvokeExpr()) {
      InvokeExpr ie = stmt.getInvokeExpr();
      if (ie instanceof InstanceInvokeExpr) {
        invoker = ((InstanceInvokeExpr) ie).getBase();
      }
    }
    return invoker;
  }
  
  /**
   * Used to show the type of points to set of a certain local variable for debugging purpose.
   * @param sb
   * @param local
   * @param name
   */
  protected void showPointsToSet(StringBuilder sb, Local local, String name) {
    PointsToAnalysis pta = Scene.v().getPointsToAnalysis();
    PointsToSet pts =  pta.reachingObjects(local);
    sb.append("\n\t");
    sb.append(name);
    sb.append(" reference points-to set type: ");
    sb.append(pts.getClass());
  }
}

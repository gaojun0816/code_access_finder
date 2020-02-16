/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.construct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.queue.QueueReader;

import lu.uni.snt.jungao.codetheftfinder.components.LoadedConstructor;
import lu.uni.snt.jungao.codetheftfinder.components.ConstructorInvocation;
import lu.uni.snt.jungao.codetheftfinder.components.FieldOrMethodAccessibleSetting;
import lu.uni.snt.jungao.codetheftfinder.components.FieldValueSetting;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedClass;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedClassLoader;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedCreator;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedField;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedMethod;
import lu.uni.snt.jungao.codetheftfinder.components.MethodInvocation;
import lu.uni.snt.jungao.codetheftfinder.utils.sootarray.SootArray;
import lu.uni.snt.jungao.codetheftfinder.utils.sootarray.SootArrayElement;

/**
 * @author jun.gao
 *
 */
public class ComponentsBuilder {
  private List<LoadedCreator> curCreators;
  private List<LoadedClassLoader> curLoaders;
  private List<LoadedClass> curClasses;
  private List<LoadedMethod> curMethods;
  private List<LoadedField> curFields;

  /**
   * 
   */
  public ComponentsBuilder() {
    // TODO Auto-generated constructor stub
  }
  
  public void build() {
    ReachableMethods reachableMethods = Scene.v().getReachableMethods();
    QueueReader<MethodOrMethodContext> listener = reachableMethods.listener();
    while (listener.hasNext()) {
      SootMethod m = listener.next().method();
      if (prepareActiveBody(m)) {
        Body body = m.getActiveBody();
        curCreators = new ArrayList<>();
        curLoaders = new ArrayList<>();
        curClasses = new ArrayList<>();
        curMethods = new ArrayList<>();
        curFields = new ArrayList<>();
        for (Unit unit : body.getUnits()) {
          Stmt stmt = (Stmt) unit;
          stmtAnalyze(stmt, m);
        }
      }
    } 
  }
  
  private void stmtAnalyze(Stmt stmt, SootMethod container) {
    if (LoadedCreator.isLoadingCreator(stmt)) {
      LoadedCreator lc = new LoadedCreator(stmt, container);
      curCreators.add(lc);
      Value creator = lc.getAssignee();
      if (creator instanceof Local) {
        localPropagate(creator, container, stmt, lc.getTheRefs());
      } else if (creator instanceof FieldRef) {
        fieldPropagate((FieldRef) creator, container.getDeclaringClass(), lc.getTheRefs());
      }
      if (creator != null) {
        passArgumentPropagate(creator, container, stmt, lc.getTheRefs(), null);
        returnValuePropagate(creator, container, lc.getTheRefs());
      }
    } else if (LoadedClassLoader.isLoadingClassLoader(stmt)) {
      LoadedClassLoader cl = new LoadedClassLoader(stmt, container);
      curLoaders.add(cl);
      Value loader = cl.getAssignee();
      if (loader instanceof Local) {
        localPropagate(loader, container, stmt, cl.getTheRefs());
      } else if (loader instanceof FieldRef) {
        fieldPropagate((FieldRef) loader, container.getDeclaringClass(), cl.getTheRefs());
      }
      if (loader != null) {
        passArgumentPropagate(loader, container, stmt, cl.getTheRefs(), null);
        returnValuePropagate(loader, container, cl.getTheRefs());
      }
    } else if (LoadedClass.isLoadingClass(stmt)) {
      LoadedClass c = new LoadedClass(stmt, container);
      curClasses.add(c);
      Value cls = c.getAssignee();
      if (cls instanceof Local) {
        localPropagate(cls, container, stmt, c.getTheRefs());
      } else if (cls instanceof FieldRef) {
        fieldPropagate((FieldRef) cls, container.getDeclaringClass(), c.getTheRefs());
      }
      if (cls != null) {
        passArgumentPropagate(cls, container, stmt, c.getTheRefs(), null);
        returnValuePropagate(cls, container, c.getTheRefs());
      }
    } else if (LoadedMethod.isLoadingMethod(stmt)) {
      LoadedMethod lm = new LoadedMethod(stmt, container);
      curMethods.add(lm);
      Value method = lm.getAssignee();
      if (method instanceof Local) {
        localPropagate(method, container, stmt, lm.getTheRefs());
      } else if (method instanceof FieldRef) {
        fieldPropagate((FieldRef) method, container.getDeclaringClass(), lm.getTheRefs());
      }
      if (method != null) {
        passArgumentPropagate(method, container, stmt, lm.getTheRefs(), null);
        returnValuePropagate(method, container, lm.getTheRefs());
      }
    } else if (LoadedField.isLoadingField(stmt)) {
      LoadedField lf = new LoadedField(stmt, container);
      curFields.add(lf);
      Value field = lf.getAssignee();
      if (field instanceof Local) {
        localPropagate(field, container, stmt, lf.getTheRefs());
      } else if (field instanceof FieldRef) {
        fieldPropagate((FieldRef) field, container.getDeclaringClass(), lf.getTheRefs());
      }
      if (field != null) {
        passArgumentPropagate(field, container, stmt, lf.getTheRefs(), null);
        returnValuePropagate(field, container, lf.getTheRefs());
      }
    } else if (FieldOrMethodAccessibleSetting.isFieldOrMethodAccessibilitySetting(stmt)) {
      new FieldOrMethodAccessibleSetting(stmt, container);
    } else if (MethodInvocation.isMethodInvocation(stmt)) {
      new MethodInvocation(stmt, container);
    } else if (ConstructorInvocation.isConstructorInvocation(stmt)) {
      new ConstructorInvocation(stmt, container);
    } else if (LoadedConstructor.isLoadingConstructor(stmt)) {
      new LoadedConstructor(stmt, container);
    } else if (FieldValueSetting.isFieldValueSetting(stmt)) {
      new FieldValueSetting(stmt, container);
    } else if (SootArray.isNewArrayStmt(stmt) || SootArrayElement.isSootArrayElementAssignment(stmt)) {
      SootArray.genSootArray(stmt, container);
    } 
  }
  
  /**
   * Propagate the local variable within the method.
   * 
   * @param v the local variable need to be propagated.
   * @param method the method of the local variable.
   * @param start specify the analysis starts from which statement.
   * @param collector the set to collect found aliasing local variables.
   */
  private void localPropagate(Value v, SootMethod method, Stmt start, Set<Value> collector) {
    if (prepareActiveBody(method)) {
      Body body = method.getActiveBody();
      Iterator<Unit> it = body.getUnits().iterator((Unit) start);
      while (it.hasNext()) {
        Stmt next = (Stmt) it.next();
        if (next instanceof AssignStmt) {
          AssignStmt astmt = (AssignStmt) next;
          Value assigner = astmt.getRightOp();
          if (assigner == v) {
            // to avoid the dead loop caused by mutual assignment
            // (i.e., $r1 = $r2; $r2 = $r1;)
            Value assignee = astmt.getLeftOp();
            if (collector.contains(assignee)) continue;
            collector.add(assignee);
            if (assignee instanceof Local) {
              localPropagate(assignee, method, next, collector);
            } else if (assignee instanceof FieldRef) {
              fieldPropagate((FieldRef) assignee, method.getDeclaringClass(), collector);
            } 
            passArgumentPropagate(assignee, method, next, collector, null);
            returnValuePropagate(assignee, method, collector);
          }
        }
      }
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("[localPropagate] ");
      sb.append(method.getSignature());
      sb.append(": has no active body.");
      System.err.println(sb.toString());
    }
  }
  
  /**
   * Check if a Soot value is passed to any other methods as an argument of the method.
   * 
   * @param v the value to be checked.
   * @param method the method where the value is found.
   * @param start specify the analysis starts form which statement.
   * @param collector the set to collect found aliasing local variables.
   * @param checkedMethods is a set of Soot methods which has been checked. return value propagation should be checked no
   *        more than once for each method to avoid dead loop caused by function self-calling (i.e., recursion) or calling
   *        loops occupy multiple methods (e.g., A -> B -> ... -> A).
   */
  private void passArgumentPropagate(Value v, SootMethod method, Stmt start, Set<Value> collector, Set<SootMethod> checkedMethods) {
    // store checked methods to avoid loop to dead.
    if (checkedMethods == null) {
      checkedMethods = new HashSet<>();
      checkedMethods.add(method);
    }
    if (prepareActiveBody(method)) {
      Body body = method.getActiveBody();
      Iterator<Unit> it = body.getUnits().iterator((Unit) start);;
      while (it.hasNext()) {
        Stmt next = (Stmt) it.next();
        if (next.containsInvokeExpr()) {
          List<Value> args = next.getInvokeExpr().getArgs();
          if (args.contains(v)) {
            int i = args.indexOf(v);
            SootMethod userMethod = next.getInvokeExpr().getMethod();
            // skip checked methods.
            if (checkedMethods.contains(userMethod)) continue;
            checkedMethods.add(userMethod);
            IdentityStmt idstmt  = getParameter2LocalStmt(userMethod, i);
            if (idstmt != null) {
              Value l = idstmt.getLeftOp();
              collector.add(l);
              localPropagate(l, userMethod, idstmt, collector);
              passArgumentPropagate(l, userMethod, idstmt, collector, checkedMethods);
              returnValuePropagate(l, userMethod, collector);
            }
          }
        }
      }
    }
  }
  
  /**
   * Find the identity statement where certain parameter is passed to a local variable.
   * 
   * @param method
   * @param parameterIndex
   * @return
   */
  private IdentityStmt getParameter2LocalStmt(SootMethod method, int parameterIndex) {
    IdentityStmt theIdStmt = null;
    if (prepareActiveBody(method)) {
      Body body = method.getActiveBody();
      for (Unit u : body.getUnits()) {
        Stmt stmt = (Stmt) u;
        if (stmt instanceof IdentityStmt) {
          IdentityStmt idStmt = (IdentityStmt) stmt;
          Value assigner = idStmt.getRightOp();
          if (assigner instanceof ParameterRef) {
            ParameterRef pr = (ParameterRef) assigner;
            if (pr.getIndex() == parameterIndex) {
              theIdStmt = idStmt;
              break;
            }
          }
        }
      }
    }
    return theIdStmt;
  }

  private void returnValuePropagate(Value v, SootMethod callee, Set<Value> collector) {
    returnValuePropagate(v, callee, collector, null);
  }
  
  /**
   * Check if a Soot value had been returned. If yes, propagate the value within invoker methods. 
   * Within the invoker, start from the invocation statement, find all the aliases
   * of the return value and collect them into the "refs" set.
   * 
   * @param v the value to check.
   * @param callee the Soot method returning relevant values.
   * @param collector the set to collect aliasing local variables. This is used to collect the results
   * @param checkedMethods is a set of Soot methods which has been checked. return value propagation should be checked no
   *        more than once for each method to avoid dead loop caused by function self-calling (i.e., recursion) or calling
   *        loops occupy multiple methods (e.g., A -> B -> ... -> A).
   */
  private void returnValuePropagate(Value v, SootMethod callee, Set<Value> collector, Set<SootMethod> checkedMethods) {
    // First check if the value "v" is returned.
    if (!callee.getReturnType().toString().equals(v.getType().toString())) {
      return;
    } else if (!getReturnValues(callee).contains(v)) {
      return;
    }
    // setup set to store checked methods
    if (checkedMethods == null) {
      checkedMethods = new HashSet<>();
      // skip self-invocation (i.e., recursion) which will lead to deadlock and stack overflow.
      checkedMethods.add(callee);
    }
    // Propagate the returned value.
    CallGraph cg = Scene.v().getCallGraph();
    Iterator<MethodOrMethodContext> it = new Targets(cg.edgesInto(callee));
    while (it.hasNext()) {
      SootMethod caller = (SootMethod) it.next();
      // skip callee and checked callers.
      if (checkedMethods.contains(caller)) continue;
      checkedMethods.add(caller);
      if (prepareActiveBody(caller)) {
        for (Unit u : caller.getActiveBody().getUnits()) {
          Stmt stmt = (Stmt) u;
          if (stmt.containsInvokeExpr()) {
            if (stmt instanceof AssignStmt) {
              if (stmt.getInvokeExpr().getMethod().getSignature().equals(callee.getSignature())) {
                Value assignee = ((AssignStmt) stmt).getLeftOp();
                collector.add(assignee);
                if (assignee instanceof Local) {
                  localPropagate(assignee, caller, stmt, collector);
                } else if (assignee instanceof FieldRef) {
                  fieldPropagate((FieldRef) assignee, caller.getDeclaringClass(), collector);
                }
                passArgumentPropagate(assignee, caller, stmt, collector, null);
                returnValuePropagate(assignee, caller, collector, checkedMethods);
              }
            }
          }
        }
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append(callee.getSignature());
        sb.append("[returnValuePropagate] ");
        sb.append(": has no active body.");
        System.err.println(sb.toString());
      }
    }
  }
   
  /**
   * Get all the possible return values of a method.
   * 
   * @param method
   * @return
   */
  private Set<Value> getReturnValues(SootMethod method) {
    Set<Value> rvs = new HashSet<>();
    if (prepareActiveBody(method)) {
      for (Unit u : method.getActiveBody().getUnits()) {
        Stmt stmt = (Stmt) u;
        if (stmt instanceof ReturnStmt) {
          rvs.add(((ReturnStmt) stmt).getOp());
        }
      }
    }
    return rvs;
  }

  /**
   * Propagate values assigned to a class field with the class.
   * It will check the whole class of the field for assignment statement which aliasing the field (e.g. $r2 = <class field>).
   * All such aliases will be collected into the "refs" set.
   * 
   * @param field the Soot class field need to be propagate.
   * @param cls the Soot class to which the field belongs.
   * @param collector the set to collect aliasing local variables. This is used to collect the results
   */
  //TODO static field propagate between classes.
  private void fieldPropagate(FieldRef field, SootClass cls, Set<Value> collector) {
    for (SootMethod m : cls.getMethods()) {
      if (prepareActiveBody(m)) {
        Body body = m.getActiveBody();
        for (Unit u : body.getUnits()) {
          Stmt stmt = (Stmt) u;
          if (stmt instanceof AssignStmt) {
            AssignStmt astmt = (AssignStmt) stmt;
            Value assigner = astmt.getRightOp();
            // Since the Soot objects of a field normally are different (possibly because they are generated per method),
            // the complete name of a field is used here to check the equality of 2 field objects.
            if (assigner.toString().equals(field.toString())) {
               Value assignee = astmt.getLeftOp();
               // to avoid the dead loop caused by mutual assignment
               // (i.e., $r1 = <class.field: Type f>; <class.field: Type f> = $r1);
               if (collector.contains(assignee)) continue;
               collector.add(assignee);
               if (assignee instanceof Local) {
                 localPropagate(assignee, m, stmt, collector);
               } else if (assignee instanceof FieldRef) {
                 fieldPropagate((FieldRef) assignee, m.getDeclaringClass(), collector);
               }
               passArgumentPropagate(assignee, m, stmt, collector, null);
               returnValuePropagate(assignee, m, collector);
            } 
          }
        }
      } else {
        StringBuilder sb = new StringBuilder();
        sb.append("[fieldPropagate] ");
        sb.append(m.getSignature());
        sb.append(": has no active body.");
        System.err.println(sb.toString());
      }
    }
  }
  
  private boolean prepareActiveBody(SootMethod method) {
    boolean ready = false;
    if (method.hasActiveBody()) {
      ready = true;
    } else if (method.isConcrete()) {
      try {
        method.retrieveActiveBody();
        ready = true;
      } catch (RuntimeException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ComponentsBuilder::prepareActiveBody] ");
        sb.append(e);
        System.err.println(sb);
      }
    }
    return ready;
  }
}
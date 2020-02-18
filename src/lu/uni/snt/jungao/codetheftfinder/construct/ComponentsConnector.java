/**
 * 
 */
package lu.uni.snt.jungao.codetheftfinder.construct;

import java.util.List;
import java.util.Set;

import soot.Value;

import lu.uni.snt.jungao.codetheftfinder.components.LoadedConstructor;
import lu.uni.snt.jungao.codetheftfinder.components.ConstructorInvocation;
import lu.uni.snt.jungao.codetheftfinder.components.FieldOrMethodAccessibleSetting;
import lu.uni.snt.jungao.codetheftfinder.components.FieldValueSetting;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedClass;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedClassLoader;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedComponent;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedCreator;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedField;
import lu.uni.snt.jungao.codetheftfinder.components.LoadedMethod;
import lu.uni.snt.jungao.codetheftfinder.components.MethodInvocation;

/**
 * @author jun.gao
 *
 */
public class ComponentsConnector {

  /**
   * 
   */
  public ComponentsConnector() {
    // TODO Auto-generated constructor stub
  }

  public void connect() {
    connectLayer(LoadedCreator.tracker, LoadedClassLoader.tracker);
    connectLayer(LoadedClassLoader.tracker, LoadedClass.tracker);
    connectLayer(LoadedClass.tracker, LoadedConstructor.tracker);
    connectLayer(LoadedClass.tracker, LoadedMethod.tracker);
    connectLayer(LoadedClass.tracker, LoadedField.tracker);
    connectLayer(LoadedClass.tracker, ConstructorInvocation.tracker);
    connectLayer(LoadedMethod.tracker, FieldOrMethodAccessibleSetting.tracker);
    connectLayer(LoadedMethod.tracker, MethodInvocation.tracker);
    connectLayer(LoadedField.tracker, FieldOrMethodAccessibleSetting.tracker);
    connectLayer(LoadedField.tracker, FieldValueSetting.tracker);
    connectLayer(LoadedConstructor.tracker, ConstructorInvocation.tracker);
  }
  
  private void connectLayer(List<LoadedComponent> predecessors, List<LoadedComponent> currents) {
    for (LoadedComponent p : predecessors) {
      Set<Value> preRefs = p.getTheRefs();
      for (LoadedComponent c : currents) {
        if (preRefs.contains(c.getInvokerRef())) {
          p.addSuccessorComponent(c);
          c.addPredecessorComponent(p);
        }
      }
    }
  }
}

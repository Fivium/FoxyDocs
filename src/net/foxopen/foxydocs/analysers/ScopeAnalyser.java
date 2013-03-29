/*
Copyright (c) 2013, Fivium Ltd.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, 
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, 
      this list of conditions and the following disclaimer in the documentation 
      and/or other materials provided with the distribution.
    * Neither the name of Fivium Ltd nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

$Revision$

*/
package net.foxopen.foxydocs.analysers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.foxopen.foxydocs.model.DocumentedElement;
import net.foxopen.foxydocs.model.FoxModule;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class ScopeAnalyser {

  private static Graph<FoxModule, DefaultEdge> completeGraph;
  private static HashMap<String, FoxModule> moduleMap;
  private static HashSet<String> moduleNotFound;

  public static void computeGraph(Collection<FoxModule> modules) {
    // Create the graph object; it is null at this point
    completeGraph = new SimpleGraph<FoxModule, DefaultEdge>(DefaultEdge.class);
    moduleMap = new HashMap<>();
    moduleNotFound = new HashSet<>();

    for (FoxModule module : modules) {
      addModule(module);
    }

    for (FoxModule module : modules) {
      for (String libraryName : module.getLibraries()) {
        FoxModule target = moduleMap.get(libraryName);
        if (target != null) {
          completeGraph.addEdge(module, target);
        } else {
          moduleNotFound.add(libraryName);
        }
      }
    }

    System.err.println("Modules not found: " + moduleNotFound);
  }

  public static void addModule(FoxModule module) {
    moduleMap.put(module.getName(), module);
    completeGraph.addVertex(module);
  }

  public static List<FoxModule> getInputLinks(String name) {
    return getInputLinks(moduleMap.get(name));
  }

  public static List<FoxModule> getInputLinks(FoxModule moduleTarget) {
    ArrayList<FoxModule> buffer = new ArrayList<>();
    for (DefaultEdge e : completeGraph.edgesOf(moduleTarget)) {
      FoxModule module = completeGraph.getEdgeTarget(e);
      if (module.compareTo(moduleTarget) == 0) {
        buffer.add(completeGraph.getEdgeSource(e));
      }
    }

    return buffer;
  }

  public static List<DocumentedElement> getActionCallCandidates(String actionName, DocumentedElement context) {
    List<DocumentedElement> actionSet = new ArrayList<>();

    // Search in the scope
    Collection<FoxModule> scope = getScope(context.getFoxModule());
    for (FoxModule mod : scope) {
      DocumentedElement candidate = mod.getGlobalAction(actionName);
      if (candidate != null && candidate.getName().equals(actionName)) {
        actionSet.add(candidate);
      }
    }

    // Search in the state
    // TODO

    return actionSet;
  }

  public static List<DocumentedElement> getHierarchyCall(DocumentedElement action) {
    // TODO
    return null;
  }

  public static Collection<FoxModule> getScope(FoxModule module) {
    Collection<FoxModule> scope = new HashSet<>();

    // Current module in the scope, obviously
    scope.add(module);

    // Add libraries
    scope.addAll(getExtendedLibraries(module));

    // Modules using this library in the scope too
    scope.addAll(getInputLinks(module));

    return scope;
  }

  public static Collection<FoxModule> getExtendedLibraries(FoxModule module) {
    Collection<FoxModule> libraries = new HashSet<>();

    // Libraries in the scope
    for (String libraryName : module.getLibraries()) {
      FoxModule lib = moduleMap.get(libraryName);
      if (lib == null)
        continue;
      libraries.add(lib);
      libraries.addAll(getScope(lib));
    }

    return libraries;
  }

  public static class CheckReference implements IRunnableWithProgress {
    @Override
    public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
      if (moduleMap != null && !moduleMap.isEmpty())
        checkReferences(moduleMap.values(), monitor);
    }
  }

  public static void checkReferences(Collection<FoxModule> modules, IProgressMonitor monitor) {
    // Check references
    monitor.beginTask("Check call references", modules.size());
    monitor.subTask("Check " + modules.size() + " FoxModules");
    StringBuffer missing = new StringBuffer();
    for (FoxModule f : modules) {
      missing.append(checkReferences(f));
      monitor.worked(1);
    }
    System.err.println(missing.toString());
  }

  public static String checkReferences(FoxModule module) {
    StringBuffer output = new StringBuffer();
    for (DocumentedElement action : module.getActions()) {
      for (String actionName : action.getActionCalls()) {
        List<DocumentedElement> candidates = ScopeAnalyser.getActionCallCandidates(actionName, action);
        if (candidates.isEmpty()) {
          output.append("No match found for " + action.getFoxModule() + "->" + action.getName() + "->" + actionName);
          output.append('\n');
        }
      }
    }
    return output.toString();

  }
}

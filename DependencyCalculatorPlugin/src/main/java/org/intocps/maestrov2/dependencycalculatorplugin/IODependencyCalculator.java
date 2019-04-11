package org.intocps.maestrov2.dependencycalculatorplugin;


import org.intocps.maestrov2.data.*;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.traverse.TopologicalOrderIterator;
import scala.collection.JavaConverters;
import scala.collection.immutable.Set;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;


public class IODependencyCalculator {

    public static IODependencyResult calculateIODependencies(Set<Connection> connections) {
        // This is a java plugin, therefore convert the argument form a scala Set to a Java Set
        java.util.Set<Connection> connectionsJ = JavaConverters.setAsJavaSet(connections);

        // Check for algebraic loops
        DirectedGraph<PortV2, LabelledEdge> portV2LabelledEdgeDirectedGraph = GraphBuilder.buildAlgebraicLoopDetectionGraph(connectionsJ);
        CycleCheckResult cycleCheckResult = checkRealAlgebraicCycles(portV2LabelledEdgeDirectedGraph);
        TopologicalOrderIterator<PortV2,LabelledEdge> topologicalOrderIterator = new TopologicalOrderIterator<PortV2, LabelledEdge>(portV2LabelledEdgeDirectedGraph);





        // Get order. If the graph is NOT cyclic then request the complete order.
        //java.util.Set<PortV2> sources = connectionsJ.stream().map(c -> GraphBuilder.convertConSVToPortV2(c.from())).collect(Collectors.toSet());
        //sources = portV2LabelledEdgeDirectedGraph.vertexSet();
        //List<PortV2> orderedPortsJava = findConnectionsInOrder(portV2LabelledEdgeDirectedGraph, sources, null, !cycleCheckResult.cyclic);

        //Stream<ConnectionScalarVariable> orderedConnsJava = orderedPortsJava.stream().map(p -> GraphBuilder.convertPortV2toConSV(p));
        //scala.collection.immutable.List<ConnectionScalarVariable> orderedConnsScala = JavaConverters.asScalaIteratorConverter(orderedConnsJava.iterator()).asScala().toList();

        if (cycleCheckResult.cyclic) {
            return new IODependencyCyclic(cycleCheckResult.cycles);
        } else {
            List<ConnectionScalarVariable> ordered = new java.util.ArrayList<ConnectionScalarVariable>();
            while(topologicalOrderIterator.hasNext()){
                PortV2 next = topologicalOrderIterator.next();
                ordered.add(GraphBuilder.convertPortV2toConSV(next));
            }

            scala.collection.immutable.List<ConnectionScalarVariable> orderedScala = JavaConverters.asScalaBuffer(ordered).toList();
            return new IODependencyAcyclic(orderedScala);

        }
    }

    public static CycleCheckResult checkRealAlgebraicCycles(
            DirectedGraph<PortV2, LabelledEdge> graph) {
        CycleDetector<PortV2, LabelledEdge> cycleDetector = new CycleDetector<>(graph);

        if (cycleDetector.detectCycles()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Cycle detected with connections: ");

            java.util.Set<PortV2> cycle = cycleDetector.findCycles();

            List<PortV2> sorted = findConnectionsInOrder(graph, cycle, null, false);

            assert sorted != null;
            for (Iterator<PortV2> itr = sorted.iterator(); itr.hasNext(); ) {
                sb.append(itr.next());
                if (itr.hasNext()) {
                    sb.append(" -> ");
                } else {
                    sb.append(" -> loop");
                }
            }

            return new CycleCheckResult(true, sb.toString());
        } else {
            return new CycleCheckResult(false, null);
        }
    }

    /**
     * sorts the set into the connection path
     *
     * @param graph
     * @param ports
     * @param current
     * @return
     */
    public static List<PortV2> findConnectionsInOrder(
            DirectedGraph<PortV2, LabelledEdge> graph, java.util.Set<PortV2> ports,
            PortV2 current, boolean completeSortOnly) {
        List<PortV2> sorted = new Vector<>();

        if (current == null) {
            current = ports.iterator().next();
            java.util.Set<PortV2> ports2 = new java.util.HashSet<>(ports);
            ports2.remove(current);
            List<PortV2> connectionsInOrder = findConnectionsInOrder(graph, ports2, current, completeSortOnly);
            // In case this is null, try a new current.
            sorted.addAll(connectionsInOrder);
            return sorted;
        } else {
            sorted.add(current);

            for (PortV2 p : ports) {
                LabelledEdge edge = graph.getEdge(current, p);

                java.util.Set<PortV2> ports2 = new java.util.HashSet<>(ports);
                ports2.remove(p);

                if (edge != null) {
                    if (ports2.isEmpty()) {
                        sorted.add(p);
                        break;
                    } else {
                        List<PortV2> res = findConnectionsInOrder(graph, ports2, p, completeSortOnly);
                        if (res != null) {
                            sorted.addAll(res);
                        }
                    }
                }
            }
        }

        if (!completeSortOnly || sorted.size() == ports.size() + 1)
            return sorted;
        else
            return null;

    }
}

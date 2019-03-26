/*
 * This file is part of the INTO-CPS toolchain.
 *
 * Copyright (c) 2017-CurrentYear, INTO-CPS Association,
 * c/o Professor Peter Gorm Larsen, Department of Engineering
 * Finlandsgade 22, 8200 Aarhus N.
 *
 * All rights reserved.
 *
 * THIS PROGRAM IS PROVIDED UNDER THE TERMS OF GPL VERSION 3 LICENSE OR
 * THIS INTO-CPS ASSOCIATION PUBLIC LICENSE VERSION 1.0.
 * ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS PROGRAM CONSTITUTES
 * RECIPIENT'S ACCEPTANCE OF THE OSMC PUBLIC LICENSE OR THE GPL
 * VERSION 3, ACCORDING TO RECIPIENTS CHOICE.
 *
 * The INTO-CPS toolchain  and the INTO-CPS Association Public License
 * are obtained from the INTO-CPS Association, either from the above address,
 * from the URLs: http://www.into-cps.org, and in the INTO-CPS toolchain distribution.
 * GNU version 3 is obtained from: http://www.gnu.org/copyleft/gpl.html.
 *
 * This program is distributed WITHOUT ANY WARRANTY; without
 * even the implied warranty of  MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE, EXCEPT AS EXPRESSLY SET FORTH IN THE
 * BY RECIPIENT SELECTED SUBSIDIARY LICENSE CONDITIONS OF
 * THE INTO-CPS ASSOCIATION.
 *
 * See the full INTO-CPS Association Public License conditions for more details.
 */

/*
 * Author:
 *		Kenneth Lausdahl
 *		Casper Thule
 */
package org.intocps.maestrov2.plugins.iodependencycalculator;

import org.intocps.maestrov2.scala.modeldescription.*;
import org.intocps.orchestration.coe.modeldefinition.ModelDescription;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import scala.collection.immutable.Set;

import java.util.Map;

/**
 * Created by kel on 25/07/16.
 */
public class GraphBuilder {

    public static ConnectionScalarVariable convertPortV2toConSV(PortV2 p)
    {
        return new ConnectionScalarVariable(p.variableName, new Instance(p.instanceName,p.fmuName));
    }

    public static PortV2 convertConSVToPortV2(ConnectionScalarVariable csv) {
        Instance i = csv.vInstance();
        return new PortV2(i.name(), i.fmu(), csv.vName());
    }

    public static EdgeType convertConnectionTypeToEdgeType(Connection c) {
        if (c.typeOf() == ConnectionType.External()) {
            return EdgeType.ExternalLink;
        } else if (c.typeOf() == ConnectionType.Internal()) {
            return EdgeType.InternalDependency;
        } else {
            throw new IllegalArgumentException("The connection type was illegal.");
        }

    }

    public static DirectedGraph<PortV2, LabelledEdge> buildAlgebraicLoopDetectionGraph(java.util.Set<Connection> connections) {


        DirectedGraph<PortV2, LabelledEdge> graph = new DefaultDirectedGraph<>(LabelledEdge.class);
        for (Connection con : connections) {
            PortV2 fromP = convertConSVToPortV2(con.from());
            graph.addVertex(fromP);

            // Convert from Scala Set to Java Set
            java.util.Set<ConnectionScalarVariable> toJ = scala.collection.JavaConverters.setAsJavaSet(con.to());
            for (ConnectionScalarVariable to : toJ) {

                PortV2 toP = convertConSVToPortV2(to);
                graph.addVertex(toP);
                graph.addEdge(fromP, toP, new LabelledEdge(convertConnectionTypeToEdgeType(con)));
            }

        }

        return graph;
    }
}

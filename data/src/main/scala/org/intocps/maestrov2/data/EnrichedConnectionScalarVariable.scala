package org.intocps.maestrov2.data

import org.intocps.orchestration.coe.modeldefinition.ModelDescription.Causality

case class EnrichedConnectionScalarVariable(vName: String, vInstance: Instance, causality: Causality, valueRef: Long)

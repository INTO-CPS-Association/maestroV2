package org.intocps.maestrov2.data.Control

sealed trait Phase
case class Simulation(data: Option[SimulationData]) extends Phase


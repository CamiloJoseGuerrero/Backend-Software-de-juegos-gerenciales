package com.estratego.application.event;

/**
 * Algo cambió en el juego de una simulación (se activó un caso, se inició o finalizó la simulación)
 * y hay que sembrar o cerrar estados financieros de sus empresas (D2).
 * Se procesa después del commit, en su propia transacción.
 */
public record EstadosFinancierosPendientesEvent(Long idSimulacion) {
}

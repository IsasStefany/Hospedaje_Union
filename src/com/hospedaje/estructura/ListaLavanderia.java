package com.hospedaje.estructura;

import java.util.ArrayList;
import java.util.List;

/**
 * LISTA ENLAZADA SIMPLE para gestionar pedidos de lavanderia.
 *
 * Estructura:
 *   cabeza -> [Nodo1] -> [Nodo2] -> [Nodo3] -> null
 *
 * Operaciones implementadas:
 *   - agregar(nodo)       : inserta al final O(n)
 *   - eliminar(id)        : elimina por id   O(n)
 *   - buscarPorHabitacion : busca por numero  O(n)
 *   - actualizarEstado    : cambia estado     O(n)
 *   - obtenerTodos        : recorre la lista  O(n)
 *   - obtenerPendientes   : filtra pendientes O(n)
 *   - totalIngresos       : suma costos       O(n)
 *   - tamano()            : cantidad de nodos O(n)
 *   - estaVacia()         : si cabeza == null O(1)
 */
public class ListaLavanderia {

    private NodoLavanderia cabeza;   // primer nodo de la lista
    private int            contador; // auto-incremento de IDs

    public ListaLavanderia() {
        this.cabeza   = null;
        this.contador = 0;
    }

    /**
     * Agrega un nuevo pedido al FINAL de la lista.
     */
    public void agregar(NodoLavanderia nuevo) {
        contador++;
        nuevo.setId(contador);

        if (cabeza == null) {
            // Lista vacia: el nuevo nodo es la cabeza
            cabeza = nuevo;
        } else {
            // Recorrer hasta el ultimo nodo
            NodoLavanderia actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevo; // enlazar al final
        }
    }

    /**
     * Elimina un nodo por su ID.
     * Retorna true si se elimino, false si no se encontro.
     */
    public boolean eliminar(int id) {
        if (cabeza == null) return false;

        // Si el nodo a eliminar es la cabeza
        if (cabeza.getId() == id) {
            cabeza = cabeza.siguiente;
            return true;
        }

        // Buscar el nodo anterior al que queremos eliminar
        NodoLavanderia anterior = cabeza;
        while (anterior.siguiente != null) {
            if (anterior.siguiente.getId() == id) {
                anterior.siguiente = anterior.siguiente.siguiente;
                return true;
            }
            anterior = anterior.siguiente;
        }
        return false;
    }

    /**
     * Busca pedidos por numero de habitacion.
     */
    public List<NodoLavanderia> buscarPorHabitacion(String numeroHabitacion) {
        List<NodoLavanderia> resultado = new ArrayList<>();
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            if (actual.getNumeroHabitacion().equals(numeroHabitacion)) {
                resultado.add(actual);
            }
            actual = actual.siguiente;
        }
        return resultado;
    }

    /**
     * Actualiza el estado de un pedido por ID.
     */
    public boolean actualizarEstado(int id, String nuevoEstado) {
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            if (actual.getId() == id) {
                actual.setEstado(nuevoEstado);
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    /**
     * Retorna todos los nodos como lista para mostrar en tabla.
     */
    public List<NodoLavanderia> obtenerTodos() {
        List<NodoLavanderia> lista = new ArrayList<>();
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            lista.add(actual);
            actual = actual.siguiente;
        }
        return lista;
    }

    /**
     * Retorna solo los pedidos PENDIENTES y EN_PROCESO.
     */
    public List<NodoLavanderia> obtenerPendientes() {
        List<NodoLavanderia> lista = new ArrayList<>();
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            if ("PENDIENTE".equals(actual.getEstado())
                    || "EN_PROCESO".equals(actual.getEstado())) {
                lista.add(actual);
            }
            actual = actual.siguiente;
        }
        return lista;
    }

    /**
     * Suma el total de ingresos de todos los pedidos.
     */
    public double totalIngresos() {
        double total = 0;
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            total += actual.getCosto();
            actual = actual.siguiente;
        }
        return total;
    }

    /**
     * Cuenta el numero de nodos en la lista.
     */
    public int tamano() {
        int count = 0;
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            count++;
            actual = actual.siguiente;
        }
        return count;
    }

    public boolean estaVacia() {
        return cabeza == null;
    }

    /**
     * Retorna representacion visual de la lista para debug.
     * Ejemplo: [Nodo1] -> [Nodo2] -> [Nodo3] -> null
     */
    public String visualizarEstructura() {
        if (estaVacia()) return "Lista vacia: null";
        StringBuilder sb = new StringBuilder();
        NodoLavanderia actual = cabeza;
        while (actual != null) {
            sb.append("[Hab.").append(actual.getNumeroHabitacion())
                    .append(" | ").append(actual.getKilos()).append("kg]");
            if (actual.siguiente != null) sb.append(" -> ");
            actual = actual.siguiente;
        }
        sb.append(" -> null");
        return sb.toString();
    }
}
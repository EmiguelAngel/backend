package com.sistemaventas.backend.observer;

import com.sistemaventas.backend.entity.Producto;

// Observer interface
public interface InventarioObserver {
    void onStockChange(Producto producto, int stockAnterior, int nuevoStock);
    void onStockBajo(Producto producto, int stockActual);
    void onProductoAgotado(Producto producto);
    void onProductoRestockado(Producto producto, int nuevoStock);
}

// Subject interface
@SuppressWarnings("unused")
interface InventarioSubject {
    void agregarObservador(InventarioObserver observer);
    void eliminarObservador(InventarioObserver observer);
    void notificarCambioStock(Producto producto, int stockAnterior, int nuevoStock);
    void notificarStockBajo(Producto producto, int stockActual);
    void notificarProductoAgotado(Producto producto);
    void notificarProductoRestockado(Producto producto, int nuevoStock);
}
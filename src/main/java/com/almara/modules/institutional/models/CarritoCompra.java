package com.almara.modules.institutional.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad de dominio Carrito de Compras para el cliente institucional (HU-22).
 */
@Entity
@Table(name = "carrito_compra")
public class CarritoCompra {

    @Id
    @Column(name = "id_carrito")
    private UUID idCarrito;

    @Column(name = "id_institucion", nullable = false, unique = true)
    private UUID idInstitucion;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion = Instant.now();

    @OneToMany(mappedBy = "carritoCompra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemCarrito> items = new ArrayList<>();

    public CarritoCompra() {
    }

    public CarritoCompra(UUID idCarrito, UUID idInstitucion, Instant fechaCreacion, List<ItemCarrito> items) {
        this.idCarrito = idCarrito;
        this.idInstitucion = idInstitucion;
        this.fechaCreacion = fechaCreacion != null ? fechaCreacion : Instant.now();
        this.items = items != null ? items : new ArrayList<>();
    }

    /**
     * Agrega o reemplaza el plan de suscripción en el carrito (modelo SaaS).
     */
    public void agregarItem(PlanSuscripcion plan, int cantidad) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        // En suscripciones SaaS institucionales se selecciona un plan a la vez
        this.items.clear();

        ItemCarrito nuevoItem = ItemCarrito.builder()
                .idItem(UUID.randomUUID())
                .carritoCompra(this)
                .idPlan(plan.getIdProducto())
                .cantidad(Math.max(1, cantidad))
                .precioUnitario(plan.calcularPrecioFinal())
                .build();

        this.items.add(nuevoItem);
    }

    /**
     * Elimina un ítem específico del carrito.
     */
    public void eliminarItem(UUID idItem) {
        if (this.items != null) {
            this.items.removeIf(item -> item.getIdItem().equals(idItem));
        }
    }

    /**
     * Calcula el subtotal general de todos los ítems agregados.
     */
    public BigDecimal calcularSubtotal() {
        if (this.items == null || this.items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return this.items.stream()
                .map(ItemCarrito::calcularSubtotalItem)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getIdCarrito() {
        return idCarrito;
    }

    public void setIdCarrito(UUID idCarrito) {
        this.idCarrito = idCarrito;
    }

    public UUID getIdInstitucion() {
        return idInstitucion;
    }

    public void setIdInstitucion(UUID idInstitucion) {
        this.idInstitucion = idInstitucion;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Instant fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    public static Constructor builder() {
        return new Constructor();
    }

    public static class Constructor {
        private UUID idCarrito;
        private UUID idInstitucion;
        private Instant fechaCreacion = Instant.now();
        private List<ItemCarrito> items = new ArrayList<>();

        public Constructor idCarrito(UUID idCarrito) {
            this.idCarrito = idCarrito;
            return this;
        }

        public Constructor idInstitucion(UUID idInstitucion) {
            this.idInstitucion = idInstitucion;
            return this;
        }

        public Constructor fechaCreacion(Instant fechaCreacion) {
            this.fechaCreacion = fechaCreacion;
            return this;
        }

        public Constructor items(List<ItemCarrito> items) {
            this.items = items;
            return this;
        }

        public CarritoCompra build() {
            return new CarritoCompra(idCarrito, idInstitucion, fechaCreacion, items);
        }
    }
}

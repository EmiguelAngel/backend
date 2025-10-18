package com.sistemaventas.backend.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sistemaventas.backend.entity.DetalleFactura;
import com.sistemaventas.backend.entity.Factura;

@Service
public class FacturaPdfService {

    /**
     * Genera un PDF de la factura con todos los detalles
     */
    public byte[] generarFacturaPdf(Factura factura) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Título principal
            Paragraph titulo = new Paragraph("FACTURA DE VENTA")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(titulo);
            
            // Información de la empresa
            document.add(new Paragraph("Sistema POS - Tienda")
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5));
            document.add(new Paragraph("NIT: 123.456.789-0")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
            
            // Información de la factura
            Table infoTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            
            infoTable.addCell(createInfoCell("No. Factura:", String.valueOf(factura.getIdFactura())));
            infoTable.addCell(createInfoCell("Fecha:", formatDate(factura.getFecha())));
            
            if (factura.getUsuario() != null) {
                infoTable.addCell(createInfoCell("Cajero:", factura.getUsuario().getNombre()));
                infoTable.addCell(createInfoCell("Email:", factura.getUsuario().getCorreo()));
            }
            
            // Información del pago y titular (si existe)
            if (factura.getPago() != null) {
                infoTable.addCell(createInfoCell("Método de Pago:", factura.getPago().getMetodoPago()));
                
                // Datos del titular si es pago con tarjeta
                if (factura.getPago().getNombreTitular() != null) {
                    infoTable.addCell(createInfoCell("Titular:", factura.getPago().getNombreTitular()));
                }
                if (factura.getPago().getNumeroTarjeta() != null) {
                    infoTable.addCell(createInfoCell("Tarjeta:", factura.getPago().getNumeroTarjeta()));
                }
            }
            
            document.add(infoTable);
            document.add(new Paragraph(" ").setMarginBottom(10)); // Espacio
            
            // Tabla de productos
            Table productTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2, 2}))
                .useAllAvailableWidth();
            
            // Encabezados
            productTable.addHeaderCell(createHeaderCell("Producto"));
            productTable.addHeaderCell(createHeaderCell("Cant."));
            productTable.addHeaderCell(createHeaderCell("Precio Unit."));
            productTable.addHeaderCell(createHeaderCell("Subtotal"));
            
            // Detalles de productos
            for (DetalleFactura detalle : factura.getDetallesFactura()) {
                productTable.addCell(createDataCell(detalle.getProducto().getDescripcion()));
                productTable.addCell(createDataCell(String.valueOf(detalle.getCantidad())));
                productTable.addCell(createDataCell(formatCurrency(detalle.getPrecioUnitario())));
                productTable.addCell(createDataCell(formatCurrency(detalle.getSubtotal())));
            }
            
            document.add(productTable);
            document.add(new Paragraph(" ").setMarginBottom(10)); // Espacio
            
            // Totales
            Table totalTable = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            
            totalTable.addCell(createTotalLabelCell("Subtotal:"));
            totalTable.addCell(createTotalValueCell(formatCurrency(factura.getSubtotal())));
            
            totalTable.addCell(createTotalLabelCell("IVA (19%):"));
            totalTable.addCell(createTotalValueCell(formatCurrency(factura.getIva())));
            
            totalTable.addCell(createTotalLabelCell("TOTAL:"));
            totalTable.addCell(createTotalValueCell(formatCurrency(factura.getTotal())).setBold());
            
            document.add(totalTable);
            
            // Información de pago
            if (factura.getIdPago() != null) {
                document.add(new Paragraph(" ").setMarginBottom(10)); // Espacio
                document.add(new Paragraph("INFORMACIÓN DE PAGO")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10));
                
                // Aquí podrías agregar más detalles del pago si los tienes
                document.add(new Paragraph("ID Pago: " + factura.getIdPago())
                    .setFontSize(10));
            }
            
            // Pie de página
            document.add(new Paragraph(" ").setMarginTop(20));
            document.add(new Paragraph("¡Gracias por su compra!")
                .setFontSize(12)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10));
            
            document.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF de factura: " + e.getMessage(), e);
        }
    }
    
    private Cell createInfoCell(String label, String value) {
        return new Cell()
            .add(new Paragraph(label).setBold().setFontSize(10))
            .add(new Paragraph(value).setFontSize(10))
            .setBorder(null)
            .setPadding(5);
    }
    
    private Cell createHeaderCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
            .setBackgroundColor(ColorConstants.DARK_GRAY)
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(8);
    }
    
    private Cell createDataCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setFontSize(10))
            .setTextAlignment(TextAlignment.CENTER)
            .setPadding(5);
    }
    
    private Cell createTotalLabelCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setBold().setFontSize(11))
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(null)
            .setPadding(3);
    }
    
    private Cell createTotalValueCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setFontSize(11))
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(null)
            .setPadding(3);
    }
    
    private String formatDate(Object date) {
        if (date == null) return "N/A";
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "$0";
        return String.format("$%,.0f", amount.doubleValue());
    }
}
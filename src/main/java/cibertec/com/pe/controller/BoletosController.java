package cibertec.com.pe.controller;

import cibertec.com.pe.model.Boleto;
import cibertec.com.pe.model.Ciudad;
import cibertec.com.pe.model.Venta;
import cibertec.com.pe.model.VentaDetalle;
import cibertec.com.pe.repository.ICiudadRepository;
import cibertec.com.pe.repository.IVentaDetalleRepository;
import cibertec.com.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@SessionAttributes({"boletosAgregados"})
public class BoletosController {

    @Autowired
    private ICiudadRepository iciudadRepository;

    @Autowired
    private IVentaRepository iventaRepository;

    @Autowired
    private IVentaDetalleRepository iventaDetalleRepository;

    @GetMapping("/")
    public String carrito(Model model) {
        List<Ciudad> ciudades = iciudadRepository.findAll();
        List<Boleto> boletosAgregados = (List<Boleto>) model.getAttribute("boletosAgregados");

        if(boletosAgregados.size()>0){
            int elemento = boletosAgregados.size()-1;
            Boleto boletoEncontrado = boletosAgregados.get(elemento);
            model.addAttribute("boleto", boletoEncontrado);
        }else{
            model.addAttribute("boleto", new Boleto());
        }

        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @PostMapping("/agregarBoleto")
    public String agregarBoleto(Model model, @ModelAttribute Boleto boleto) {
        List<Ciudad> ciudades = iciudadRepository.findAll();
        List<Boleto> boletosAgregados = (List<Boleto>) model.getAttribute("boletosAgregados");

        boleto.setSubTotal(boleto.getCantidad() * 50.00);

        boletosAgregados.add(boleto);

        model.addAttribute("boletosAgregados", boletosAgregados);
        model.addAttribute("ciudades", ciudades);
        model.addAttribute("boleto", new Boleto());

        return "index";
    }

    @GetMapping("/comprarBoletos")
    public String comprarBoletos(Model model){
        List<Ciudad> ciudades = iciudadRepository.findAll();
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosAgregados");
        Double montoTotal = 0.0;

        for (Boleto boleto : boletos) {
            montoTotal += boleto.getSubTotal();
        }

        Venta ventaNueva = new Venta();
        ventaNueva.setFechaVenta(new Date());
        ventaNueva.setMontoTotal(montoTotal);
        ventaNueva.setNombreComprador(boletos.get(0).getNombreComprador());

        Venta ventaRealizada = iventaRepository.save(ventaNueva);

        for (Boleto boleto : boletos) {
            VentaDetalle ventaDetalle = new VentaDetalle();

            Ciudad ciudadDestino = iciudadRepository.findById(boleto.getCiudadDestino()).get();
            ventaDetalle.setCiudadDestino(ciudadDestino);
            Ciudad ciudadOrigen = iciudadRepository.findById(boleto.getCiudadOrigen()).get();
            ventaDetalle.setCiudadOrigen(ciudadOrigen);
            ventaDetalle.setCantidad(boleto.getCantidad());
            ventaDetalle.setSubTotal(boleto.getSubTotal());
            LocalDate fechaRetorno = LocalDate.parse(boleto.getFechaRetorno());
            ventaDetalle.setFechaRetorno(fechaRetorno);
            LocalDate fechaSalida = LocalDate.parse(boleto.getFechaSalida());
            ventaDetalle.setFechaViaje(fechaSalida);
            ventaDetalle.setVenta(ventaRealizada);

            iventaDetalleRepository.save(ventaDetalle);
        }

        model.addAttribute("boletosAgregados", new ArrayList<>());
        model.addAttribute("ciudades", ciudades);
        model.addAttribute("boleto", new Boleto());

        return "index";
    }

    @GetMapping("/limpiarFormulario")
    public String limpiarFormulario(Model model){
        List<Ciudad> ciudades = iciudadRepository.findAll();

        model.addAttribute("boleto", new Boleto());
        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @ModelAttribute("boletosAgregados")
    public List<Boleto> boletosComprados() {
        return new ArrayList<>();
    }
}
